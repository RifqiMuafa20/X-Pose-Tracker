package com.rifqidev.x_posetracker.utils.repetition_counter

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.rifqidev.x_posetracker.utils.AngleIdx
import com.rifqidev.x_posetracker.utils.IRepetitionCounter
import com.rifqidev.x_posetracker.utils.LandmarkIdx
import com.rifqidev.x_posetracker.utils.MovementState
import com.rifqidev.x_posetracker.utils.ValidationMessage
import com.rifqidev.x_posetracker.utils.ValidationResult
import com.rifqidev.x_posetracker.utils.updateConsecutiveCounter

class SitUpCounter : IRepetitionCounter {
    override var count: Int = 0
        private set

    override var currentState: MovementState = MovementState.CENTRE
        private set

    private var newState: MovementState = MovementState.CENTRE

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

    private val HIP_FULL_DOWN = 110f
    private val HIP_NEAR_DOWN = 90f
    private val HIP_CENTRE = 65f
    private val HIP_FULL_UP = 45f

    private val KNEE_MAX_VALID = 100f
    private val TORSO_MIN_FOR_DOWN = 80f

    private var kneeInvalidFrames = 0
    private var handNotBehindHeadFrames = 0

    private var elbowTouchedFloor = false
    private var elbowTouchedKnee = false

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
        newState = MovementState.CENTRE
        lastValidationResult = ValidationResult(emptyList())
        lastWarningResult = ValidationResult(emptyList())
        isValid = true

        reachedUp = false
        isGoingUp = false
        startedFromDown = false
        isGoingDown = false
        isInitialized = false
        minStateIdx = Int.MAX_VALUE
        maxStateIdx = -1
        kneeInvalidFrames = 0
        handNotBehindHeadFrames = 0
        elbowTouchedFloor = false
        elbowTouchedKnee = false
    }

    override fun update(angles13: FloatArray, landmarks: List<NormalizedLandmark>) {
        lastValidationResult = ValidationResult(emptyList())
        lastWarningResult = ValidationResult(emptyList())

        val hL = angles13[AngleIdx.LEFT_HIP]
        val hR = angles13[AngleIdx.RIGHT_HIP]
        val kL = angles13[AngleIdx.LEFT_KNEE]
        val kR = angles13[AngleIdx.RIGHT_KNEE]
        val torso = angles13[AngleIdx.TORSO]

        // State Determination
        newState = determineState(hL, hR, torso)

        if(!isInitialized) {
            currentState = newState
            isInitialized = true
        }

        val oldIdx = stateOrder.indexOf(currentState)
        val newIdx = stateOrder.indexOf(newState)

        // Warnings Validation
        updateSitUpWarnings(landmarks)

        // Initial Start
        if (currentState == MovementState.DOWN && newIdx > oldIdx && !startedFromDown) {
            startedFromDown = true
            isValid = true

            if (!elbowTouchedFloor) {
                warningRep(listOf(ValidationMessage.ELBOW_NOT_TOUCH_FLOOR))
            }
        }

        // Posture Validation
        if (startedFromDown){
            val postureValidation = validatePosture(kL, kR)

            if (postureValidation.message.isNotEmpty()) {
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
        }

        // Down
        if (reachedUp && newIdx < oldIdx && !isGoingDown) {
            isGoingDown = true
            isGoingUp = false

            if (!elbowTouchedKnee) {
                warningRep(listOf(ValidationMessage.ELBOW_NOT_TOUCH_KNEE))
            }
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

        // Go down but go up again before going Down
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

            // Reset siklus
            reachedUp = false
            isGoingUp = false
            isGoingDown = false
            startedFromDown = false
            minStateIdx = Int.MAX_VALUE
            maxStateIdx = -1
            kneeInvalidFrames = 0
            handNotBehindHeadFrames = 0

            elbowTouchedFloor = false
            elbowTouchedKnee = false
        }

        currentState = newState
    }

    private fun determineState(hL: Float, hR: Float, torso: Float): MovementState {
        val isDownPosition = hL >= HIP_FULL_DOWN && hR >= HIP_FULL_DOWN && torso >= TORSO_MIN_FOR_DOWN
        val isNearDownPosition = hL >= HIP_NEAR_DOWN && hR >= HIP_NEAR_DOWN

        val isUpPosition = hL <= HIP_FULL_UP && hR <= HIP_FULL_UP
        val isNearUpPosition = hL > HIP_FULL_UP && hR > HIP_FULL_UP

        return when {
            isDownPosition -> MovementState.DOWN
            isNearDownPosition -> MovementState.NEAR_DOWN
            hL >= HIP_CENTRE && hR >= HIP_CENTRE -> MovementState.CENTRE
            isNearUpPosition -> MovementState.NEAR_UP
            isUpPosition -> MovementState.UP
            else -> currentState
        }
    }

    private fun validatePosture(kL: Float, kR: Float): ValidationResult {
        kneeInvalidFrames = updateConsecutiveCounter(
            kL > KNEE_MAX_VALID || kR > KNEE_MAX_VALID,
            kneeInvalidFrames
        )

        val messages = mutableListOf<ValidationMessage>()

        if (kneeInvalidFrames >= 2) {
            messages.add(ValidationMessage.KNEE_TOO_WIDE)
        }

        return if (messages.isEmpty()) {
            ValidationResult(emptyList())
        } else {
            ValidationResult(messages)
        }
    }

    private fun updateSitUpWarnings(landmarks: List<NormalizedLandmark>) {
        // DOWN position -> elbow touch floor
        if (newState == MovementState.DOWN) {
            val leftElbowY = landmarks[LandmarkIdx.LEFT_ELBOW].y()
            val rightElbowY = landmarks[LandmarkIdx.RIGHT_ELBOW].y()

            val avgShoulderY = (landmarks[LandmarkIdx.LEFT_SHOULDER].y() + landmarks[LandmarkIdx.RIGHT_SHOULDER].y()) / 2f

            if ( leftElbowY >= avgShoulderY || rightElbowY >= avgShoulderY) {
                elbowTouchedFloor = true
            }
        }

        // UP position -> elbow touch knee
        if (newState == MovementState.UP) {
            val minElbowX = minOf(landmarks[LandmarkIdx.LEFT_ELBOW].x(), landmarks[LandmarkIdx.RIGHT_ELBOW].x())

            val maxKneeX = maxOf(landmarks[LandmarkIdx.LEFT_KNEE].x(), landmarks[LandmarkIdx.RIGHT_KNEE].x())

            if (minElbowX <= maxKneeX) {
                elbowTouchedKnee = true
            }
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

    private fun warningRep(message: List<ValidationMessage>) {
        lastWarningResult =
            ValidationResult(
                (lastWarningResult.message + message)
                    .distinct()
            )
    }
}