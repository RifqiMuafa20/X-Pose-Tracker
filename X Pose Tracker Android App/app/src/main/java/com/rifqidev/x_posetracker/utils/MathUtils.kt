package com.rifqidev.x_posetracker.utils

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import kotlin.math.acos
import kotlin.math.sqrt

fun calculateAngle(a: Landmark3D, b: Landmark3D, c: Landmark3D): Float {
    val ab = floatArrayOf(a.x - b.x, a.y - b.y)
    val bc = floatArrayOf(c.x - b.x, c.y - b.y)

    val dot = ab[0] * bc[0] + ab[1] * bc[1]
    val magAb = sqrt(ab[0] * ab[0] + ab[1] * ab[1])
    val magBc = sqrt(bc[0] * bc[0] + bc[1] * bc[1])

    if (magAb == 0f || magBc == 0f) return 0f

    var cosTheta = dot / (magAb * magBc)
    cosTheta = cosTheta.coerceIn(-1f, 1f)

    return Math.toDegrees(acos(cosTheta).toDouble()).toFloat()
}

fun extractAngles(landmarks: List<NormalizedLandmark>): List<Float> {
    val get = { index: Int ->
        val lm = landmarks[index]
        Landmark3D(lm.x(), lm.y(), lm.z())
    }

    return listOf(
        // left Body
        calculateAngle(get(13), get(11), get(23)), // left_shoulder
        calculateAngle(get(11), get(13), get(15)), // left_elbow
        calculateAngle(get(13), get(15), get(19)), // left_wrist
        calculateAngle(get(11), get(23), get(25)), // left_hip
        calculateAngle(get(23), get(25), get(27)), // left_knee
        calculateAngle(get(25), get(27), get(31)), // left_ankle

        // right Body
        calculateAngle(get(14), get(12), get(24)), // right_shoulder
        calculateAngle(get(12), get(14), get(16)), // right_elbow
        calculateAngle(get(14), get(16), get(20)), // right_wrist
        calculateAngle(get(12), get(24), get(26)), // right_hip
        calculateAngle(get(24), get(26), get(28)), // right_knee
        calculateAngle(get(26), get(28), get(32))  // right_ankle
    )
}

fun processPose(landmarks: List<NormalizedLandmark>): List<Float> {
    return extractAngles(landmarks)
}

data class Landmark3D(val x: Float, val y: Float, val z: Float)
