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

    @Update
    fun updateUserProfile(profile: UserProfileEntity)

    //User Record
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUserRecord(record: UserRecordEntity)

    @Query("SELECT * FROM user_record WHERE id_user = :userId ORDER BY record_date DESC, record_time DESC")
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

    @Query("""
        SELECT 'Push-Up' AS category, t.bestCount, t.bestDate
        FROM (
            SELECT pushup_count AS bestCount, record_date AS bestDate
            FROM user_record
            WHERE pushup_count IS NOT NULL
              AND record_date BETWEEN :startDate AND :endDate
            ORDER BY pushup_count DESC
            LIMIT 1
        ) t
    
        UNION ALL
    
        SELECT 'Sit-Up', t.bestCount, t.bestDate
        FROM (
            SELECT situp_count AS bestCount, record_date AS bestDate
            FROM user_record
            WHERE situp_count IS NOT NULL
              AND record_date BETWEEN :startDate AND :endDate
            ORDER BY situp_count DESC
            LIMIT 1
        ) t
    
        UNION ALL
    
        SELECT 'Pull-Up', t.bestCount, t.bestDate
        FROM (
            SELECT pullup_count AS bestCount, record_date AS bestDate
            FROM user_record
            WHERE pullup_count IS NOT NULL
              AND record_date BETWEEN :startDate AND :endDate
            ORDER BY pullup_count DESC
            LIMIT 1
        ) t
    
        UNION ALL
    
        SELECT 'Lunges', t.bestCount, t.bestDate
        FROM (
            SELECT lunges_count AS bestCount, record_date AS bestDate
            FROM user_record
            WHERE lunges_count IS NOT NULL
              AND record_date BETWEEN :startDate AND :endDate
            ORDER BY lunges_count DESC
            LIMIT 1
        ) t
    """)
    fun getBestAchievementsPerCategoryInRange(
        startDate: String,
        endDate: String
    ): LiveData<List<BestCategoryAchievement>>

    @Query("""
        SELECT 
            record_date AS date,
            SUM(
                CASE
                    WHEN :category = 'Push-Up' THEN IFNULL(pushup_count, 0)
                    WHEN :category = 'Sit-Up' THEN IFNULL(situp_count, 0)
                    WHEN :category = 'Pull-Up' THEN IFNULL(pullup_count, 0)
                    WHEN :category = 'Lunges' THEN IFNULL(lunges_count, 0)
                    ELSE 0
                END
            ) AS totalValue
        FROM user_record
        WHERE record_date BETWEEN :startDate AND :endDate
        GROUP BY record_date
        ORDER BY record_date
    """)
    fun getWeeklyProgressByCategory(
        category: String,
        startDate: String,
        endDate: String
    ): LiveData<List<WeeklyProgress>>
}
