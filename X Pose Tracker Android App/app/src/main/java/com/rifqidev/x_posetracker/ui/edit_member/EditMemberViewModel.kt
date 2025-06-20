package com.rifqidev.x_posetracker.ui.edit_member

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.rifqidev.x_posetracker.data.ActivityMemberEntity
import com.rifqidev.x_posetracker.repository.AppRepository

class EditMemberViewModel(mApplication: Application) : ViewModel() {
    private val repository: AppRepository = AppRepository(mApplication)

    fun getMemberById(memberId: String): LiveData<ActivityMemberEntity?> {
        return repository.getMemberByMemberId(memberId)
    }

    fun updateMember(member: ActivityMemberEntity) {
        repository.updateMember(member)
    }
}