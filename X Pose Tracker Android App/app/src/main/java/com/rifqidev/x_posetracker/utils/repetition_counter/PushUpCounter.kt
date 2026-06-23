package com.rifqidev.x_posetracker.utils.repetition_counter

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.rifqidev.x_posetracker.utils.AngleIdx
import com.rifqidev.x_posetracker.utils.IRepetitionCounter
import com.rifqidev.x_posetracker.utils.LandmarkIdx
import com.rifqidev.x_posetracker.utils.MovementState
import com.rifqidev.x_posetracker.utils.ValidationMessage
import com.rifqidev.x_posetracker.utils.ValidationResult
import com.rifqidev.x_posetracker.utils.updateConsecutiveCounter

class PushUpCounter : IRepetitionCounter {
    override var count: Int = 0
        private set

    override var currentState: MovementState = MovementState.CENTRE
        private set

    override var lastValidationResult: ValidationResult =
        ValidationResult(emptyList())
        private set

    override var lastWarningResult: ValidationResult =
        ValidationResult(emptyList())
        private set

    override var isValid: Boolean = true
        private set

    private var isInitialized = false

    private var reachedUp = false
    private var startedFromDown = false

    private var minStateIdx = Int.MAX_VALUE
    private var maxStateIdx = -1

    private var isGoingUp = false
    private var isGoingDown = false

    private val ELBOW_FULL_UP = 160f
    private val ELBOW_CENTRE = 130f
    private val ELBOW_NEAR_DOWN = 100f
    private val ELBOW_FULL_DOWN = 70f

    private val HIP_MIN_VALID = 150f
    private val KNEE_MIN_VALID = 150f
    private val TORSO_MIN_DEVIATION = 45f

    private var hipInvalidFrames = 0
    private var kneeInvalidFrames = 0
    private var torsoInvalidFrames = 0

    private val stateOrder = listOf(
        MovementState.DOWN,
        MovementState.NEAR_DOWN,
        MovementState.CENTRE,
        MovementState.NEAR_UP,
        MovementState.UP
    )

    private var downShoulderY: Float? = null
    private var downHipY: Float? = null
    private var upShoulderY: Float? = null
    private var upHipY: Float? = null

    override fun resetAll() {
        count = 0
        resetStateOnly()
    }

    override fun resetStateOnly() {
        currentState = MovementState.CENTRE
        lastValidationResult = ValidationResult(emptyList())
        lastWarningResult = ValidationResult(emptyList())
        isValid = true

        reachedUp = false
        isGoingUp = false
        isGoingDown = false
        startedFromDown = false
        isInitialized = false
        minStateIdx = Int.MAX_VALUE
        maxStateIdx = -1

        hipInvalidFrames = 0
        kneeInvalidFrames = 0
        torsoInvalidFrames = 0

        downShoulderY = null
        downHipY = null
        upShoulderY = null
        upHipY = null
    }

