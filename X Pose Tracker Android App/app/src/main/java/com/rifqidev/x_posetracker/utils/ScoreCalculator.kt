package com.rifqidev.x_posetracker.utils

import com.rifqidev.x_posetracker.data.ScorePoint
import com.rifqidev.x_posetracker.data.ScorePointDouble
import com.rifqidev.x_posetracker.data.TniScorePoint
import com.rifqidev.x_posetracker.data.TniScorePointDouble

object ScoreCalculator {

    //POLRI

    fun getScore(
        value: Int,
        table: List<ScorePoint>
    ): Int {

        if (table.isEmpty())
            return 0

        if (value < table.first().distance)
            return 0

        if (value >= table.last().distance)
            return table.last().score

        var left = 0
        var right = table.lastIndex

        while (left <= right) {

            val mid = (left + right) / 2

            when {

                value == table[mid].distance -> {
                    return table[mid].score
                }

                value > table[mid].distance -> {
                    left = mid + 1
                }

                else -> {
                    right = mid - 1
                }
            }
        }

        return table[right].score
    }

    fun getScoreDouble(
        value: Double,
        table: List<ScorePointDouble>
    ): Int {

        if (table.isEmpty()) return 0

        if (value <= table.first().time) {
            return table.first().score
        }

        if (value >= table.last().time) {
            return table.last().score
        }

        var left = 0
        var right = table.lastIndex

        while (left <= right) {
            val mid = (left + right) / 2

            when {
                value == table[mid].time -> {
                    return table[mid].score
                }

                value > table[mid].time -> {
                    left = mid + 1
                }
                else -> {
                    right = mid - 1
                }
            }
        }

        return table[left].score
    }

    // TNI

    fun getTniScore(
        value: Int,
        age: Int,
        table: List<TniScorePoint>
    ): Int {
        if (table.isEmpty()) return 0

        val ageGroupIdx = getAgeGroupIndex(age)

        if (value >= table.first().value) {
            return table.first().scores[ageGroupIdx]
        }

        if (value <= table.last().value) {
            return table.last().scores[ageGroupIdx]
        }

        var left = 0
        var right = table.lastIndex

        while (left <= right) {
            val mid = (left + right) / 2
            val midDistance = table[mid].value

            when {
                value == midDistance -> {
                    return table[mid].scores[ageGroupIdx]
                }
                value > midDistance -> {
                    right = mid - 1
                }
                else -> {
                    left = mid + 1
                }
            }
        }

        return table[left].scores[ageGroupIdx]
    }

    fun getTniScoreDouble(
        value: Double,
        age: Int,
        table: List<TniScorePointDouble>
    ): Int {

        if (table.isEmpty()) return 0

        val ageGroupIdx = getAgeGroupIndex(age)

        if (value <= table.first().time) {
            return table.first().scores[ageGroupIdx]
        }

        if (value >= table.last().time) {
            return table.last().scores[ageGroupIdx]
        }

        var left = 0
        var right = table.lastIndex

        while (left <= right) {

            val mid = (left + right) / 2
            val midValue = table[mid].time

            when {

                value == midValue -> {
                    return table[mid].scores[ageGroupIdx]
                }

                value > midValue -> {
                    left = mid + 1
                }

                else -> {
                    right = mid - 1
                }
            }
        }

        return table[right].scores[ageGroupIdx]
    }

    // Helper

    private fun getAgeGroupIndex(age: Int): Int {
        return when (age) {
            in 18..21 -> 0
            in 22..25 -> 1
            in 26..29 -> 2
            in 30..33 -> 3
            in 34..37 -> 4
            in 38..41 -> 5
            in 42..45 -> 6
            in 46..49 -> 7
            in 50..53 -> 8
            in 54..57 -> 9
            else -> if (age < 18) 0 else 9
        }
    }
}