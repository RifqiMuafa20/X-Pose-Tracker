package com.rifqidev.x_posetracker.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateHelper {
    private val formatter = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private val indoFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
    private val monthDayFormat = SimpleDateFormat("dd/MM", Locale.getDefault())

    fun getCurrentDate(): String {
        val date = Date()
        return formatter.format(date)
    }

    fun getMonthDay(dateString: String): String {
        val date = formatter.parse(dateString)
        return monthDayFormat.format(date!!)
    }

    fun getCurrentTime(): String {
        val time = Date()
        return timeFormat.format(time)
    }

    fun formatDateToIndo(dateString: String): String {
        val date = formatter.parse(dateString)
        return indoFormat.format(date!!)
    }

    fun formatTime(seconds: Long): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    fun getTodayRange(): Pair<String, String> {
        val cal = Calendar.getInstance()

        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = formatter.format(cal.time)

        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val end = formatter.format(cal.time)

        return start to end
    }

    fun getThisWeekRange(): Pair<String, String> {
        val cal = Calendar.getInstance()

        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        val start = formatter.format(cal.time)

        cal.add(Calendar.DAY_OF_WEEK, 6)
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val end = formatter.format(cal.time)

        return start to end
    }

    fun getThisMonthRange(): Pair<String, String> {
        val cal = Calendar.getInstance()

        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        val start = formatter.format(cal.time)

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val end = formatter.format(cal.time)

        return start to end
    }

    fun getThisYearRange(): Pair<String, String> {
        val cal = Calendar.getInstance()

        cal.set(Calendar.DAY_OF_YEAR, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        val start = formatter.format(cal.time)

        cal.set(Calendar.DAY_OF_YEAR, cal.getActualMaximum(Calendar.DAY_OF_YEAR))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val end = formatter.format(cal.time)

        return start to end
    }

    fun getLast7DaysRange(): Pair<String, String> {
        val cal = Calendar.getInstance()

        val endDate = formatter.format(cal.time)

        cal.add(Calendar.DAY_OF_YEAR, -6)
        val startDate = formatter.format(cal.time)

        return startDate to endDate
    }

    fun getDateRange(startDate: String, endDate: String): List<String> {
        val dates = mutableListOf<String>()

        try {
            val start = formatter.parse(startDate)
            val end = formatter.parse(endDate)
            val calendar = Calendar.getInstance()
            if (start != null) {
                calendar.time = start
            }

            while (calendar.time <= end) {
                dates.add(formatter.format(calendar.time))
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return dates
    }
}