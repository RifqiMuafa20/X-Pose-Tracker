package com.rifqidev.x_posetracker.utils

import com.rifqidev.x_posetracker.data.CounterResult
import com.rifqidev.x_posetracker.data.ValidationResult

object AngleIdx {
    const val LEFT_ELBOW = 1
    const val RIGHT_ELBOW = 7
    const val LEFT_HIP = 3
    const val RIGHT_HIP = 9
    const val LEFT_KNEE = 4
    const val RIGHT_KNEE = 10
    const val TORSO = 12
}

enum class MovementState {
    DOWN,
    NEAR_DOWN,
    CENTRE,
    NEAR_UP,
    UP
}

enum class ValidationMessage {
    HIP_TOO_BENT,
    KNEE_TOO_BENT,
    TORSO_NOT_HORIZONTAL,
    KNEE_TOO_WIDE,
    KNEE_NOT_STRAIGHT,
    HIP_NOT_STRAIGHT,
    TORSO_NOT_VERTICAL,
    TORSO_TOO_TILTED,
    NOT_START_FROM_UP,
    NOT_REACH_UP,
    NOT_REACH_DOWN,
    NOT_START_FROM_DOWN
}

interface IRepetitionCounter {
    val count: Int
    val currentState: MovementState
    val lastValidationResult: ValidationResult

    fun resetAll()
    fun resetStateOnly()
    fun update(angles13: FloatArray): CounterResult
}

class RepetitionCounterEngine(
    counters: Map<String, IRepetitionCounter>
) {
    private val counters = counters.toMutableMap()
    private var activeLabel: String? = null

    fun resetSession() {
        counters.values.forEach { it.resetAll() }
        activeLabel = null
    }

    fun update(label: String, angles13: FloatArray): CounterResult? {
        if (label != activeLabel) {
            counters[label]?.resetStateOnly()
            activeLabel = label
        }
        return counters[label]?.update(angles13)
    }

    fun getCount(label: String): Int = counters[label]?.count ?: 0
    fun getState(label: String): MovementState? = counters[label]?.currentState
    fun getLastValidation(label: String): ValidationResult? = counters[label]?.lastValidationResult
}

class PushUpCounter : IRepetitionCounter {
    override var count: Int = 0
        private set

    override var currentState: MovementState = MovementState.CENTRE
        private set

    override var lastValidationResult: ValidationResult =
        ValidationResult(true, emptyList())
        private set

    private var isInvalidCycle = false
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
        lastValidationResult = ValidationResult(true, emptyList())
        isInvalidCycle = false
        reachedUp = false
        isGoingUp = false
        isGoingDown = false
        startedFromDown = false
        isInitialized = false
        minStateIdx = Int.MAX_VALUE
        maxStateIdx = -1
    }

    override fun update(angles13: FloatArray): CounterResult {
        val eL = angles13[AngleIdx.LEFT_ELBOW]
        val eR = angles13[AngleIdx.RIGHT_ELBOW]
        val hL = angles13[AngleIdx.LEFT_HIP]
        val hR = angles13[AngleIdx.RIGHT_HIP]
        val kL = angles13[AngleIdx.LEFT_KNEE]
        val kR = angles13[AngleIdx.RIGHT_KNEE]
        val torso = angles13[AngleIdx.TORSO]

        // Posture Validation
        val postureValidation = validatePosture(hL, hR, kL, kR, torso)

        if (!postureValidation.isValid) {
            isInvalidCycle = true
            lastValidationResult = postureValidation
        }

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
            isInvalidCycle = false
            lastValidationResult = ValidationResult(true, emptyList())
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
        }

        if (isGoingUp) {
            maxStateIdx = maxOf(maxStateIdx, newIdx)
        }

        if (isGoingDown) {
            minStateIdx = minOf(minStateIdx, newIdx)
        }

        // Go Up but go Down again before UP
        if (isGoingUp && newIdx < maxStateIdx-1) {
            isInvalidCycle = true
            lastValidationResult = ValidationResult(false, listOf(ValidationMessage.NOT_REACH_UP))
            isGoingUp = false
        }

        // Go Down but go Up again before going Down
        if (isGoingDown && newIdx > minStateIdx+1) {
            isInvalidCycle = true
            lastValidationResult = ValidationResult(false, listOf(ValidationMessage.NOT_REACH_DOWN))
            isGoingDown = false
        }

        // Repetition Counting
        if (newState == MovementState.DOWN && newIdx < oldIdx) {
            when {
                !isInvalidCycle && reachedUp && startedFromDown -> {
                    count++
                    lastValidationResult = ValidationResult(true, emptyList())
                }

                !startedFromDown -> {
                    lastValidationResult = ValidationResult(false, listOf(ValidationMessage.NOT_START_FROM_DOWN))
                }
            }

            // Reset cycle
            isInvalidCycle = false
            reachedUp = false
            isGoingUp = false
            isGoingDown = false
            startedFromDown = false
            minStateIdx = Int.MAX_VALUE
            maxStateIdx = -1
        }

        currentState = newState
        return CounterResult(count, currentState, lastValidationResult)
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
        val messages = mutableListOf<ValidationMessage>()

        if (hL < HIP_MIN_VALID || hR < HIP_MIN_VALID)
            messages.add(ValidationMessage.HIP_TOO_BENT)

        if (kL < KNEE_MIN_VALID || kR < KNEE_MIN_VALID)
            messages.add(ValidationMessage.KNEE_TOO_BENT)

        if (torso < TORSO_MIN_DEVIATION)
            messages.add(ValidationMessage.TORSO_NOT_HORIZONTAL)

        return if (messages.isEmpty()) {
            ValidationResult(true, emptyList())
        } else {
            ValidationResult(false, messages)
        }
    }
}

