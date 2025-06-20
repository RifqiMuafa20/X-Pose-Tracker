package com.rifqidev.x_posetracker.ui.add_member

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rifqidev.x_posetracker.data.ActivityEntity
import com.rifqidev.x_posetracker.data.ActivityMemberEntity
import com.rifqidev.x_posetracker.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddMemberViewModel(mApplication: Application) : ViewModel() {
    private val repository: AppRepository = AppRepository(mApplication)

    fun insertActivityMember(member: ActivityMemberEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertActivityMember(member)
        }
    }

    fun updateActivity(event: ActivityEntity) {
        repository.updateActivity(event)
    }
}