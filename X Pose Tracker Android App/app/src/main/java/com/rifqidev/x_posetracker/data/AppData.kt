package com.rifqidev.x_posetracker.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

data class WorkoutItem(
    val name: String,
    val iconResId: Int,
    val date: String?,
    val repetition: String?
)

data class AktivitasLatihan(
    val jenis: String,
    val durasiMenit: Double,
    val repetisi: Int
)

data class FaqItem(
    val question: String,
    val answer: String,
    var isExpanded: Boolean = false
)

data class BestCategoryAchievement(
    val category: String,
    val bestCount: Int,
    val bestDate: String?
)

data class ActivityMemberExport(
    val memberName: String?,
    val memberRegistrationNumber: String?,
    val maxPushup: Int?,
    val maxPullup: Int?,
    val maxSitup: Int?,
    val maxLunges: Int?
)

data class WeeklyProgress(
    @ColumnInfo(name = "date")
    val date: String,

    @ColumnInfo(name = "totalValue")
    val totalValue: Int
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    @ColumnInfo(name = "id_user") val idUser: String,
    @ColumnInfo(name = "user_name") val userName: String?,
    @ColumnInfo(name = "user_gender") val userGender: String?,
    @ColumnInfo(name = "user_birth") val userBirth: String?,
    @ColumnInfo(name = "user_weight") val userWeight: Int?,
    @ColumnInfo(name = "user_height") val userHeight: Int?,
    @ColumnInfo(name = "user_goal") val userGoal: String?,
    @ColumnInfo(name = "user_profile") val userProfile: ByteArray?
)

@Entity(
    tableName = "user_record",
    foreignKeys = [ForeignKey(
        entity = UserProfileEntity::class,
        parentColumns = ["id_user"],
        childColumns = ["id_user"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("id_user")]
)
@Parcelize
data class UserRecordEntity(
    @PrimaryKey
    @ColumnInfo(name = "id_record") val idRecord: String,
    @ColumnInfo(name = "id_user") val idUser: String,
    @ColumnInfo(name = "record_name") val recordName: String?,
    @ColumnInfo(name = "record_duration") val recordDuration: Int?,
    @ColumnInfo(name = "record_date") val recordDate: String?,
    @ColumnInfo(name = "record_time") val recordTime: String?,
    @ColumnInfo(name = "record_calories") val recordCalories: Double?,
    @ColumnInfo(name = "pushup_count") val pushupCount: Int?,
    @ColumnInfo(name = "situp_count") val situpCount: Int?,
    @ColumnInfo(name = "pullup_count") val pullupCount: Int?,
    @ColumnInfo(name = "lunges_count") val lungesCount: Int?,
    @ColumnInfo(name = "record_photos") val recordPhotos: ByteArray?
) : Parcelable

@Entity(tableName = "activity")
@Parcelize
data class ActivityEntity(
    @PrimaryKey
    @ColumnInfo(name = "id_activity") val idActivity: String,
    @ColumnInfo(name = "activity_name") val activityName: String?,
    @ColumnInfo(name = "activity_date") val activityDate: String?,
    @ColumnInfo(name = "activity_supervisor") val activitySupervisor: String?,
    @ColumnInfo(name = "activity_location") val activityLocation: String?,
    @ColumnInfo(name = "member_amount") val memberAmount: Int?
) : Parcelable

@Entity(
    tableName = "activity_member",
    foreignKeys = [ForeignKey(
        entity = ActivityEntity::class,
        parentColumns = ["id_activity"],
        childColumns = ["id_activity"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("id_activity")]
)
@Parcelize
data class ActivityMemberEntity(
    @PrimaryKey
    @ColumnInfo(name = "id_member") val idMember: String,
    @ColumnInfo(name = "id_activity") val idActivity: String,
    @ColumnInfo(name = "member_name") val memberName: String?,
    @ColumnInfo(name = "member_registration_number") val memberRegistrationNumber: String?
) : Parcelable

@Entity(
    tableName = "member_record",
    foreignKeys = [ForeignKey(
        entity = ActivityMemberEntity::class,
        parentColumns = ["id_member"],
        childColumns = ["id_member"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("id_member")]
)
@Parcelize
data class MemberRecordEntity(
    @PrimaryKey
    @ColumnInfo(name = "id_record") val idRecord: String,
    @ColumnInfo(name = "id_member") val idMember: String,
    @ColumnInfo(name = "record_date") val recordDate: String?,
    @ColumnInfo(name = "record_time") val recordTime: String?,
    @ColumnInfo(name = "record_duration") val recordDuration: Int?,
    @ColumnInfo(name = "pushup_count") val pushupCount: Int?,
    @ColumnInfo(name = "pullup_count") val pullupCount: Int?,
    @ColumnInfo(name = "situp_count") val situpCount: Int?,
    @ColumnInfo(name = "lunges_count") val lungesCount: Int?
) : Parcelable