class SitUpCounter : IRepetitionCounter {
    override var count: Int = 0
        private set

    override var currentState: MovementState = MovementState.CENTRE
        private set

    override var lastValidationResult: ValidationResult =
        ValidationResult(true, emptyList())
        private set

    private var isInvalidCycle = false
    private var isInitialized = false

    private var reachedUp = false
    private var startedFromDown = false

    private var minStateIdx = Int.MAX_VALUE
    private var maxStateIdx = -1

    private var isGoingUp = false
    private var isGoingDown = false

    private val HIP_FULL_DOWN = 110f
    private val HIP_NEAR_DOWN = 90f
    private val HIP_CENTRE = 70f
    private val HIP_FULL_UP = 50f

    private val KNEE_MAX_VALID = 100f

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
        lastValidationResult = ValidationResult(true, emptyList())
        isInvalidCycle = false
        reachedUp = false
        isGoingUp = false
        startedFromDown = false
        isGoingDown = false
        isInitialized = false
        minStateIdx = Int.MAX_VALUE
        maxStateIdx = -1
    }

    override fun update(angles13: FloatArray): CounterResult {
        val hL = angles13[AngleIdx.LEFT_HIP]
        val hR = angles13[AngleIdx.RIGHT_HIP]
        val kL = angles13[AngleIdx.LEFT_KNEE]
        val kR = angles13[AngleIdx.RIGHT_KNEE]

        // Posture Validation
        val postureValidation = validatePosture(kL, kR)

        if (!postureValidation.isValid) {
            isInvalidCycle = true
            lastValidationResult = postureValidation
        }

        // State Determination
        val newState = determineState(hL, hR)

        if(!isInitialized) {
            currentState = newState
            isInitialized = true
        }

        val oldIdx = stateOrder.indexOf(currentState)
        val newIdx = stateOrder.indexOf(newState)

        // Initial Start
        if (currentState == MovementState.DOWN && newIdx > oldIdx && !startedFromDown) {
            startedFromDown = true
            isInvalidCycle = false
            lastValidationResult = ValidationResult(true, emptyList())
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
        }

        if (isGoingUp) {
            maxStateIdx = maxOf(maxStateIdx, newIdx)
        }

        if (isGoingDown) {
            minStateIdx = minOf(minStateIdx, newIdx)
        }

        // Go Up but go Down again before UP
        if (isGoingUp && newIdx < maxStateIdx-1) {
            isInvalidCycle = true
            lastValidationResult = ValidationResult(false, listOf(ValidationMessage.NOT_REACH_UP))
            isGoingUp = false
        }

        // Go down but go up again before going Down
        if (isGoingDown && newIdx > minStateIdx+1) {
            isInvalidCycle = true
            lastValidationResult = ValidationResult(false, listOf(ValidationMessage.NOT_REACH_DOWN))
            isGoingDown = false
        }

        // Repetition Counting
        if (newState == MovementState.DOWN && newIdx < oldIdx) {
            when {
                !isInvalidCycle && reachedUp && startedFromDown -> {
                    count++
                    lastValidationResult = ValidationResult(true, emptyList())
                }

                !startedFromDown -> {
                    lastValidationResult = ValidationResult(false, listOf(ValidationMessage.NOT_START_FROM_DOWN))
                }
            }

            // Reset siklus
            isInvalidCycle = false
            reachedUp = false
            isGoingUp = false
            isGoingDown = false
            startedFromDown = false
            minStateIdx = Int.MAX_VALUE
            maxStateIdx = -1
        }

        currentState = newState
        return CounterResult(count, currentState, lastValidationResult)
    }

    private fun determineState(hL: Float, hR: Float): MovementState {
        return when {
            hL >= HIP_FULL_DOWN && hR >= HIP_FULL_DOWN -> MovementState.DOWN
            hL >= HIP_NEAR_DOWN && hR >= HIP_NEAR_DOWN -> MovementState.NEAR_DOWN
            hL >= HIP_CENTRE && hR >= HIP_CENTRE -> MovementState.CENTRE
            hL > HIP_FULL_UP && hR > HIP_FULL_UP -> MovementState.NEAR_UP
            hL <= HIP_FULL_UP && hR <= HIP_FULL_UP -> MovementState.UP
            else -> currentState
        }
    }

    private fun validatePosture(kL: Float, kR: Float): ValidationResult {
        return if (kL > KNEE_MAX_VALID || kR > KNEE_MAX_VALID) {
            ValidationResult(false, listOf(ValidationMessage.KNEE_TOO_WIDE))
        } else {
            ValidationResult(true, emptyList())
        }
    }
}

