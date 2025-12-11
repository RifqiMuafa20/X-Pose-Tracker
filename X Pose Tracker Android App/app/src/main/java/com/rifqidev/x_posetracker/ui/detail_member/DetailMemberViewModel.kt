package com.rifqidev.x_posetracker.ui.detail_member

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.rifqidev.x_posetracker.data.ActivityMemberEntity
import com.rifqidev.x_posetracker.data.MemberRecordEntity
import com.rifqidev.x_posetracker.repository.AppRepository

class DetailMemberViewModel(mApplication: Application) : ViewModel() {
    private val repository: AppRepository = AppRepository(mApplication)

    fun getMemberById(memberId: String): LiveData<ActivityMemberEntity?> {
        return repository.getMemberByMemberId(memberId)
    }

    fun getTopPullUpRecordByMemberId(memberId: String): LiveData<MemberRecordEntity?> {
        return repository.getTopPullUpRecordByMemberId(memberId)
    }

    fun getTopPushUpRecordByMemberId(memberId: String): LiveData<MemberRecordEntity?> {
        return repository.getTopPushUpRecordByMemberId(memberId)
    }

    fun getTopSitUpRecordByMemberId(memberId: String): LiveData<MemberRecordEntity?> {
        return repository.getTopSitUpRecordByMemberId(memberId)
    }

    fun getTopLungesRecordByMemberId(memberId: String): LiveData<MemberRecordEntity?> {
        return repository.getTopLungesRecordByMemberId(memberId)
    }
}