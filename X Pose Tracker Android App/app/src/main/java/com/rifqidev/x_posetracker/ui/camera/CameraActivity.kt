package com.rifqidev.x_posetracker.ui.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.dicoding.picodiploma.mynoteapps.helper.ViewModelFactory
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.rifqidev.x_posetracker.R
import com.rifqidev.x_posetracker.data.AktivitasLatihan
import com.rifqidev.x_posetracker.data.UserProfileEntity
import com.rifqidev.x_posetracker.databinding.ActivityCameraBinding
import com.rifqidev.x_posetracker.ui.result.ResultActivity
import com.rifqidev.x_posetracker.utils.DateHelper
import com.rifqidev.x_posetracker.utils.DateHelper.formatTime
import com.rifqidev.x_posetracker.utils.PoseClassificationHelper
import com.rifqidev.x_posetracker.utils.PoseLandmarkerHelper
import com.rifqidev.x_posetracker.utils.RepetitionCounter
import com.rifqidev.x_posetracker.utils.estimasiDurasi
import com.rifqidev.x_posetracker.utils.hitungTotalKalori
import com.rifqidev.x_posetracker.utils.processPose
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class CameraActivity : AppCompatActivity(), PoseLandmarkerHelper.LandmarkerListener {
    private lateinit var binding: ActivityCameraBinding
    private lateinit var viewModel: CameraViewModel

    private lateinit var poseLandmarkerHelper: PoseLandmarkerHelper
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraFacing = CameraSelector.LENS_FACING_BACK

    private var activityType: String? = null
    private var recordType: Int = 0
    private var memberId: String = ""
    private var timer: CountDownTimer? = null
    private var start: Boolean = false
    private var userProfile: UserProfileEntity? = null

    private lateinit var poseClassifier: PoseClassificationHelper
    private val slidingWindow = mutableListOf<List<Float>>()

    private lateinit var backgroundExecutor: ExecutorService

    private val repetitionCounters = mapOf(
        "Push-Up" to RepetitionCounter("Push-Up", thresholdDown = 70f, thresholdUp = 160f),
        "Sit-Up" to RepetitionCounter("Sit-Up", thresholdDown = 60f, thresholdUp = 130f),
        "Pull-Up" to RepetitionCounter("Pull-Up", thresholdDown = 60f, thresholdUp = 140f),
        "Lunges" to RepetitionCounter("Lunges", thresholdDown = 70f, thresholdUp = 160f)
    )

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Permission request granted", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Permission request denied", Toast.LENGTH_LONG).show()
            }
        }

    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            this,
            REQUIRED_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val factory = ViewModelFactory.getInstance(this.application)
        viewModel =
            ViewModelProvider(this, factory)[CameraViewModel::class.java]

        viewModel.getUserProfile().observe(this) { user ->
            if (user != null) {
                userProfile = user
            }
        }

        poseClassifier = PoseClassificationHelper(this)

        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSION)
        }

        activityType = intent.getStringExtra("type") ?: "Unknown"
        memberId = intent.getStringExtra("member_id") ?: ""
        recordType = intent.getIntExtra("record_type", 0)
        val durationInSeconds = intent.getLongExtra("duration", 0L)

        // Initialize our background executor
        backgroundExecutor = Executors.newSingleThreadExecutor()

        binding.duration.text = formatTime(durationInSeconds)

        binding.startCamera.setOnClickListener {
            binding.startCamera.visibility = View.GONE
            binding.startText.visibility = View.GONE
            start = true

            showCountdown {
                if (durationInSeconds > 0) {
                    startCountdown(durationInSeconds)
                }
                // Mulai pendeteksian pose setelah countdown
                setUpCamera()
            }
        }

        binding.switchCamera.setOnClickListener {
            cameraFacing = if (cameraFacing == CameraSelector.LENS_FACING_BACK)
                CameraSelector.LENS_FACING_FRONT
            else
                CameraSelector.LENS_FACING_BACK

            setUpCamera()
        }

        binding.type.text = activityType

        binding.viewFinder.post {
            setUpCamera()
        }

        // Create the PoseLandmarkerHelper that will handle the inference
        backgroundExecutor.execute {
            poseLandmarkerHelper = PoseLandmarkerHelper(
                context = this,
                runningMode = RunningMode.LIVE_STREAM,
                minPoseDetectionConfidence = viewModel.currentMinPoseDetectionConfidence,
                minPoseTrackingConfidence = viewModel.currentMinPoseTrackingConfidence,
                minPosePresenceConfidence = viewModel.currentMinPosePresenceConfidence,
                currentDelegate = viewModel.currentDelegate,
                poseLandmarkerHelperListener = this@CameraActivity
            )
        }
    }

    // Initialize CameraX, and prepare to bind the camera use cases
    private fun setUpCamera() {
        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(
            {
                // CameraProvider
                cameraProvider = cameraProviderFuture.get()

                // Build and bind the camera use cases
                bindCameraUseCases()
            }, ContextCompat.getMainExecutor(this)
        )
    }

    // Declare and bind preview, capture and analysis use cases
    @SuppressLint("UnsafeOptInUsageError")
    private fun bindCameraUseCases() {

        // CameraProvider
        val cameraProvider = cameraProvider
            ?: throw IllegalStateException("Camera initialization failed.")

        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(cameraFacing).build()

        // Preview. Only using the 4:3 ratio because this is the closest to our models
        preview = Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(binding.viewFinder.display.rotation)
            .build()

        // ImageAnalysis. Using RGBA 8888 to match how our models work
        imageAnalyzer =
            ImageAnalysis.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(binding.viewFinder.display.rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                // The analyzer can then be assigned to the instance
                .also {
                    it.setAnalyzer(backgroundExecutor) { image ->
                        detectPose(image)
                    }
                }

        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll()

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageAnalyzer
            )

            // Attach the viewfinder's surface provider to preview use case
            preview?.setSurfaceProvider(binding.viewFinder.surfaceProvider)
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    private fun detectPose(imageProxy: ImageProxy) {
        if (this::poseLandmarkerHelper.isInitialized) {
            poseLandmarkerHelper.detectLiveStream(
                imageProxy = imageProxy,
                isFrontCamera = cameraFacing == CameraSelector.LENS_FACING_FRONT
            )
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        imageAnalyzer?.targetRotation =
            binding.viewFinder.display.rotation
    }

    override fun onError(error: String, errorCode: Int) {
        runOnUiThread {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResults(
        resultBundle: PoseLandmarkerHelper.ResultBundle
    ) {
        runOnUiThread {
            binding.overlay.setResults(
                resultBundle.results.first(),
                resultBundle.inputImageHeight,
                resultBundle.inputImageWidth,
                RunningMode.LIVE_STREAM
            )

            // binding.inferenceTime.text = String.format("%d ms", resultBundle.inferenceTime)

            // Force a redraw of overlay
            binding.overlay.invalidate()

            var prediction = ""

            val result = resultBundle.results.firstOrNull() ?: return@runOnUiThread
            val inputForModel = processPose(result.landmarks().firstOrNull() ?: return@runOnUiThread)

            if(activityType == resources.getStringArray(R.array.category_menu)[0].toString()) {
                if (inputForModel.isNotEmpty()) {
                    slidingWindow.add(inputForModel)

                    if (slidingWindow.size > 30) {
                        slidingWindow.removeAt(0)
                    }

                    if (slidingWindow.size == 30) {
                        prediction = poseClassifier.runModel(slidingWindow.toList())
                        binding.type.text = prediction
                    }
                }
            } else {
                prediction = activityType.toString()
            }

            if (start) {
                val mainAngle = when (prediction) {
                    "Push-Up" -> inputForModel[7]
                    "Sit-Up" -> inputForModel[9]
                    "Pull-Up" -> inputForModel[7]
                    "Lunges" -> inputForModel[11]
                    else -> null
                }

                mainAngle?.let {
                    repetitionCounters[prediction]?.update(it)
                }

                binding.repetition.text = repetitionCounters[prediction]?.count.toString()

                binding.pushUpRep.text = repetitionCounters["Push-Up"]?.count.toString()
                binding.sitUpRep.text = repetitionCounters["Sit-Up"]?.count.toString()
                binding.pullUpRep.text = repetitionCounters["Pull-Up"]?.count.toString()
                binding.lungesRep.text = repetitionCounters["Lunges"]?.count.toString()
            }
        }
    }

    public override fun onResume() {
        super.onResume()
        hideSystemUI()
        setUpCamera()

        backgroundExecutor.execute {
            if (this::poseLandmarkerHelper.isInitialized) {
                if (poseLandmarkerHelper.isClose()) {
                    poseLandmarkerHelper.setupPoseLandmarker()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (this::poseLandmarkerHelper.isInitialized) {
            viewModel.setMinPoseDetectionConfidence(poseLandmarkerHelper.minPoseDetectionConfidence)
            viewModel.setMinPoseTrackingConfidence(poseLandmarkerHelper.minPoseTrackingConfidence)
            viewModel.setMinPosePresenceConfidence(poseLandmarkerHelper.minPosePresenceConfidence)
            viewModel.setDelegate(poseLandmarkerHelper.currentDelegate)

            // Close the PoseLandmarkerHelper and release resources
            backgroundExecutor.execute { poseLandmarkerHelper.clearPoseLandmarker() }
        }
    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()

        backgroundExecutor.shutdown()
        backgroundExecutor.awaitTermination(
            Long.MAX_VALUE, TimeUnit.NANOSECONDS
        )

        poseClassifier.close()
    }

    private fun hideSystemUI() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun startCountdown(durationInSeconds: Long) {
        timer = object : CountDownTimer(durationInSeconds * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                binding.duration.text = formatTime(secondsRemaining)
            }

            override fun onFinish() {
                binding.duration.text = "00:00"
                val intent = Intent(
                    this@CameraActivity,
                    ResultActivity::class.java
                )

                val aktivitasSesi = listOf(
                    AktivitasLatihan("Push-Up", durasiMenit = estimasiDurasi("Push-Up", repetitionCounters["Push-Up"]?.count ?: 0), repetisi = repetitionCounters["Push-Up"]?.count ?: 0),
                    AktivitasLatihan("Sit-Up", durasiMenit = estimasiDurasi("Sit-Up", repetitionCounters["Sit-Up"]?.count ?: 0), repetisi = repetitionCounters["Sit-Up"]?.count ?: 0),
                    AktivitasLatihan("Pull-Up", durasiMenit = estimasiDurasi("Pull-Up", repetitionCounters["Pull-Up"]?.count ?: 0), repetisi = repetitionCounters["Pull-Up"]?.count ?: 0),
                    AktivitasLatihan("Lunges", durasiMenit = estimasiDurasi("Lunges", repetitionCounters["Lunges"]?.count ?: 0), repetisi = repetitionCounters["Lunges"]?.count ?: 0),
                )

                val userId = userProfile?.idUser
                val beratBadan = userProfile?.userWeight?.toFloat()
                val totalKalori = hitungTotalKalori(beratBadan, aktivitasSesi)

                intent.putExtra("user_id", userId)
                intent.putExtra("date", DateHelper.getCurrentDate())
                intent.putExtra("time", DateHelper.getCurrentTime())
                intent.putExtra("duration", durationInSeconds.toInt())
                intent.putExtra("calorie", totalKalori.toInt())
                intent.putExtra("push_up", repetitionCounters["Push-Up"]?.count ?: 0)
                intent.putExtra("sit_up", repetitionCounters["Sit-Up"]?.count ?: 0)
                intent.putExtra("pull_up", repetitionCounters["Pull-Up"]?.count ?: 0)
                intent.putExtra("lunges", repetitionCounters["Lunges"]?.count ?: 0)
                intent.putExtra("record_type", recordType)
                intent.putExtra("member_id", memberId)

                startActivity(intent)
                finish()
            }
        }
        timer?.start()
    }

    private fun showCountdown(onFinish: () -> Unit) {
        val countdownValues = listOf("3", "2", "1")
        var index = 0

        val countdownText = binding.countdownText
        countdownText.visibility = View.VISIBLE

        fun showNext() {
            if (index >= countdownValues.size) {
                countdownText.visibility = View.GONE
                onFinish()
                return
            }

            countdownText.text = countdownValues[index]
            countdownText.alpha = 1f
            countdownText.scaleX = 1f
            countdownText.scaleY = 1f

            countdownText.animate()
                .alpha(0f)
                .scaleX(2f)
                .scaleY(2f)
                .setDuration(800)
                .withEndAction {
                    index++
                    showNext()
                }
                .start()
        }

        showNext()
    }

    private fun showConfirmationDialog(message: Int, type: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(message)
        builder.setPositiveButton(R.string.yes) { _, _ ->
            if (type == 0) {
                finish()
            }
        }
        builder.setNegativeButton(R.string.no) { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        showConfirmationDialog(R.string.cancel_activity_confirmation, 0)
    }

    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
        private const val TAG = "Pose Landmarker"
    }
}