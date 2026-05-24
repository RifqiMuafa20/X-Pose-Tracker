package com.rifqidev.x_posetracker.ui.calculator

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rifqidev.x_posetracker.data.calculator_data.tni.PullUpChinningTableTni
import com.rifqidev.x_posetracker.data.calculator_data.tni.PushUpTableTni
import com.rifqidev.x_posetracker.data.calculator_data.tni.RenangTableTni
import com.rifqidev.x_posetracker.data.calculator_data.tni.Run12MinuteTableTni
import com.rifqidev.x_posetracker.data.calculator_data.tni.ShuttleRunTableTni
import com.rifqidev.x_posetracker.data.calculator_data.tni.SitUpTableTni
import com.rifqidev.x_posetracker.utils.ScoreCalculator

class CalculatorTniViewModel(
    mApplication: Application
) : ViewModel() {

    private val _gender = MutableLiveData(0)
    private val _age = MutableLiveData(0)

    fun setGender(gender: Int) {
        _gender.value = gender
    }

    fun setAge(age: Int) {
        _age.value = age
    }

    // Running TNI

    private val _runningTni = MutableLiveData(0)
    val runningTni: LiveData<Int> = _runningTni

    fun calculateRunningTni(distance: Int) {
        val table =
            if (_gender.value == 0)
                Run12MinuteTableTni.male
            else
                Run12MinuteTableTni.female

        val age = _age.value ?: 0

        _runningTni.value =
            ScoreCalculator.getTniScore(
                value = distance,
                age = age,
                table = table
            )
    }

    // Pull Up TNI

    private val _pullUpTni = MutableLiveData(0)
    val pullUpTni: LiveData<Int> = _pullUpTni

    fun calculatePullUpTni(count: Int) {
        val table =
            if (_gender.value == 0)
                PullUpChinningTableTni.male
            else
                PullUpChinningTableTni.female

        val age = _age.value ?: 0

        _pullUpTni.value =
            ScoreCalculator.getTniScore(
                value = count,
                age = age,
                table = table
            )
    }

    // Sit Up TNI

    private val _sitUpTni = MutableLiveData(0)
    val sitUpTni: LiveData<Int> = _sitUpTni

    fun calculateSitUpTni(count: Int) {
        val table =
            if (_gender.value == 0)
                SitUpTableTni.male
            else
                SitUpTableTni.female

        val age = _age.value ?: 0

        _sitUpTni.value =
            ScoreCalculator.getTniScore(
                value = count,
                age = age,
                table = table
            )
    }

    // Push Up TNI

    private val _pushUpTni = MutableLiveData(0)
    val pushUpTni: LiveData<Int> = _pushUpTni

    fun calculatePushUpTni(count: Int) {
        val table =
            if (_gender.value == 0)
                PushUpTableTni.male
            else
                PushUpTableTni.female

        val age = _age.value ?: 0

        _pushUpTni.value =
            ScoreCalculator.getTniScore(
                value = count,
                age = age,
                table = table
            )
    }

    // Shuttle Run TNI

    private val _shuttleRunTni = MutableLiveData(0)
    val shuttleRunTni: LiveData<Int> = _shuttleRunTni

    fun calculateShuttleRunTni(time: Double) {
        val table =
            if (_gender.value == 0)
                ShuttleRunTableTni.male
            else
                ShuttleRunTableTni.female

        val age = _age.value ?: 0

        _shuttleRunTni.value =
            ScoreCalculator.getTniScoreDouble(
                value = time,
                age = age,
                table = table
            )
    }

    // Swimming TNI

    private val _swimmingTni = MutableLiveData(0)
    val swimmingTni: LiveData<Int> = _swimmingTni

    fun calculateSwimmingTni(time: Int) {
        val table =
            if (_gender.value == 0)
                RenangTableTni.male
            else
                RenangTableTni.female

        val age = _age.value ?: 0

        _swimmingTni.value =
            ScoreCalculator.getTniScoreDouble(
                value = time.toDouble(),
                age = age,
                table = table
            )
    }

    // FINAL RESULT

    private val _fitnessBResult = MutableLiveData(0.0)
    val fitnessBResult: LiveData<Double> = _fitnessBResult

    private val _finalResult = MutableLiveData(0.0)
    val finalResult: LiveData<Double> = _finalResult

    fun calculateFitnessBResult() {
        _fitnessBResult.value = ((_pullUpTni.value?.toDouble() ?: 0.0) +
                (_pushUpTni.value?.toDouble() ?: 0.0) +
                (_sitUpTni.value?.toDouble() ?: 0.0) +
                (_shuttleRunTni.value?.toDouble() ?: 0.0)) / 4
    }

    fun calculateFinalResult() {
        _finalResult.value = ((_runningTni.value?.toDouble() ?: 0.0) +
                (_swimmingTni.value?.toDouble() ?: 0.0) +
                (_fitnessBResult.value ?: 0.0)) / 3
    }
}