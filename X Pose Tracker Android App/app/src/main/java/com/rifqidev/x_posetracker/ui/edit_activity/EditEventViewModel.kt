package com.rifqidev.x_posetracker.ui.edit_activity

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.rifqidev.x_posetracker.data.ActivityEntity
import com.rifqidev.x_posetracker.repository.AppRepository
import kotlinx.coroutines.flow.Flow

class EditEventViewModel(mApplication: Application) : ViewModel() {
    private val repository: AppRepository = AppRepository(mApplication)

    fun getActivityById(activityId: String): Flow<ActivityEntity> {
         return repository.getActivityById(activityId)
    }

    fun updateActivity(event: ActivityEntity) {
        repository.updateActivity(event)
    }
}