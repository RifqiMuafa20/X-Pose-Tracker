package com.rifqidev.x_posetracker.ui.detail_activity

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rifqidev.x_posetracker.data.ActivityEntity
import com.rifqidev.x_posetracker.data.ActivityMemberEntity
import com.rifqidev.x_posetracker.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DetailEventViewModel(mApplication: Application) : ViewModel() {
    private val repository: AppRepository = AppRepository(mApplication)

    fun getActivityById(activityId: String): LiveData<ActivityEntity?> {
        return repository.getActivityById(activityId)
    }

    fun deleteActivityById(activityId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteActivityById(activityId)
        }
    }

    fun getActivityMembers(activityId: String): LiveData<List<ActivityMemberEntity>> {
        return repository.getActivityMembers(activityId)
    }

    fun deleteMemberById(memberId: String, activityId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteMemberById(memberId, activityId)
        }
    }
}