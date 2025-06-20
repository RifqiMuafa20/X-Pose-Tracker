package com.rifqidev.x_posetracker.ui.record_supervisor

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.rifqidev.x_posetracker.data.ActivityEntity
import com.rifqidev.x_posetracker.repository.AppRepository

class SupervisorRecordViewModel(mApplication: Application) : ViewModel() {
    private val repository: AppRepository = AppRepository(mApplication)

    val allActivities: LiveData<List<ActivityEntity>> = repository.getAllActivities()
}