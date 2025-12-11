package com.rifqidev.x_posetracker.ui.home

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.rifqidev.x_posetracker.data.BestCategoryAchievement
import com.rifqidev.x_posetracker.data.UserProfileEntity
import com.rifqidev.x_posetracker.repository.AppRepository
import com.rifqidev.x_posetracker.utils.DateHelper
import java.util.Date

class HomeViewModel(mApplication: Application) : ViewModel() {

    private val repository: AppRepository = AppRepository(mApplication)
    private val _dateRange = MutableLiveData<Pair<String, String>>()

    fun getUserProfile(): LiveData<UserProfileEntity?> {
        return repository.getUserProfile()
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

    fun setDateRange(start: String, end: String) {
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
}