    override fun update(angles13: FloatArray, landmarks: List<NormalizedLandmark>) {
        lastValidationResult = ValidationResult(emptyList())
        lastWarningResult = ValidationResult(emptyList())

        val eL = angles13[AngleIdx.LEFT_ELBOW]
        val eR = angles13[AngleIdx.RIGHT_ELBOW]
        val hL = angles13[AngleIdx.LEFT_HIP]
        val hR = angles13[AngleIdx.RIGHT_HIP]
        val kL = angles13[AngleIdx.LEFT_KNEE]
        val kR = angles13[AngleIdx.RIGHT_KNEE]
        val torso = angles13[AngleIdx.TORSO]

        // determine state
        val newState = determineState(eL, eR)

        if(!isInitialized) {
            currentState = newState
            isInitialized = true
        }

        val oldIdx = stateOrder.indexOf(currentState)
        val newIdx = stateOrder.indexOf(newState)

        // Initial Start
        if (currentState == MovementState.DOWN && newIdx > oldIdx && !startedFromDown) {
            startedFromDown = true
            isValid = true

            // Track hip movement
            if (downShoulderY == null && downHipY == null) {
                val leftShoulderY = landmarks.getOrNull(LandmarkIdx.LEFT_SHOULDER)?.y() ?: 0f
                val rightShoulderY = landmarks.getOrNull(LandmarkIdx.RIGHT_SHOULDER)?.y() ?: 0f
                val leftHipY = landmarks.getOrNull(LandmarkIdx.LEFT_HIP)?.y() ?: 0f
                val rightHipY = landmarks.getOrNull(LandmarkIdx.RIGHT_HIP)?.y() ?: 0f

                downShoulderY = (leftShoulderY + rightShoulderY) / 2f
                downHipY = (leftHipY + rightHipY) / 2f
            }
        }

        // Posture Validation
        if (startedFromDown){
            val postureValidation = validatePosture(hL, hR, kL, kR, torso)

            if (postureValidation.message.isNotEmpty() && newState != MovementState.UP) {
                invalidateRep(postureValidation.message)
            }
        }

        // Up
        if (!reachedUp && newIdx > oldIdx && !isGoingUp) {
            isGoingUp = true
            isGoingDown = false
        }

        // Reach Up
        if (newState == MovementState.UP && !reachedUp) {
            reachedUp = true
            isGoingUp = false
            isGoingDown = false
            minStateIdx = Int.MAX_VALUE

            // Hip movement validation
            val leftShoulderY = landmarks.getOrNull(LandmarkIdx.LEFT_SHOULDER)?.y() ?: 0f
            val rightShoulderY = landmarks.getOrNull(LandmarkIdx.RIGHT_SHOULDER)?.y() ?: 0f
            val leftHipY = landmarks.getOrNull(LandmarkIdx.LEFT_HIP)?.y() ?: 0f
            val rightHipY = landmarks.getOrNull(LandmarkIdx.RIGHT_HIP)?.y() ?: 0f

            upShoulderY = (leftShoulderY + rightShoulderY) / 2f
            upHipY = (leftHipY + rightHipY) / 2f

            if(isValid){
                val hipMovementValidation = evaluateHipMovement()

                if(!hipMovementValidation){
                    invalidateRep(listOf(ValidationMessage.HIP_NOT_FOLLOWING))
                }
            }
        }

        // Down
        if (reachedUp && newIdx < oldIdx && !isGoingDown) {
            isGoingDown = true
            isGoingUp = false
        }

        if (isGoingUp) {
            maxStateIdx = maxOf(maxStateIdx, newIdx)
        }

        if (isGoingDown) {
            minStateIdx = minOf(minStateIdx, newIdx)
        }

        // Go Up but go Down again before UP
        if (isGoingUp && newIdx < maxStateIdx-1) {
            invalidateRep(listOf(ValidationMessage.NOT_REACH_UP))
            isGoingUp = false
        }

        // Go Down but go Up again before going Down
        if (isGoingDown && newIdx > minStateIdx+1) {
            invalidateRep(listOf(ValidationMessage.NOT_REACH_DOWN))
            isGoingDown = false
        }

        // Repetition Counting
        if (newState == MovementState.DOWN && newIdx < oldIdx) {
            when {
                isValid && reachedUp && startedFromDown -> {
                    count++
                }

                !startedFromDown -> {
                    invalidateRep(listOf(ValidationMessage.NOT_START_FROM_DOWN))
                }
            }

            // Reset cycle
            reachedUp = false
            isGoingUp = false
            isGoingDown = false
            startedFromDown = false
            minStateIdx = Int.MAX_VALUE
            maxStateIdx = -1

            downShoulderY = null
            downHipY = null
            upShoulderY = null
            upHipY = null

            hipInvalidFrames = 0
            kneeInvalidFrames = 0
            torsoInvalidFrames = 0
        }

        currentState = newState
    }

    private fun evaluateHipMovement(): Boolean {
        val shoulderMovement = kotlin.math.abs((upShoulderY ?: 0f) - (downShoulderY ?: 0f))
        if (shoulderMovement < 0.02f) {
            return false
        }

        val hipMovement = kotlin.math.abs((upHipY ?: 0f) - (downHipY ?: 0f))
        val movementRatio = hipMovement / shoulderMovement

        return movementRatio >= 0.40f
    }

    private fun determineState(eL: Float, eR: Float): MovementState {
        return when {
            eL <= ELBOW_FULL_DOWN && eR <= ELBOW_FULL_DOWN -> MovementState.DOWN
            eL <= ELBOW_NEAR_DOWN && eR <= ELBOW_NEAR_DOWN -> MovementState.NEAR_DOWN
            eL <= ELBOW_CENTRE && eR <= ELBOW_CENTRE -> MovementState.CENTRE
            eL < ELBOW_FULL_UP && eR < ELBOW_FULL_UP -> MovementState.NEAR_UP
            eL >= ELBOW_FULL_UP && eR >= ELBOW_FULL_UP -> MovementState.UP
            else -> currentState
        }
    }

    private fun validatePosture(hL: Float, hR: Float, kL: Float, kR: Float, torso: Float): ValidationResult {
        hipInvalidFrames = updateConsecutiveCounter(
            hL < HIP_MIN_VALID || hR < HIP_MIN_VALID,
            hipInvalidFrames
        )

        kneeInvalidFrames = updateConsecutiveCounter(
            kL < KNEE_MIN_VALID || kR < KNEE_MIN_VALID,
            kneeInvalidFrames
        )

        torsoInvalidFrames = updateConsecutiveCounter(
            kotlin.math.abs(torso) < TORSO_MIN_DEVIATION,
            torsoInvalidFrames
        )

        val messages = mutableListOf<ValidationMessage>()

        if (hipInvalidFrames >= 2) {
            messages.add(ValidationMessage.HIP_TOO_BENT)
        }

        if (kneeInvalidFrames >= 2) {
            messages.add(ValidationMessage.KNEE_TOO_BENT)
        }

        if (torsoInvalidFrames >= 2) {
            messages.add(ValidationMessage.TORSO_NOT_HORIZONTAL)
        }

        return if (messages.isEmpty()) {
            ValidationResult(emptyList())
        } else {
            ValidationResult(messages)
        }
    }

    private fun invalidateRep(messages: List<ValidationMessage>) {
        lastValidationResult =
            ValidationResult(
                (lastValidationResult.message + messages)
                    .distinct()
            )

        isValid = false
    }
}