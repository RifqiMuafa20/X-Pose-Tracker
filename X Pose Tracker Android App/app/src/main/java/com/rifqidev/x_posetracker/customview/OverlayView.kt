package com.rifqidev.x_posetracker.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import com.rifqidev.x_posetracker.R
import com.rifqidev.x_posetracker.utils.Landmark3D
import com.rifqidev.x_posetracker.utils.calculateAngle
import kotlin.math.max
import kotlin.math.min

class OverlayView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    private var results: PoseLandmarkerResult? = null
    private var pointPaint = Paint()
    private var linePaint = Paint()

    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1

    init {
        initPaints()
    }

    fun clear() {
        results = null
        pointPaint.reset()
        linePaint.reset()
        invalidate()
        initPaints()
    }

    private fun initPaints() {
        linePaint.color =
            ContextCompat.getColor(context!!, R.color.mp_color_primary)
        linePaint.strokeWidth = LANDMARK_STROKE_WIDTH
        linePaint.style = Paint.Style.STROKE

        pointPaint.color = Color.YELLOW
        pointPaint.strokeWidth = LANDMARK_STROKE_WIDTH
        pointPaint.style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        results?.let { poseLandmarkerResult ->
            for(landmark in poseLandmarkerResult.landmarks()) {
                for(normalizedLandmark in landmark) {
                    canvas.drawPoint(
                        normalizedLandmark.x() * imageWidth * scaleFactor,
                        normalizedLandmark.y() * imageHeight * scaleFactor,
                        pointPaint
                    )
                }

                PoseLandmarker.POSE_LANDMARKS.forEach {
                    canvas.drawLine(
                        poseLandmarkerResult.landmarks().get(0).get(it!!.start()).x() * imageWidth * scaleFactor,
                        poseLandmarkerResult.landmarks().get(0).get(it.start()).y() * imageHeight * scaleFactor,
                        poseLandmarkerResult.landmarks().get(0).get(it.end()).x() * imageWidth * scaleFactor,
                        poseLandmarkerResult.landmarks().get(0).get(it.end()).y() * imageHeight * scaleFactor,
                        linePaint)
                }
            }

            val textPaint = Paint().apply {
                color = ContextCompat.getColor(context!!, R.color.red_accent)
                strokeWidth = LANDMARK_STROKE_WIDTH
                textSize = 32f
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
                isAntiAlias = true
            }

            val allLandmarks = poseLandmarkerResult.landmarks()
            if (allLandmarks.isNotEmpty()) {
                val landmarks = allLandmarks.first()

                val get = { index: Int ->
                    val lm = landmarks[index]
                    Landmark3D(lm.x(), lm.y(), lm.z())
                }

                val centerPoints = listOf(
                    12, 14, 24, 26, // kanan
                    11, 13, 23, 25, // kiri
                )

                val angles = listOf(
                    // left Body
                    calculateAngle(get(13), get(11), get(23)), // left_shoulder: elbow - shoulder - hip
                    calculateAngle(get(11), get(13), get(15)), // left_elbow: shoulder - elbow - wrist
                    calculateAngle(get(11), get(23), get(25)), // left_hip: shoulder - hip - knee
                    calculateAngle(get(23), get(25), get(27)), // left_knee: hip - knee - ankle

                    // right Body
                    calculateAngle(get(14), get(12), get(24)), // right_shoulder: elbow - shoulder - hip
                    calculateAngle(get(12), get(14), get(16)), // right_elbow: shoulder - elbow - wrist
                    calculateAngle(get(12), get(24), get(26)), // right_hip: shoulder - hip - knee
                    calculateAngle(get(24), get(26), get(28)), // right_knee: hip - knee - ankle
                )

                for ((i, angle) in angles.withIndex()) {
                    val landmark = landmarks[centerPoints[i]]
                    canvas.drawText(
                        "${angle.toInt()}°",
                        landmark.x() * imageWidth * scaleFactor + 10,
                        landmark.y() * imageHeight * scaleFactor - 10,
                        textPaint
                    )
                }
            }
        }
    }

    fun setResults(
        poseLandmarkerResults: PoseLandmarkerResult,
        imageHeight: Int,
        imageWidth: Int,
        runningMode: RunningMode = RunningMode.IMAGE
    ) {
        results = poseLandmarkerResults

        this.imageHeight = imageHeight
        this.imageWidth = imageWidth

        scaleFactor = when (runningMode) {
            RunningMode.IMAGE,
            RunningMode.VIDEO -> {
                min(width * 1f / imageWidth, height * 1f / imageHeight)
            }
            RunningMode.LIVE_STREAM -> {
                max(width * 1f / imageWidth, height * 1f / imageHeight)
            }
        }
        invalidate()
    }

    companion object {
        private const val LANDMARK_STROKE_WIDTH = 12F
    }
}