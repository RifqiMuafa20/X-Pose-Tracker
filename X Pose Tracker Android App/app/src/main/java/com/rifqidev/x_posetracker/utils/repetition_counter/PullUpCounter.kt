package com.rifqidev.x_posetracker.utils.repetition_counter

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.rifqidev.x_posetracker.utils.AngleIdx
import com.rifqidev.x_posetracker.utils.IRepetitionCounter
import com.rifqidev.x_posetracker.utils.LandmarkIdx
import com.rifqidev.x_posetracker.utils.MovementState
import com.rifqidev.x_posetracker.utils.ValidationMessage
import com.rifqidev.x_posetracker.utils.ValidationResult
import com.rifqidev.x_posetracker.utils.updateConsecutiveCounter

class PullUpCounter : IRepetitionCounter {
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

    private var downNoseY: Float? = null
    private var bodyRoseEnough: Boolean = true
    private var lastDownPostureValidation: Boolean = false

    private val ELBOW_FULL_DOWN = 150f
    private val ELBOW_NEAR_DOWN = 110f
    private val ELBOW_CENTRE = 80f
    private val ELBOW_FULL_UP = 40f

    private val KNEE_MIN_VALID = 120f
    private val HIP_MIN_VALID = 120f

    private val TORSO_MAX_DEVIATION = 45f

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

        downNoseY = null
        bodyRoseEnough = true
        lastDownPostureValidation = false

