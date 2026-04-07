package com.rifqidev.x_posetracker.ui.result

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.rifqidev.x_posetracker.data.MemberRecordEntity
import com.rifqidev.x_posetracker.data.UserRecordEntity
import com.rifqidev.x_posetracker.data.UserStreakEntity
import com.rifqidev.x_posetracker.repository.AppRepository
import com.rifqidev.x_posetracker.utils.DateHelper

class ResultViewModel(mApplication: Application) : ViewModel()  {
    private val repository: AppRepository = AppRepository(mApplication)

    fun insertUserRecord(userRecord: UserRecordEntity) {
        repository.insertUserRecord(userRecord)
    }

    private fun getUserStreak(): LiveData<UserStreakEntity?> {
        return repository.getUserStreak()
    }

    private fun updateUserStreak(streak: UserStreakEntity) {
        repository.updateUserStreak(streak)
    }

    fun onWorkoutCompleted() {
        getUserStreak().observeForever(object : Observer<UserStreakEntity?> {
            override fun onChanged(streak: UserStreakEntity?) {
                if (streak != null) {
                    checkAndUpdateStreak(streak)
                    getUserStreak().removeObserver(this)
                }
            }
        })
    }

    private fun checkAndUpdateStreak(streak: UserStreakEntity) {
        val today = DateHelper.getCurrentLocaleDate()
        val yesterday = today.minusDays(1)

        val lastDate = streak.lastActivityDate?.let {
            DateHelper.parseDate(it)
        }

        var newStreak = streak.currentStreak ?: 0

        when {
            lastDate == null -> {
                newStreak = 1
            }

            lastDate.isEqual(today) -> {
                return
            }

            lastDate.isEqual(yesterday) -> {
                newStreak += 1
            }

            else -> {
                newStreak = 1
            }
        }

        val updated = streak.copy(
            currentStreak = newStreak,
            lastActivityDate = DateHelper.getCurrentDate(),
            longestStreak = maxOf(streak.longestStreak ?: 0, newStreak)
        )

        updateUserStreak(updated)
    }

    fun insertMemberRecord(memberRecord: MemberRecordEntity){
        repository.insertMemberRecord(memberRecord)
    }

    fun deleteUserRecord(userRecord: String) {
        repository.deleteUserRecordById(userRecord)
    }
}