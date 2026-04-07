package com.rifqidev.x_posetracker.ui.home

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.rifqidev.x_posetracker.data.BestCategoryAchievement
import com.rifqidev.x_posetracker.data.UserProfileEntity
import com.rifqidev.x_posetracker.data.UserStreakEntity
import com.rifqidev.x_posetracker.repository.AppRepository
import com.rifqidev.x_posetracker.utils.DateHelper

class HomeViewModel(mApplication: Application) : ViewModel() {

    private val repository: AppRepository = AppRepository(mApplication)
    private val _dateRange = MutableLiveData<Pair<String, String>>()

    private val _selectedProgressCategory = MutableLiveData<String>("Push-Up")

    init {
        if(_dateRange.value == null) {
            setTodayRange()
        }
    }

    fun getUserProfile(): LiveData<UserProfileEntity?> {
        return repository.getUserProfile()
    }

    fun insertUserStreak(userStreak: UserStreakEntity) {
        return repository.insertUserStreak(userStreak)
    }

    fun getUserStreak(): LiveData<UserStreakEntity?> {
        return repository.getUserStreak()
    }

    private fun updateUserStreak(streak: UserStreakEntity) {
        repository.updateUserStreak(streak)
    }

    fun checkAndUpdateStreakOnHomeOpen(streak: UserStreakEntity) {
        val today = DateHelper.getCurrentLocaleDate()
        val yesterday = today.minusDays(1)

        val lastDate = streak.lastActivityDate?.let {
            DateHelper.parseDate(it)
        }

        var newStreak = streak.currentStreak ?: 0

        when {
            lastDate == null -> {
                newStreak = 0
            }

            lastDate.isEqual(today) -> {
                return
            }

            lastDate.isEqual(yesterday) -> {
                return
            }

            lastDate.isBefore(yesterday) -> {
                newStreak = 0
            }
        }

        if (newStreak != streak.currentStreak) {
            updateUserStreak(
                streak.copy(currentStreak = newStreak)
            )
        }
    }

    fun getTodayCalories(date: String): LiveData<Double?> {
        return repository.getTodayCalories(date)
    }

    fun getTodayDuration(date: String): LiveData<Int?> {
        return repository.getTodayDuration(date)
    }

    val bestAchievements: LiveData<List<BestCategoryAchievement>> =
        _dateRange.switchMap { (start, end) ->
            repository.getBestAchievements(start, end)
        }

    private fun setDateRange(start: String, end: String) {
        _dateRange.value = start to end
    }

    fun setTodayRange() {
        val (start, end) = DateHelper.getTodayRange()
        setDateRange(start, end)
    }

    fun setThisWeekRange() {
        val (start, end) = DateHelper.getThisWeekRange()
        setDateRange(start, end)
    }

    fun setThisMonthRange() {
        val (start, end) = DateHelper.getThisMonthRange()
        setDateRange(start, end)
    }

    fun setThisYearRange() {
        val (start, end) = DateHelper.getThisYearRange()
        setDateRange(start, end)
    }

    fun setAllRange() {
        val start = "0000/01/01 00:00:00"
        val end = "9999/12/31 23:59:59"
        setDateRange(start, end)
    }

    val weeklyProgress = _selectedProgressCategory.switchMap { category ->
        val (startDate, endDate) = DateHelper.getLast7DaysRange()
        repository.getWeeklyProgressByCategory(category, startDate, endDate)
    }

    fun setProgressCategory(category: String) {
        _selectedProgressCategory.value = category
    }
}