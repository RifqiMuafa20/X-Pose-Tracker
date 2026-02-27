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

        interpreter.run(input, output)

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

    companion object {
        const val NUM_CLASSES = 4
        const val SEQ_LEN = 30
        const val NUM_FEATURES = 13

        val categories = listOf("Lunges", "Pull-Up", "Push-Up", "Sit-Up")

        // Z-SCORE PARAMETERS (FROM TRAINING - UPDATED)
        val MEAN = floatArrayOf(
            72.402084f, 110.663605f, 148.9273f, 137.8151f, 123.97522f,
            122.75812f, 69.49488f, 114.12173f, 150.44919f, 137.25717f,
            124.43726f, 122.49349f, 26.49276f
        )

        val STD = floatArrayOf(
            55.94534f, 51.222f, 26.668055f, 49.45349f, 53.407986f,
            27.073004f, 56.32109f, 50.377304f, 25.035316f, 49.312088f,
            52.23887f, 26.631166f, 32.655975f
        )
    }
}