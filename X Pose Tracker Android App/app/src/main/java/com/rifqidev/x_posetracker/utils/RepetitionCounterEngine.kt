package com.rifqidev.x_posetracker.utils

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.rifqidev.x_posetracker.utils.repetition_counter.LungesCounter
import com.rifqidev.x_posetracker.utils.repetition_counter.PullUpCounter
import com.rifqidev.x_posetracker.utils.repetition_counter.PushUpCounter
import com.rifqidev.x_posetracker.utils.repetition_counter.SitUpCounter

object AngleIdx {
    const val LEFT_ELBOW = 1
    const val RIGHT_ELBOW = 7
    const val LEFT_HIP = 3
    const val RIGHT_HIP = 9
    const val LEFT_KNEE = 4
    const val RIGHT_KNEE = 10
    const val TORSO = 12
}

object LandmarkIdx {
    const val NOSE = 0
    const val LEFT_MOUTH = 9
    const val RIGHT_MOUTH = 10
    const val LEFT_SHOULDER = 11
    const val RIGHT_SHOULDER = 12
    const val LEFT_ELBOW = 13
    const val RIGHT_ELBOW = 14
    const val LEFT_THUMB = 21
    const val RIGHT_THUMB = 22
    const val LEFT_HIP = 23
    const val RIGHT_HIP = 24
    const val LEFT_KNEE = 25
    const val RIGHT_KNEE = 26
    const val LEFT_ANKLE = 27
    const val RIGHT_ANKLE = 28
}

enum class MovementState {
    DOWN,
    NEAR_DOWN,
    CENTRE,
    NEAR_UP,
    UP
}

enum class FrontLegState {
    NONE,
    LEFT_FRONT,
    RIGHT_FRONT
}

data class RepUiState(
    val current: Int,
    val pushUp: Int,
    val sitUp: Int,
    val pullUp: Int,
    val lunges: Int
)

data class ValidationResult(
    val message: List<ValidationMessage>
)

data class ValidationUiState(
    val isValid: Boolean,
    val message: List<ValidationMessage>
)

enum class MessageType {
    ERROR,
    WARNING
}

sealed class UiEvent {
    data class SpeakText(val text: String) : UiEvent()
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
    NOT_START_FROM_DOWN,
    BODY_NOT_HIGH_ENOUGH,
    LEG_NOT_SWITCHED,
    HIP_NOT_FOLLOWING,
    BODY_SWAYING,
    ELBOW_NOT_TOUCH_KNEE,
    ELBOW_NOT_TOUCH_FLOOR
}

interface IRepetitionCounter {
    val count: Int
    val currentState: MovementState
    val lastValidationResult: ValidationResult
    val lastWarningResult: ValidationResult
    val isValid: Boolean

    fun resetAll()
    fun resetStateOnly()
    fun update(angles13: FloatArray, landmarks: List<NormalizedLandmark>)
}

fun updateConsecutiveCounter(
    condition: Boolean,
    currentCounter: Int
): Int {
    return if (condition) {
        currentCounter + 1
    } else {
        0
    }
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

    fun update(label: String, angles13: FloatArray, landmarks: List<NormalizedLandmark>) {
        if (label != activeLabel) {
            counters[label]?.resetStateOnly()
            activeLabel = label
        }
        return counters[label]?.update(angles13, landmarks) ?: Unit
    }

    fun getCount(label: String): Int = counters[label]?.count ?: 0

    fun getIsValid(label: String): Boolean = counters[label]?.isValid ?: true

    fun getLastValidationMessages(label: String): List<ValidationMessage> =
        counters[label]?.lastValidationResult?.message ?: emptyList()

    fun getLastWarningMessages(label: String): List<ValidationMessage> =
        counters[label]?.lastWarningResult?.message ?: emptyList()
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