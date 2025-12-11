package com.dicoding.picodiploma.mynoteapps.helper

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rifqidev.x_posetracker.ui.add_activity.AddEventViewModel
import com.rifqidev.x_posetracker.ui.add_member.AddMemberViewModel
import com.rifqidev.x_posetracker.ui.camera.CameraViewModel
import com.rifqidev.x_posetracker.ui.detail_activity.DetailEventViewModel
import com.rifqidev.x_posetracker.ui.detail_member.DetailMemberViewModel
import com.rifqidev.x_posetracker.ui.edit_activity.EditEventViewModel
import com.rifqidev.x_posetracker.ui.edit_member.EditMemberViewModel
import com.rifqidev.x_posetracker.ui.editprofile.EditProfileViewModel
import com.rifqidev.x_posetracker.ui.history.HistoryViewModel
import com.rifqidev.x_posetracker.ui.home.HomeViewModel
import com.rifqidev.x_posetracker.ui.profile.ProfileViewModel
import com.rifqidev.x_posetracker.ui.record_supervisor.SupervisorRecordViewModel
import com.rifqidev.x_posetracker.ui.result.ResultViewModel
import com.rifqidev.x_posetracker.ui.welcome.WelcomeViewModel

class ViewModelFactory private constructor(private val mApplication: Application) :
    ViewModelProvider.NewInstanceFactory() {

    companion object {
        @Volatile
        private var INSTANCE: ViewModelFactory? = null

        @JvmStatic
        fun getInstance(application: Application): ViewModelFactory {
            if (INSTANCE == null) {
                synchronized(ViewModelFactory::class.java) {
                    INSTANCE = ViewModelFactory(application)
                }
            }
            return INSTANCE as ViewModelFactory
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(mApplication) as T
        } else if (modelClass.isAssignableFrom(WelcomeViewModel::class.java)) {
            return WelcomeViewModel(mApplication) as T
        } else if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(mApplication) as T
        } else if (modelClass.isAssignableFrom(EditProfileViewModel::class.java)) {
            return EditProfileViewModel(mApplication) as T
        } else if (modelClass.isAssignableFrom((AddEventViewModel::class.java))) {
            return AddEventViewModel(mApplication) as T
        } else if (modelClass.isAssignableFrom((SupervisorRecordViewModel::class.java))) {
            return SupervisorRecordViewModel(mApplication) as T
        } else if (modelClass.isAssignableFrom(DetailEventViewModel::class.java)) {
            return DetailEventViewModel(mApplication) as T
        } else if (modelClass.isAssignableFrom(AddMemberViewModel::class.java)) {
            return AddMemberViewModel(mApplication) as T
        } else if (modelClass.isAssignableFrom(EditEventViewModel::class.java)) {
            return EditEventViewModel(mApplication) as T
        } else if (modelClass.isAssignableFrom(DetailMemberViewModel::class.java)) {
            return DetailMemberViewModel(mApplication) as T
        } else if (modelClass.isAssignableFrom(EditMemberViewModel::class.java)) {
            return EditMemberViewModel(mApplication) as T
        } else if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            return HistoryViewModel(mApplication) as T
        } else if (modelClass.isAssignableFrom(CameraViewModel::class.java)) {
            return CameraViewModel(mApplication) as T
        } else if (modelClass.isAssignableFrom(ResultViewModel::class.java)) {
            return ResultViewModel(mApplication) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}