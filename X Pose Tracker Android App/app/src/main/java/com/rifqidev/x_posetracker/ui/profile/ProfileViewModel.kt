package com.rifqidev.x_posetracker.ui.profile

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.rifqidev.x_posetracker.data.UserProfileEntity
import com.rifqidev.x_posetracker.repository.AppRepository

class ProfileViewModel(mApplication: Application) : ViewModel() {
    private val repository: AppRepository = AppRepository(mApplication)

    fun getUserProfile(): LiveData<UserProfileEntity?> {
        return repository.getUserProfile()
    }
}