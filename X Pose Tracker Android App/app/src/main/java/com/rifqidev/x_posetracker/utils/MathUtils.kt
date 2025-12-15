package com.rifqidev.x_posetracker.utils

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.rifqidev.x_posetracker.data.AktivitasLatihan
import kotlin.math.acos
import kotlin.math.sqrt

data class Landmark3D(val x: Float, val y: Float, val z: Float)
data class JointDef(val name: String, val a: Int, val b: Int, val c: Int)

private val JOINT_DEFS = listOf(
    // LEFT BODY
    JointDef("left_shoulder", 13, 11, 23),
    JointDef("left_elbow", 11, 13, 15),
    JointDef("left_wrist", 13, 15, 19),
    JointDef("left_hip", 11, 23, 25),
    JointDef("left_knee", 23, 25, 27),
    JointDef("left_ankle", 25, 27, 31),

    // RIGHT BODY
    JointDef("right_shoulder", 14, 12, 24),
    JointDef("right_elbow", 12, 14, 16),
    JointDef("right_wrist", 14, 16, 20),
    JointDef("right_hip", 12, 24, 26),
    JointDef("right_knee", 24, 26, 28),
    JointDef("right_ankle", 26, 28, 32),
)

fun calculateAngle(a: Landmark3D, b: Landmark3D, c: Landmark3D): Float? {
    val abx = a.x - b.x
    val aby = a.y - b.y
    val bcx = c.x - b.x
    val bcy = c.y - b.y

    val dot = abx * bcx + aby * bcy
    val magAb = sqrt(abx * abx + aby * aby)
    val magBc = sqrt(bcx * bcx + bcy * bcy)

    if (magAb == 0f || magBc == 0f) return null

    var cosTheta = dot / (magAb * magBc)
    cosTheta = cosTheta.coerceIn(-1f, 1f)

    return Math.toDegrees(acos(cosTheta).toDouble()).toFloat()
}

private fun midpoint(a: Landmark3D, b: Landmark3D): Landmark3D =
    Landmark3D(
        (a.x + b.x) / 2f,
        (a.y + b.y) / 2f,
        (a.z + b.z) / 2f
    )

fun calculateTorsoAngle(
    leftShoulder: Landmark3D,
    rightShoulder: Landmark3D,
    leftHip: Landmark3D,
    rightHip: Landmark3D
): Float? {
    val midShoulder = midpoint(leftShoulder, rightShoulder)
    val midHip = midpoint(leftHip, rightHip)

    val vx = midShoulder.x - midHip.x
    val vy = midShoulder.y - midHip.y

    val mag = sqrt(vx * vx + vy * vy)
    if (mag == 0f) return null

    val verticalX = 0f
    val verticalY = -1f

    var cosTheta = (vx * verticalX + vy * verticalY) / mag
    cosTheta = cosTheta.coerceIn(-1f, 1f)

    return Math.toDegrees(acos(cosTheta).toDouble()).toFloat()
}

fun extractAngles(
    landmarks: List<NormalizedLandmark>,
    state: AngleFallbackState
): FloatArray {

    val get = { idx: Int ->
        val lm = landmarks[idx]
        Landmark3D(lm.x(), lm.y(), lm.z())
    }

    val angles = FloatArray(JOINT_DEFS.size + 1)

    for (i in JOINT_DEFS.indices) {
        val j = JOINT_DEFS[i]

        var angle = calculateAngle(get(j.a), get(j.b), get(j.c))

        if (angle == null) {
            val opposite = state.getOpposite(j.name)
            angle = state.getPrev(opposite)
        }

        if (angle == null) angle = state.getPrev(j.name)
        if (angle == null) angle = 0f

        state.setPrev(j.name, angle)
        angles[i] = angle
    }

    var torsoAngle = calculateTorsoAngle(
        leftShoulder = get(11),
        rightShoulder = get(12),
        leftHip = get(23),
        rightHip = get(24)
    )

    if (torsoAngle == null) torsoAngle = state.getPrevTorso()
    if (torsoAngle == null) torsoAngle = 0f

    state.setPrevTorso(torsoAngle)
    angles[JOINT_DEFS.size] = torsoAngle

    return angles
}

fun calculateCaloriesActivity(beratKg: Float, aktivitas: AktivitasLatihan): Float {
    val metMap = mapOf(
        "Push-Up" to 8.0,
        "Sit-Up" to 5.0,
        "Pull-Up" to 9.0,
        "Lunges" to 4.0
    )

    val met = metMap[aktivitas.jenis] ?: 6.0
    val durasiJam = aktivitas.durasiMenit / 60.0

    return (met * beratKg * durasiJam).toFloat()
}

fun calculateTotalCalories(beratKg: Float?, aktivitasList: List<AktivitasLatihan>): Float {
    var totalKalori = 0f
    for (aktivitas in aktivitasList) {
        totalKalori += calculateCaloriesActivity(beratKg!!, aktivitas)
    }
    return totalKalori
}

fun estimateDuration(jenis: String, repetisi: Int): Double {
    val repsPerMinute = mapOf(
        "Push-Up" to 30,
        "Sit-Up" to 30,
        "Pull-Up" to 15,
        "Lunges" to 45
    )
    val rpm = repsPerMinute[jenis] ?: 15
    return repetisi.toDouble() / rpm
}
