package com.rifqidev.x_posetracker.utils

class PredictionCountPerClass {
    private val countMap = mutableMapOf<String, Int>()

    fun addPrediction(prediction: String) {
        countMap[prediction] = (countMap[prediction] ?: 0) + 1
    }

    fun getCount(prediction: String): Int {
        return countMap[prediction] ?: 0
    }

    fun getAllCounts(): Map<String, Int> {
        return countMap.toMap()
    }

    fun reset() {
        countMap.clear()
    }
}
