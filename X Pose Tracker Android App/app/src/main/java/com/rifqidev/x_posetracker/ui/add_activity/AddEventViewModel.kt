package com.rifqidev.x_posetracker.ui.add_activity

import android.app.Application
import androidx.lifecycle.ViewModel
import com.rifqidev.x_posetracker.data.ActivityEntity
import com.rifqidev.x_posetracker.repository.AppRepository

class AddEventViewModel(mApplication: Application) : ViewModel() {
    private val repository: AppRepository = AppRepository(mApplication)

    fun insertActivity(event: ActivityEntity) {
        repository.insertActivity(event)
    }
}