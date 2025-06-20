package com.rifqidev.x_posetracker.utils

import android.content.Context
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.flex.FlexDelegate
import org.tensorflow.lite.support.common.FileUtil

class PoseClassificationHelper(context: Context) {

    private val interpreter: Interpreter
    private val predictions = mutableListOf<Int>()

    init {
        val modelBuffer = FileUtil.loadMappedFile(context, "bilstm_model.tflite")

        val options = Interpreter.Options().apply {
            addDelegate(FlexDelegate())
            numThreads = 2
        }

        interpreter = Interpreter(modelBuffer, options)
    }

    fun runModel(jointAngles: List<List<Float>>): String {
        require(jointAngles.size == 30) { "Input must be 30 frames long" }
        require(jointAngles.all { it.size == 12 }) { "Each frame must have 12 joint angles" }

        val input = Array(1) { Array(30) { FloatArray(12) } }
        for (i in 0 until 30) {
            for (j in 0 until 12) {
                input[0][i][j] = jointAngles[i][j]
            }
        }

        val output = Array(1) { FloatArray(NUM_CLASSES) }

        interpreter.run(input, output)

        val predictionIndex = output[0].indices.maxByOrNull { output[0][it] } ?: -1
        predictions.add(predictionIndex)

        return categories.getOrElse(predictionIndex) { "Unknown" }
    }

    fun close() {
        interpreter.close()
    }

    companion object {
        const val NUM_CLASSES = 4
        val categories = listOf("Lunges", "Pull-Up", "Push-Up", "Sit-Up")
    }
}
