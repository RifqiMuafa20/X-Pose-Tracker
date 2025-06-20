package com.rifqidev.x_posetracker.utils

object StringHelper {
    fun getInitials(text: String): String {
        val cleaned = text.trim()
        if (cleaned.isEmpty()) return ""

        val words = cleaned.split("\\s+".toRegex())

        return if (words.size >= 2) {
            words.take(2)
                .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                .joinToString("")
        } else {
            val word = words[0]
            if (word.length >= 2) {
                word.take(2).uppercase()
            } else {
                word.first().uppercaseChar().toString()
            }
        }
    }
}
