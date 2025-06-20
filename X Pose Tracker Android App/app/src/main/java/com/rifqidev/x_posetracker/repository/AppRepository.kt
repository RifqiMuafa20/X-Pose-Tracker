package com.rifqidev.x_posetracker.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.rifqidev.x_posetracker.data.ActivityEntity
import com.rifqidev.x_posetracker.data.ActivityMemberEntity
import com.rifqidev.x_posetracker.data.AppDao
import com.rifqidev.x_posetracker.data.AppDatabase
import com.rifqidev.x_posetracker.data.MemberRecordEntity
import com.rifqidev.x_posetracker.data.UserProfileEntity
import com.rifqidev.x_posetracker.data.UserRecordEntity
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AppRepository(application: Application) {
    private val mAppDao: AppDao
    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()

    init {
        val db = AppDatabase.getDatabase(application)
        mAppDao = db.appDao()
    }

    fun getUserProfile(): LiveData<UserProfileEntity?> {
        return mAppDao.getUserProfileById()
    }

    fun insertUserProfile(profile: UserProfileEntity) {
        executorService.execute { mAppDao.insertUserProfile(profile) }
    }

    fun getUserRecords(userId: String): LiveData<List<UserRecordEntity>> =
        mAppDao.getUserRecordsByUserId(userId)

    fun insertUserRecord(record: UserRecordEntity) {
        executorService.execute { mAppDao.insertUserRecord(record) }
    }

    fun deleteUserRecord(record: UserRecordEntity) {
        executorService.execute { mAppDao.deleteUserRecord(record) }
    }

    fun getAllActivities(): LiveData<List<ActivityEntity>> = mAppDao.getAllActivities()

    fun insertActivity(activity: ActivityEntity) {
        executorService.execute { mAppDao.insertActivity(activity) }
    }

    fun getActivityById(activityId: String): LiveData<ActivityEntity?> {
        return mAppDao.getActivityById(activityId)
    }

    fun deleteActivityById(activityId: String) {
        executorService.execute { mAppDao.deleteActivityById(activityId) }
    }

    fun getActivityMembers(activityId: String): LiveData<List<ActivityMemberEntity>> =
        mAppDao.getMembersByActivityId(activityId)

    fun insertActivityMember(member: ActivityMemberEntity) {
        executorService.execute {
            mAppDao.insertActivityMember(member)
            mAppDao.incrementMemberCount(member.idActivity)
        }
    }

    fun updateActivity(activity: ActivityEntity) {
        executorService.execute { mAppDao.updateActivity(activity) }
    }

    fun getMemberByMemberId(memberId: String): LiveData<ActivityMemberEntity?> {
        return mAppDao.getMemberByMemberId(memberId)
    }

    fun updateMember(member: ActivityMemberEntity) {
        executorService.execute { mAppDao.updateMember(member) }
    }

    fun deleteMemberById(memberId: String, activityId: String) {
        executorService.execute {
            mAppDao.deleteMemberById(memberId)
            mAppDao.decrementMemberCount(activityId)
        }
    }

    fun getMemberRecords(memberId: String): LiveData<List<MemberRecordEntity>> =
        mAppDao.getMemberRecordsByMemberId(memberId)

    fun insertMemberRecord(record: MemberRecordEntity) {
        executorService.execute { mAppDao.insertMemberRecord(record) }
    }

    fun deleteMemberRecord(record: MemberRecordEntity) {
        executorService.execute { mAppDao.deleteMemberRecord(record) }
    }

}