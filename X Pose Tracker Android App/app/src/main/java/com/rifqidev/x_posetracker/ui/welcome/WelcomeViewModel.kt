package com.rifqidev.x_posetracker.ui.welcome

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rifqidev.x_posetracker.data.UserProfileEntity
import com.rifqidev.x_posetracker.repository.AppRepository

class WelcomeViewModel(mApplication: Application) : ViewModel() {

    private val repository: AppRepository = AppRepository(mApplication)

    private val _name = MutableLiveData<String>()
    val name: LiveData<String> = _name

    private val _birthDate = MutableLiveData<String>()
    val birthDate: LiveData<String> = _birthDate

    private val _gender = MutableLiveData<String>()
    val gender: LiveData<String> = _gender

    private val _weight = MutableLiveData<String>()
    val weight: LiveData<String> = _weight

    private val _height = MutableLiveData<String>()
    val height: LiveData<String> = _height

    private val _goal = MutableLiveData<String>()
    val goal: LiveData<String> = _goal

    fun setName(name: String) {
        _name.value = name
    }

    fun setBirthDate(date: String) {
        _birthDate.value = date
    }

    fun setGender(gender: String) {
        _gender.value = gender
    }

    fun setWeight(weight: String) {
        _weight.value = weight
    }

    fun setHeight(height: String) {
        _height.value = height
    }

    fun setGoal(goal: String) {
        _goal.value = goal
    }

    fun insertUserProfile(userProfile: UserProfileEntity) {
        repository.insertUserProfile(userProfile)
    }

    fun getUserProfile(): LiveData<UserProfileEntity?> {
        return repository.getUserProfile()
    }
}