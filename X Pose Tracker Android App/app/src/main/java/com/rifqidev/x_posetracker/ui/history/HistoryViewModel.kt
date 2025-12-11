package com.rifqidev.x_posetracker.ui.history

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rifqidev.x_posetracker.data.UserProfileEntity
import com.rifqidev.x_posetracker.data.UserRecordEntity
import com.rifqidev.x_posetracker.repository.AppRepository

class HistoryViewModel(mApplication: Application) : ViewModel() {
    private val repository: AppRepository = AppRepository(mApplication)

    fun getUserProfile(): LiveData<UserProfileEntity?> {
        return repository.getUserProfile()
    }

    fun getAllRecord(userId: String): LiveData<List<UserRecordEntity>> {
        return repository.getUserRecords(userId)
    }
}