class PullUpCounter : IRepetitionCounter {
    override var count: Int = 0
        private set

    override var currentState: MovementState = MovementState.CENTRE
        private set

    override var lastValidationResult: ValidationResult =
        ValidationResult(true, emptyList())
        private set

    private var isInvalidCycle = false
    private var isInitialized = false

    private var reachedUp = false
    private var startedFromDown = false

    private var minStateIdx = Int.MAX_VALUE
    private var maxStateIdx = -1

    private var isGoingUp = false
    private var isGoingDown = false

    private val ELBOW_FULL_DOWN = 130f
    private val ELBOW_NEAR_DOWN = 100f
    private val ELBOW_CENTRE = 80f
    private val ELBOW_FULL_UP = 50f

    private val KNEE_MIN_VALID = 120f
    private val HIP_MIN_VALID = 120f

    private val TORSO_MAX_DEVIATION = 45f

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
        lastValidationResult = ValidationResult(true, emptyList())
        isInvalidCycle = false
        reachedUp = false
        isGoingUp = false
        isGoingDown = false
        startedFromDown = false
        isInitialized = false
        minStateIdx = Int.MAX_VALUE
        maxStateIdx = -1
    }

    override fun update(angles13: FloatArray): CounterResult {
        val eL = angles13[AngleIdx.LEFT_ELBOW]
        val eR = angles13[AngleIdx.RIGHT_ELBOW]
        val kL = angles13[AngleIdx.LEFT_KNEE]
        val kR = angles13[AngleIdx.RIGHT_KNEE]
        val hL = angles13[AngleIdx.LEFT_HIP]
        val hR = angles13[AngleIdx.RIGHT_HIP]
        val torso = angles13[AngleIdx.TORSO]

        // Posture Validation
        val postureValidation = validatePosture(kL, kR, hL, hR, torso)

        if (!postureValidation.isValid) {
            isInvalidCycle = true
            lastValidationResult = postureValidation
        }

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
            startedFromDown = true
            isInvalidCycle = false
            lastValidationResult = ValidationResult(true, emptyList())
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
        }

        if (isGoingUp) {
            maxStateIdx = maxOf(maxStateIdx, newIdx)
        }

        if (isGoingDown) {
            minStateIdx = minOf(minStateIdx, newIdx)
        }

        // Go Up but Go Down again before UP
        if (isGoingUp && newIdx < maxStateIdx-1) {
            isInvalidCycle = true
            lastValidationResult = ValidationResult(false, listOf(ValidationMessage.NOT_REACH_UP))
            isGoingUp = false
        }

        // Go Down but Go Up again before DOWN
        if (isGoingDown && newIdx > minStateIdx+1) {
            isInvalidCycle = true
            lastValidationResult = ValidationResult(false, listOf(ValidationMessage.NOT_REACH_DOWN))
            isGoingDown = false
        }

        // Repetition Count
        if (newState == MovementState.DOWN && newIdx < oldIdx) {
            when {
                !isInvalidCycle && reachedUp && startedFromDown -> {
                    count++
                    lastValidationResult = ValidationResult(true, emptyList())
                }

                !startedFromDown -> {
                    lastValidationResult = ValidationResult(false, listOf(ValidationMessage.NOT_START_FROM_DOWN))
                }
            }

            // Reset siklus
            isInvalidCycle = false
            reachedUp = false
            isGoingUp = false
            isGoingDown = false
            startedFromDown = false
            minStateIdx = Int.MAX_VALUE
            maxStateIdx = -1
        }

        currentState = newState
        return CounterResult(count, currentState, lastValidationResult)
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
        val messages = mutableListOf<ValidationMessage>()

        if (kL < KNEE_MIN_VALID || kR < KNEE_MIN_VALID)
            messages.add(ValidationMessage.KNEE_NOT_STRAIGHT)

        if (hL < HIP_MIN_VALID || hR < HIP_MIN_VALID)
            messages.add(ValidationMessage.HIP_NOT_STRAIGHT)

        if (torso > TORSO_MAX_DEVIATION)
            messages.add(ValidationMessage.TORSO_NOT_VERTICAL)

        return if (messages.isEmpty()) {
            ValidationResult(true, emptyList())
        } else {
            ValidationResult(false, messages)
        }
    }
}