        hipInvalidFrames = 0
        kneeInvalidFrames = 0
        torsoInvalidFrames = 0
    }

    override fun update(angles13: FloatArray, landmarks: List<NormalizedLandmark>) {
        lastValidationResult = ValidationResult(emptyList())
        lastWarningResult = ValidationResult(emptyList())

        val eL = angles13[AngleIdx.LEFT_ELBOW]
        val eR = angles13[AngleIdx.RIGHT_ELBOW]
        val kL = angles13[AngleIdx.LEFT_KNEE]
        val kR = angles13[AngleIdx.RIGHT_KNEE]
        val hL = angles13[AngleIdx.LEFT_HIP]
        val hR = angles13[AngleIdx.RIGHT_HIP]
        val torso = angles13[AngleIdx.TORSO]

        // Determine state
        val newState = determineState(eL, eR)

        if(!isInitialized) {
            currentState = newState
            isInitialized = true
        }

        val oldIdx = stateOrder.indexOf(currentState)
        val newIdx = stateOrder.indexOf(newState)

        // Initial Start
        if (currentState == MovementState.DOWN && newIdx > oldIdx && !startedFromDown) {
            isValid = true
            startedFromDown = true

            if (lastDownPostureValidation) {
                invalidateRep(listOf(ValidationMessage.BODY_SWAYING))
            }
        }

        // Posture Validation
        val postureValidation = validatePosture(kL, kR, hL, hR, torso)

        if (newState == MovementState.DOWN){
            lastDownPostureValidation = postureValidation.message.isNotEmpty()
        }

        if (startedFromDown){
            if (postureValidation.message.isNotEmpty()) {
                invalidateRep(postureValidation.message)
            }
        }

        // Track elbow Y position
        if (newState == MovementState.DOWN && downNoseY == null) {
            downNoseY = landmarks.getOrNull(LandmarkIdx.NOSE)?.y() ?: 0f
        }

        // Up
        if (!reachedUp && newIdx > oldIdx && !isGoingUp) {
            isGoingUp = true
            isGoingDown = false
        }

        // Reach Up
        if (newState == MovementState.UP && !reachedUp) {
            val leftMouthY = landmarks.getOrNull(LandmarkIdx.LEFT_MOUTH)?.y() ?: 0f
            val rightMouthY = landmarks.getOrNull(LandmarkIdx.RIGHT_MOUTH)?.y() ?: 0f

            val mouthY = (leftMouthY + rightMouthY) / 2f

            val leftThumbY = landmarks.getOrNull(LandmarkIdx.LEFT_THUMB)?.y() ?: 0f
            val rightThumbY = landmarks.getOrNull(LandmarkIdx.RIGHT_THUMB)?.y() ?: 0f

            val thumbY = (leftThumbY + rightThumbY) / 2f

            if (mouthY <= thumbY) {
                reachedUp = true
                isGoingUp = false
                isGoingDown = false
                minStateIdx = Int.MAX_VALUE

                if(!bodyRoseEnough && downNoseY != null){
                    val shoulderY = ((landmarks.getOrNull(LandmarkIdx.LEFT_SHOULDER)?.y() ?: 0f) + (landmarks.getOrNull(LandmarkIdx.RIGHT_SHOULDER)?.y() ?: 0f)) / 2f

                    bodyRoseEnough = if (shoulderY <= downNoseY!!) {
                        true
                    } else {
                        false
                    }
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

        // Go Up but Go Down again before UP
        if (isGoingUp && newIdx < maxStateIdx-1) {
            invalidateRep(listOf(ValidationMessage.NOT_REACH_UP))
            isGoingUp = false
        }

        // Go Down but Go Up again before DOWN
        if (isGoingDown && newIdx > minStateIdx+1) {
            invalidateRep(listOf(ValidationMessage.NOT_REACH_DOWN))
            isGoingDown = false
        }

        // Repetition Count
        if (newState == MovementState.DOWN && newIdx < oldIdx) {
            when {
                isValid && reachedUp && startedFromDown && bodyRoseEnough -> {
                    count++
                }

                !startedFromDown -> {
                    invalidateRep(listOf(ValidationMessage.NOT_START_FROM_DOWN))
                }

                reachedUp && !bodyRoseEnough -> {
                    invalidateRep(listOf(ValidationMessage.BODY_NOT_HIGH_ENOUGH))
                }
            }

            // Reset siklus
            reachedUp = false
            isGoingUp = false
            isGoingDown = false
            startedFromDown = false
            minStateIdx = Int.MAX_VALUE
            maxStateIdx = -1
            downNoseY = null

            bodyRoseEnough = true
            lastDownPostureValidation = false

            hipInvalidFrames = 0
            kneeInvalidFrames = 0
            torsoInvalidFrames = 0
        }

        currentState = newState
    }

    private fun determineState(eL: Float, eR: Float): MovementState {
        return when {
            eL >= ELBOW_FULL_DOWN && eR >= ELBOW_FULL_DOWN -> MovementState.DOWN
            eL >= ELBOW_NEAR_DOWN && eR >= ELBOW_NEAR_DOWN -> MovementState.NEAR_DOWN
            eL >= ELBOW_CENTRE && eR >= ELBOW_CENTRE -> MovementState.CENTRE
            eL > ELBOW_FULL_UP && eR > ELBOW_FULL_UP -> MovementState.NEAR_UP
            eL <= ELBOW_FULL_UP && eR <= ELBOW_FULL_UP -> MovementState.UP
            else -> currentState
        }
    }

    private fun validatePosture( kL: Float, kR: Float, hL: Float, hR: Float, torso: Float ): ValidationResult {
        kneeInvalidFrames = updateConsecutiveCounter(
            kL < KNEE_MIN_VALID || kR < KNEE_MIN_VALID,
            kneeInvalidFrames
        )

        hipInvalidFrames = updateConsecutiveCounter(
            hL < HIP_MIN_VALID || hR < HIP_MIN_VALID,
            hipInvalidFrames
        )

        torsoInvalidFrames = updateConsecutiveCounter(
            kotlin.math.abs(torso) > TORSO_MAX_DEVIATION,
            torsoInvalidFrames
        )

        val messages = mutableListOf<ValidationMessage>()

        if (kneeInvalidFrames >= 2) {
            messages.add(ValidationMessage.KNEE_NOT_STRAIGHT)
        }

        if (hipInvalidFrames >= 2){
            messages.add(ValidationMessage.HIP_NOT_STRAIGHT)
        }

        if (torsoInvalidFrames >= 2) {
            messages.add(ValidationMessage.TORSO_NOT_VERTICAL)
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