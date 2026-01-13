package com.rifqidev.x_posetracker.utils

import android.content.Context
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import kotlin.math.abs

class PoseClassificationHelper(context: Context) {

    private val interpreter: Interpreter

    private val input = Array(1) { Array(30) { FloatArray(13) } }
    private val output = Array(1) { FloatArray(NUM_CLASSES) }

    private val voteWindow = 5
    private val voteBuf = IntArray(voteWindow) { -1 }
    private var voteIdx = 0
    private var voteSize = 0

    private var totalInferenceTimeNs = 0L
    private var inferenceCount = 0

    init {
        val modelBuffer = FileUtil.loadMappedFile(context, "BiLSTM_Model_Classification.tflite")
        val options = Interpreter.Options().apply {
            numThreads = 4
        }
        interpreter = Interpreter(modelBuffer, options)
    }

    fun runModel(ring: Array<FloatArray>, headIdx: Int): String {
        for (t in 0 until SEQ_LEN) {
            val src = ring[(headIdx + t) % SEQ_LEN]
            val dst = input[0][t]

            for (j in 0 until NUM_FEATURES) {
                dst[j] = standardize(src[j], MEAN[j], STD[j])
            }
        }

        val startTime = System.nanoTime()

        interpreter.run(input, output)

        val endTime = System.nanoTime()

        totalInferenceTimeNs += (endTime - startTime)
        inferenceCount++

        val pred = argMax(output[0])
        val voted = updateMajorityVote(pred)

        return categories.getOrElse(voted) { "Unknown" }
    }

    private fun standardize(x: Float, mean: Float, std: Float): Float {
        return if (abs(std) < 1e-6f) 0f else (x - mean) / std
    }

    private fun updateMajorityVote(pred: Int): Int {
        voteBuf[voteIdx] = pred
        voteIdx = (voteIdx + 1) % voteWindow
        if (voteSize < voteWindow) voteSize++

        val counts = IntArray(NUM_CLASSES)
        for (i in 0 until voteSize) {
            val p = voteBuf[i]
            if (p in 0 until NUM_CLASSES) counts[p]++
        }
        return argMax(counts)
    }

    private fun argMax(arr: FloatArray): Int {
        var bestIdx = 0
        var bestVal = arr[0]
        for (i in 1 until arr.size) {
            if (arr[i] > bestVal) {
                bestVal = arr[i]
                bestIdx = i
            }
        }
        return bestIdx
    }

    private fun argMax(arr: IntArray): Int {
        var bestIdx = 0
        var bestVal = arr[0]
        for (i in 1 until arr.size) {
            if (arr[i] > bestVal) {
                bestVal = arr[i]
                bestIdx = i
            }
        }
        return bestIdx
    }

    fun getAverageInferenceTimeMs(): Double {
        if (inferenceCount == 0) return 0.0
        return (totalInferenceTimeNs / inferenceCount) / 1_000_000.0
    }

    fun close() = interpreter.close()

    companion object {
        const val NUM_CLASSES = 4
        const val SEQ_LEN = 30
        const val NUM_FEATURES = 13

        val categories = listOf("Lunges", "Pull-Up", "Push-Up", "Sit-Up")

        // Z-SCORE PARAMETERS (FROM TRAINING)
        val MEAN = floatArrayOf(
            72.36067f, 110.54639f, 148.8717f, 137.80536f, 124.20968f,
            123.16265f, 69.22333f, 114.03867f, 150.23697f, 137.31897f,
            124.48476f, 122.65251f, 50.37258f
        )

        val STD = floatArrayOf(
            55.85047f, 51.28964f, 26.91423f, 49.3622f, 53.51188f,
            27.37082f, 56.11798f, 50.29099f, 25.06907f, 49.27043f,
            52.25864f, 26.43981f, 38.88928f
        )
    }
}