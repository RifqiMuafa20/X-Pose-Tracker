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
import com.rifqidev.x_posetracker.utils.AngleFallbackState
import com.rifqidev.x_posetracker.utils.extractAngles
import kotlin.math.max
import kotlin.math.min

class OverlayView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var results: PoseLandmarkerResult? = null

    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.YELLOW
        strokeWidth = LANDMARK_STROKE_WIDTH
        style = Paint.Style.FILL
    }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context!!, R.color.mp_color_primary)
        strokeWidth = LANDMARK_STROKE_WIDTH
        style = Paint.Style.STROKE
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context!!, R.color.red_accent)
        strokeWidth = LANDMARK_STROKE_WIDTH
        textSize = 32f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private var scaleFactor = 1f
    private var imageWidth = 1
    private var imageHeight = 1
    private var offsetX = 0f
    private var offsetY = 0f

    private val angleState = AngleFallbackState()

    private val showAngleIndices = intArrayOf(0, 1, 3, 4, 6, 7, 9, 10, 12)
    private val showAnchorLandmarks = intArrayOf(11, 13, 23, 25, 12, 14, 24, 26, 0)

    fun clear() {
        results = null
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val res = results ?: return
        val first = res.landmarks().firstOrNull() ?: return

        fun tx(xNorm: Float) = xNorm * imageWidth * scaleFactor + offsetX
        fun ty(yNorm: Float) = yNorm * imageHeight * scaleFactor + offsetY

        for (landmarkList in res.landmarks()) {
            for (nl in landmarkList) {
                canvas.drawPoint(tx(nl.x()), ty(nl.y()), pointPaint)
            }
        }

        PoseLandmarker.POSE_LANDMARKS.forEach { c ->
            if (c == null) return@forEach
            val s = first[c.start()]
            val e = first[c.end()]
            canvas.drawLine(
                tx(s.x()), ty(s.y()),
                tx(e.x()), ty(e.y()),
                linePaint
            )
        }

        val angles13 = extractAngles(first, angleState)

        fun midX(a: Int, b: Int) = (first[a].x() + first[b].x()) / 2f
        fun midY(a: Int, b: Int) = (first[a].y() + first[b].y()) / 2f

        val torsoTextX = tx((midX(11, 12) + midX(23, 24)) / 2f)
        val torsoTextY = ty((midY(11, 12) + midY(23, 24)) / 2f)

        for (i in showAngleIndices.indices) {
            val angleIdx = showAngleIndices[i]
            val angle = angles13.getOrNull(angleIdx) ?: continue

            if (angleIdx == 12) {
                canvas.drawText("${angle.toInt()}°", torsoTextX, torsoTextY, textPaint)
            } else {
                val lmIdx = showAnchorLandmarks[i]
                val lm = first[lmIdx]
                canvas.drawText(
                    "${angle.toInt()}°",
                    tx(lm.x()) + 10f,
                    ty(lm.y()) - 10f,
                    textPaint
                )
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

        if (width == 0 || height == 0) {
            post { setResults(poseLandmarkerResults, imageHeight, imageWidth, runningMode) }
            return
        }

        val viewW = width.toFloat()
        val viewH = height.toFloat()

        val scaleX = viewW / imageWidth
        val scaleY = viewH / imageHeight

        scaleFactor =
            if (runningMode == RunningMode.LIVE_STREAM) max(scaleX, scaleY) else min(scaleX, scaleY)

        val scaledW = imageWidth * scaleFactor
        val scaledH = imageHeight * scaleFactor
        offsetX = (viewW - scaledW) / 2f
        offsetY = (viewH - scaledH) / 2f

        invalidate()
    }

    companion object {
        private const val LANDMARK_STROKE_WIDTH = 12F
    }
}