class LungesCounter : IRepetitionCounter {
    override var count: Int = 0
        private set

    override var currentState: MovementState = MovementState.CENTRE
        private set

    override var lastValidationResult: ValidationResult =
        ValidationResult(true, emptyList())
        private set

    private var isInvalidCycle = false
    private var isInitialized = false

    private var reachedDown = false
    private var startedFromUp = false

    private var minStateIdx = Int.MAX_VALUE
    private var maxStateIdx = -1

    private var isGoingDown = false
    private var isGoingUp = false

    // Knee thresholds
    private val KNEE_FULL_UP = 160f
    private val KNEE_NEAR_UP = 130f
    private val KNEE_CENTRE = 120f
    private val KNEE_FULL_DOWN = 100f

    private val TORSO_MAX_DEVIATION = 45f

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
        lastValidationResult = ValidationResult(true, emptyList())
        isInvalidCycle = false
        reachedDown = false
        startedFromUp = false
        isGoingDown = false
        isGoingUp = false
        isInitialized = false
        minStateIdx = Int.MAX_VALUE
        maxStateIdx = -1
    }

    override fun update(angles13: FloatArray): CounterResult {
        val kL = angles13[AngleIdx.LEFT_KNEE]
        val kR = angles13[AngleIdx.RIGHT_KNEE]
        val torso = angles13[AngleIdx.TORSO]

        // Posture validation
        val postureValidation = validatePosture(torso)

        if (!postureValidation.isValid) {
            isInvalidCycle = true
            lastValidationResult = postureValidation
        }

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
            isInvalidCycle = false
            lastValidationResult = ValidationResult(true, emptyList())
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
            isInvalidCycle = true
            lastValidationResult = ValidationResult(false, listOf(ValidationMessage.NOT_REACH_DOWN))
            isGoingDown = false
        }

        // Go Up but Go Down again before UP
        if (isGoingUp && newIdx < maxStateIdx-2) {
            isInvalidCycle = true
            lastValidationResult = ValidationResult(false, listOf(ValidationMessage.NOT_REACH_UP))
            isGoingUp = false
        }

        // Count repetition when back to UP
        if (newState == MovementState.UP && newIdx > oldIdx) {
            when {
                !isInvalidCycle && startedFromUp && reachedDown -> {
                    count++
                    lastValidationResult = ValidationResult(true, emptyList())
                }

                !startedFromUp -> {
                    lastValidationResult = ValidationResult(false, listOf(ValidationMessage.NOT_START_FROM_UP))
                }
            }

            // reset cycle
            isInvalidCycle = false
            reachedDown = false
            startedFromUp = false
            isGoingDown = false
            isGoingUp = false
            minStateIdx = Int.MAX_VALUE
            maxStateIdx = -1
        }

        currentState = newState
        return CounterResult(count, currentState, lastValidationResult)
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
        return if (kotlin.math.abs(torso) > TORSO_MAX_DEVIATION) {
            ValidationResult(false, listOf(ValidationMessage.TORSO_TOO_TILTED))
        } else {
            ValidationResult(true, emptyList())
        }
    }
}

fun createDefaultRepetitionEngine(): RepetitionCounterEngine {
    return RepetitionCounterEngine(
        mapOf(
            "Push-Up" to PushUpCounter(),
            "Sit-Up" to SitUpCounter(),
            "Pull-Up" to PullUpCounter(),
            "Lunges" to LungesCounter()
        )
    )
}