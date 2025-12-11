package com.rifqidev.x_posetracker.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AppDao {

    //Profile
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUserProfile(profile: UserProfileEntity)

    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getUserProfileById(): LiveData<UserProfileEntity?>

    //User Record
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUserRecord(record: UserRecordEntity)

    @Query("SELECT * FROM user_record WHERE id_user = :userId")
    fun getUserRecordsByUserId(userId: String): LiveData<List<UserRecordEntity>>

    @Query("DELETE FROM user_record WHERE id_record = :recordId")
    fun deleteUserRecordById(recordId: String)

    //Activity
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertActivity(activity: ActivityEntity)

    @Query("SELECT * FROM activity")
    fun getAllActivities(): LiveData<List<ActivityEntity>>

    @Query("SELECT * FROM activity WHERE id_activity = :activityId LIMIT 1")
    fun getActivityById(activityId: String): LiveData<ActivityEntity?>

    @Update
    fun updateActivity(activity: ActivityEntity)

    @Query("UPDATE activity SET member_amount = member_amount + 1 WHERE id_activity = :activityId")
    fun incrementMemberCount(activityId: String)

    @Query("UPDATE activity SET member_amount = member_amount - 1 WHERE id_activity = :activityId AND member_amount > 0")
    fun decrementMemberCount(activityId: String)

    @Query("DELETE FROM activity WHERE id_activity = :activityId")
    fun deleteActivityById(activityId: String)

    //Member
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertActivityMember(member: ActivityMemberEntity)

    @Query("SELECT * FROM activity_member WHERE id_activity = :activityId")
    fun getMembersByActivityId(activityId: String): LiveData<List<ActivityMemberEntity>>

    @Query("SELECT * FROM activity_member WHERE id_member = :memberId LIMIT 1")
    fun getMemberByMemberId(memberId: String): LiveData<ActivityMemberEntity?>

    @Update
    fun updateMember(member: ActivityMemberEntity)

    @Query("DELETE FROM activity_member WHERE id_member = :memberId")
    fun deleteMemberById(memberId: String)

    //Member Record
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMemberRecord(record: MemberRecordEntity)

    @Query("SELECT * FROM member_record WHERE id_member = :memberId ORDER BY pullup_count DESC LIMIT 1")
    fun getTopPullUpRecordByMemberId(memberId: String): LiveData<MemberRecordEntity?>

    @Query("SELECT * FROM member_record WHERE id_member = :memberId ORDER BY pushup_count DESC LIMIT 1")
    fun getTopPushUpRecordByMemberId(memberId: String): LiveData<MemberRecordEntity?>

    @Query("SELECT * FROM member_record WHERE id_member = :memberId ORDER BY situp_count DESC LIMIT 1")
    fun getTopSitUpRecordByMemberId(memberId: String): LiveData<MemberRecordEntity?>

    @Query("SELECT * FROM member_record WHERE id_member = :memberId ORDER BY lunges_count DESC LIMIT 1")
    fun getTopLungesRecordByMemberId(memberId: String): LiveData<MemberRecordEntity?>

    //home page
    @Query("SELECT SUM(record_calories) FROM user_record WHERE record_date LIKE :todayDate || '%'")
    fun getTodayCalories(todayDate: String): LiveData<Double?>

    @Query("SELECT SUM(record_duration) FROM user_record WHERE record_date LIKE :todayDate || '%'")
    fun getTodayDurations(todayDate: String): LiveData<Int?>
}
