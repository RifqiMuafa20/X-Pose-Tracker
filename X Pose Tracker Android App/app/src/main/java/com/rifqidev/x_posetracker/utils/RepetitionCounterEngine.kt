package com.rifqidev.x_posetracker.utils

import kotlin.math.abs

object AngleIdx {
    const val LEFT_ELBOW = 1
    const val RIGHT_ELBOW = 7

    const val LEFT_HIP = 3
    const val RIGHT_HIP = 9

    const val LEFT_KNEE = 4
    const val RIGHT_KNEE = 10

    const val TORSO = 12
}

private enum class Side { LEFT, RIGHT }

interface IRepetitionCounter {
    val count: Int
    fun resetAll()
    fun resetStateOnly()
    fun update(angles13: FloatArray)
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

    fun update(label: String, angles13: FloatArray) {
        if (label != activeLabel) {
            counters[label]?.resetStateOnly()
            activeLabel = label
        }
        counters[label]?.update(angles13)
    }

    fun getCount(label: String): Int = counters[label]?.count ?: 0
}

class PushUpCounter : IRepetitionCounter {
    override var count: Int = 0
        private set

    private var isDown = false

    private val ELBOW_UP = 160f
    private val ELBOW_DOWN = 70f
    private val HIP_VALID = 160f
    private val KNEE_VALID = 160f

    override fun resetAll() {
        count = 0
        isDown = false
    }

    override fun resetStateOnly() {
        isDown = false
    }

    override fun update(angles13: FloatArray) {
        val eL = angles13[AngleIdx.LEFT_ELBOW]
        val eR = angles13[AngleIdx.RIGHT_ELBOW]
        val hL = angles13[AngleIdx.LEFT_HIP]
        val hR = angles13[AngleIdx.RIGHT_HIP]
        val kL = angles13[AngleIdx.LEFT_KNEE]
        val kR = angles13[AngleIdx.RIGHT_KNEE]

        val valid = (hL >= HIP_VALID && hR >= HIP_VALID && kL >= KNEE_VALID && kR >= KNEE_VALID)

        if (!isDown) {
            if (valid && eL <= ELBOW_DOWN && eR <= ELBOW_DOWN) isDown = true
        } else {
            if (valid && eL >= ELBOW_UP && eR >= ELBOW_UP) {
                count++
                isDown = false
            }
        }
    }
}

class SitUpCounter : IRepetitionCounter {
    override var count: Int = 0
        private set

    private var isUp = false

    private val HIP_UP = 60f
    private val HIP_DOWN = 120f
    private val KNEE_TARGET = 100f
    private val KNEE_TOL = 15f

    override fun resetAll() {
        count = 0
        isUp = false
    }

    override fun resetStateOnly() {
        isUp = false
    }

    override fun update(angles13: FloatArray) {
        val hL = angles13[AngleIdx.LEFT_HIP]
        val hR = angles13[AngleIdx.RIGHT_HIP]
        val kL = angles13[AngleIdx.LEFT_KNEE]
        val kR = angles13[AngleIdx.RIGHT_KNEE]

        val validKnee = (abs(kL - KNEE_TARGET) <= KNEE_TOL && abs(kR - KNEE_TARGET) <= KNEE_TOL)

        if (!isUp) {
            if (validKnee && hL <= HIP_UP && hR <= HIP_UP) isUp = true
        } else {
            if (validKnee && hL >= HIP_DOWN && hR >= HIP_DOWN) {
                count++
                isUp = false
            }
        }
    }
}

class PullUpCounter : IRepetitionCounter {
    override var count: Int = 0
        private set

    private var isUp = false

    private val ELBOW_UP = 50f
    private val ELBOW_DOWN = 160f
    private val KNEE_VALID = 160f

    override fun resetAll() {
        count = 0
        isUp = false
    }

    override fun resetStateOnly() {
        isUp = false
    }

    override fun update(angles13: FloatArray) {
        val eL = angles13[AngleIdx.LEFT_ELBOW]
        val eR = angles13[AngleIdx.RIGHT_ELBOW]
        val kL = angles13[AngleIdx.LEFT_KNEE]
        val kR = angles13[AngleIdx.RIGHT_KNEE]

        val valid = (kL >= KNEE_VALID && kR >= KNEE_VALID)

        if (!isUp) {
            if (valid && eL <= ELBOW_UP && eR <= ELBOW_UP) isUp = true
        } else {
            if (valid && eL >= ELBOW_DOWN && eR >= ELBOW_DOWN) {
                count++
                isUp = false
            }
        }
    }
}

class LungesCounter : IRepetitionCounter {
    override var count: Int = 0
        private set

    private var inDeep = false
    private var expected = Side.LEFT

    private val KNEE_UP = 145f
    private val KNEE_DOWN = 100f
    private val TORSO_MAX = 15f

    override fun resetAll() {
        count = 0
        inDeep = false
        expected = Side.LEFT
    }

    override fun resetStateOnly() {
        inDeep = false
    }

    override fun update(angles13: FloatArray) {
        val kL = angles13[AngleIdx.LEFT_KNEE]
        val kR = angles13[AngleIdx.RIGHT_KNEE]
        val torso = angles13[AngleIdx.TORSO]

        if (abs(torso) > TORSO_MAX) return

        val stand = (kL >= KNEE_UP && kR >= KNEE_UP)

        val frontSide = if (kL < kR) Side.LEFT else Side.RIGHT
        val deep = (kL <= KNEE_DOWN && kR <= KNEE_DOWN)

        if (!inDeep) {
            if (deep && frontSide == expected) inDeep = true
        } else {
            if (stand) {
                count++
                inDeep = false
                expected = if (expected == Side.LEFT) Side.RIGHT else Side.LEFT
            }
        }
    }
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
