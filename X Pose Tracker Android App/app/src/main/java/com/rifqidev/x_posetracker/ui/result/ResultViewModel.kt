package com.rifqidev.x_posetracker.ui.result

import android.app.Application
import androidx.lifecycle.ViewModel
import com.rifqidev.x_posetracker.data.MemberRecordEntity
import com.rifqidev.x_posetracker.data.UserRecordEntity
import com.rifqidev.x_posetracker.repository.AppRepository

class ResultViewModel(mApplication: Application) : ViewModel()  {
    private val repository: AppRepository = AppRepository(mApplication)

    fun insertUserRecord(userRecord: UserRecordEntity) {
        repository.insertUserRecord(userRecord)
    }

    fun insertMemberRecord(memberRecord: MemberRecordEntity){
        repository.insertMemberRecord(memberRecord)
    }

    fun deleteUserRecord(userRecord: String) {
        repository.deleteUserRecordById(userRecord)
    }
}