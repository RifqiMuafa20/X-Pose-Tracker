package com.rifqidev.x_posetracker.ui.camera

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.rifqidev.x_posetracker.data.AktivitasLatihan
import com.rifqidev.x_posetracker.data.RepUiState
import com.rifqidev.x_posetracker.data.UiEvent
import com.rifqidev.x_posetracker.data.UserProfileEntity
import com.rifqidev.x_posetracker.data.ValidationUiState
import com.rifqidev.x_posetracker.repository.AppRepository
import com.rifqidev.x_posetracker.utils.AngleFallbackState
import com.rifqidev.x_posetracker.utils.PoseClassificationHelper
import com.rifqidev.x_posetracker.utils.PoseLandmarkerHelper
import com.rifqidev.x_posetracker.utils.calculateTotalCalories
import com.rifqidev.x_posetracker.utils.createDefaultRepetitionEngine
import com.rifqidev.x_posetracker.utils.estimateDuration
import com.rifqidev.x_posetracker.utils.extractAngles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CameraViewModel(application: Application) : AndroidViewModel(application) {

    // PoseLandmark config
    private var _delegate: Int = PoseLandmarkerHelper.DELEGATE_GPU
    private var _minPoseDetectionConfidence: Float =
        PoseLandmarkerHelper.DEFAULT_POSE_DETECTION_CONFIDENCE
    private var _minPoseTrackingConfidence: Float =
        PoseLandmarkerHelper.DEFAULT_POSE_TRACKING_CONFIDENCE
    private var _minPosePresenceConfidence: Float =
        PoseLandmarkerHelper.DEFAULT_POSE_PRESENCE_CONFIDENCE

    val currentDelegate: Int get() = _delegate
    val currentMinPoseDetectionConfidence: Float get() = _minPoseDetectionConfidence
    val currentMinPoseTrackingConfidence: Float get() = _minPoseTrackingConfidence
    val currentMinPosePresenceConfidence: Float get() = _minPosePresenceConfidence

    fun setDelegate(delegate: Int) { _delegate = delegate }
    fun setMinPoseDetectionConfidence(c: Float) { _minPoseDetectionConfidence = c }
    fun setMinPoseTrackingConfidence(c: Float) { _minPoseTrackingConfidence = c }
    fun setMinPosePresenceConfidence(c: Float) { _minPosePresenceConfidence = c }

    // Repository
    private val repository = AppRepository(application)
    fun getUserProfile(): LiveData<UserProfileEntity?> = repository.getUserProfile()

    // Pose processing state
    private val angleState = AngleFallbackState()
    private val repEngine = createDefaultRepetitionEngine()
    private lateinit var poseClassifier: PoseClassificationHelper

    private val windowPose = Array(30) { FloatArray(13) }
    private var windowSize = 0
    private var windowIdx = 0
    private var clsTick = 0
    private val CLS_EVERY_N_FRAMES = 10

    private var lastPrediction: String? = null
    private var lastCountMap = mutableMapOf<String, Int>()
    private var lastMessage: String? = ""

    // Exposed UI LiveData

    private val _prediction = MutableLiveData<String>()
    val prediction: LiveData<String> = _prediction

    private val _repState = MutableLiveData<RepUiState>()
    val repState: LiveData<RepUiState> = _repState

    private val _validationState = MutableLiveData<ValidationUiState>()
    val validationState: LiveData<ValidationUiState> = _validationState

    private val _uiEvent = MutableLiveData<UiEvent?>()
    val uiEvent: LiveData<UiEvent?> = _uiEvent

    // Initializer

    fun initClassifier(classifier: PoseClassificationHelper) {
        poseClassifier = classifier
    }

    // Pose frame processing — runs on Dispatchers.Default

    fun processPoseFrame(
        poseLandmarks: List<NormalizedLandmark>,
        activityType: String?,
        autoLabel: String
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            // Extract angles
            val angles13 = extractAngles(landmarks = poseLandmarks, state = angleState)

            // Update sliding window
            val dst = windowPose[windowIdx]
            for (j in 0 until 13) dst[j] = angles13[j]
            windowIdx = (windowIdx + 1) % 30
            if (windowSize < 30) windowSize++

            // Classification or fixed label
            val isAuto = activityType == autoLabel
            val currentPrediction: String = if (isAuto && windowSize == 30) {
                clsTick++
                if (clsTick % CLS_EVERY_N_FRAMES == 0) {
                    poseClassifier.runModel(windowPose, windowIdx)
                } else {
                    _prediction.value ?: autoLabel
                }
            } else {
                activityType ?: "Unknown"
            }

            // Speak prediction change
            if (currentPrediction != lastPrediction) {
                emitEvent(UiEvent.Speak(currentPrediction))
                lastPrediction = currentPrediction
            }

            // Rep engine update
            repEngine.update(currentPrediction, angles13)
            val count = repEngine.getCount(currentPrediction)

            // Speak rep count on increment
            if (count > (lastCountMap[currentPrediction] ?: 0)) {
                emitEvent(UiEvent.Speak("$count"))
                lastCountMap[currentPrediction] = count
            }

            // Validation
            val isValid: Boolean
            val message: String
            if (currentPrediction == autoLabel || currentPrediction == "Unknown") {
                isValid = true
                message = ""
            } else {
                isValid = repEngine.getLastValidation(currentPrediction)?.isValid == true
                message = repEngine.getLastValidation(currentPrediction)?.message.orEmpty()
            }

            if (!isValid && message != lastMessage) {
                emitEvent(UiEvent.PlayErrorSound)
                emitEvent(UiEvent.Speak(message))
            }
            lastMessage = message

            // Post all UI state to main thread
            _prediction.postValue(currentPrediction)
            _repState.postValue(
                RepUiState(
                    current = count,
                    pushUp = repEngine.getCount("Push-Up"),
                    sitUp = repEngine.getCount("Sit-Up"),
                    pullUp = repEngine.getCount("Pull-Up"),
                    lunges = repEngine.getCount("Lunges")
                )
            )
            _validationState.postValue(ValidationUiState(isValid = isValid, message = message))
        }
    }

    // Session helpers

    fun resetSession() {
        angleState.reset()
        repEngine.resetSession()
        windowSize = 0
        windowIdx = 0
        clsTick = 0
        lastPrediction = null
        lastCountMap.clear()
        lastMessage = ""
    }

    private fun buildAktivitasList(): List<AktivitasLatihan> = listOf(
        "Push-Up", "Sit-Up", "Pull-Up", "Lunges"
    ).map { name ->
        AktivitasLatihan(
            name,
            durasiMenit = estimateDuration(name, repEngine.getCount(name)),
            repetisi = repEngine.getCount(name)
        )
    }

    fun calculateCalories(beratBadan: Float?): Float =
        calculateTotalCalories(beratBadan, buildAktivitasList())

    fun getRepCount(name: String): Int = repEngine.getCount(name)

    // Internal helpers

    private fun emitEvent(event: UiEvent) {
        _uiEvent.postValue(event)
    }

    fun onEventConsumed() {
        _uiEvent.postValue(null)
    }
}

