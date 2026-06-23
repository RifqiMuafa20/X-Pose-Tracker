package com.rifqidev.x_posetracker.utils.repetition_counter

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.rifqidev.x_posetracker.utils.AngleIdx
import com.rifqidev.x_posetracker.utils.FrontLegState
import com.rifqidev.x_posetracker.utils.IRepetitionCounter
import com.rifqidev.x_posetracker.utils.LandmarkIdx
import com.rifqidev.x_posetracker.utils.MovementState
import com.rifqidev.x_posetracker.utils.ValidationMessage
import com.rifqidev.x_posetracker.utils.ValidationResult
import com.rifqidev.x_posetracker.utils.updateConsecutiveCounter

class LungesCounter : IRepetitionCounter {
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

    private var reachedDown = false
    private var startedFromUp = false

    private var minStateIdx = Int.MAX_VALUE
    private var maxStateIdx = -1

    private var isGoingDown = false
    private var isGoingUp = false

    // Alternating leg validation
    private var lastValidFrontLeg: FrontLegState = FrontLegState.NONE
    private var hadInvalidRepSinceLastValid = false

    // Knee thresholds
    private val KNEE_FULL_UP = 150f
    private val KNEE_NEAR_UP = 130f
    private val KNEE_CENTRE = 120f
    private val KNEE_FULL_DOWN = 100f

    private val TORSO_MAX_DEVIATION = 45f

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

        reachedDown = false
        startedFromUp = false
        isGoingDown = false
        isGoingUp = false
        isInitialized = false
        minStateIdx = Int.MAX_VALUE
        maxStateIdx = -1

        torsoInvalidFrames = 0
        lastValidFrontLeg = FrontLegState.NONE
        hadInvalidRepSinceLastValid = false
    }

    override fun update(angles13: FloatArray, landmarks: List<NormalizedLandmark>) {
        lastValidationResult = ValidationResult(emptyList())
        lastWarningResult = ValidationResult(emptyList())

        val kL = angles13[AngleIdx.LEFT_KNEE]
        val kR = angles13[AngleIdx.RIGHT_KNEE]
        val torso = angles13[AngleIdx.TORSO]

        // State determination
        val newState = determineState(kL, kR)

        if(!isInitialized) {
            currentState = newState
            isInitialized = true
        }

        val oldIdx = stateOrder.indexOf(currentState)
        val newIdx = stateOrder.indexOf(newState)

        // Initial Start
        if (currentState == MovementState.UP && newIdx < oldIdx && !startedFromUp) {
            startedFromUp = true
            isValid = true
        }

        // Posture validation
        if (startedFromUp){
            val postureValidation = validatePosture(torso)

            if (postureValidation.message.isNotEmpty()) {
                invalidateRep(postureValidation.message)
            }
        }

        // Going down
        if (!reachedDown && newIdx < oldIdx && !isGoingDown) {
            isGoingDown = true
            isGoingUp = false
        }

        // Reach DOWN
        if (newState == MovementState.DOWN && !reachedDown) {
            reachedDown = true
            isGoingDown = false
            isGoingUp = false
            maxStateIdx = -1

            // Front leg detection
            if (isValid) {
                val leftHip = landmarks.getOrNull(LandmarkIdx.LEFT_HIP)?.x() ?: 0f
                val rightHip = landmarks.getOrNull(LandmarkIdx.RIGHT_HIP)?.x() ?: 0f
                val leftAnkle = landmarks.getOrNull(LandmarkIdx.LEFT_ANKLE)?.x() ?: 0f
                val rightAnkle = landmarks.getOrNull(LandmarkIdx.RIGHT_ANKLE)?.x() ?: 0f

                val hipCenterX = (leftHip + rightHip) / 2f

                val currentFrontLeg = if (leftAnkle > hipCenterX) {
                    FrontLegState.LEFT_FRONT
                } else if (rightAnkle > hipCenterX) {
                    FrontLegState.RIGHT_FRONT
                } else {
                    FrontLegState.NONE
                }

                // Check leg switching
                if (currentFrontLeg != FrontLegState.NONE && !hadInvalidRepSinceLastValid && lastValidFrontLeg != FrontLegState.NONE) {
                    if (currentFrontLeg == lastValidFrontLeg) {
                        invalidateRep(listOf(ValidationMessage.LEG_NOT_SWITCHED ))
                    } else {
                        ValidationResult(emptyList())
                    }
                }

                if (isValid){
                    lastValidFrontLeg = currentFrontLeg
                    hadInvalidRepSinceLastValid = false
                }
            }
        }

        // Going up
        if (reachedDown && newIdx > oldIdx && !isGoingUp) {
            isGoingUp = true
            isGoingDown = false
        }

        if (isGoingDown) {
            minStateIdx = minOf(minStateIdx, newIdx)
        }

        if (isGoingUp) {
            maxStateIdx = maxOf(maxStateIdx, newIdx)
        }

        // Go Down but Go Up again before DOWN
        if (isGoingDown && newIdx > minStateIdx+2) {
            invalidateRep(listOf(ValidationMessage.NOT_REACH_DOWN ))
            isGoingDown = false
        }

        // Go Up but Go Down again before UP
        if (isGoingUp && newIdx < maxStateIdx-2) {
            invalidateRep(listOf(ValidationMessage.NOT_REACH_UP ))
            isGoingUp = false
        }

        // Count repetition when back to UP
        if (newState == MovementState.UP && newIdx > oldIdx) {
            when {
                isValid && startedFromUp && reachedDown -> {
                    count++
                }

                !startedFromUp -> {
                    invalidateRep(listOf(ValidationMessage.NOT_START_FROM_UP ))
                }
            }

            // reset cycle
            reachedDown = false
            startedFromUp = false
            isGoingDown = false
            isGoingUp = false
            minStateIdx = Int.MAX_VALUE
            maxStateIdx = -1
            torsoInvalidFrames = 0
        }

        currentState = newState
    }

    private fun determineState(kL: Float, kR: Float): MovementState {
        return when {
            kL >= KNEE_FULL_UP && kR >= KNEE_FULL_UP -> MovementState.UP
            kL >= KNEE_NEAR_UP && kR >= KNEE_NEAR_UP -> MovementState.NEAR_UP
            kL >= KNEE_CENTRE && kR >= KNEE_CENTRE -> MovementState.CENTRE
            kL > KNEE_FULL_DOWN && kR > KNEE_FULL_DOWN -> MovementState.NEAR_DOWN
            kL <= KNEE_FULL_DOWN && kR <= KNEE_FULL_DOWN -> MovementState.DOWN
            else -> currentState
        }
    }

    private fun validatePosture(torso: Float): ValidationResult {
        torsoInvalidFrames = updateConsecutiveCounter(
            kotlin.math.abs(torso) > TORSO_MAX_DEVIATION,
            torsoInvalidFrames
        )

        val messages = mutableListOf<ValidationMessage>()

        if (torsoInvalidFrames >= 2){
            messages.add(ValidationMessage.TORSO_TOO_TILTED)
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
        hadInvalidRepSinceLastValid = true
    }
}