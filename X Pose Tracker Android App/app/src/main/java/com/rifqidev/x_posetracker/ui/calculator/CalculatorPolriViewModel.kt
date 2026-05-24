package com.rifqidev.x_posetracker.ui.calculator

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rifqidev.x_posetracker.data.calculator_data.polri.PullUpChinningTablePolri
import com.rifqidev.x_posetracker.data.calculator_data.polri.PushUpTablePolri
import com.rifqidev.x_posetracker.data.calculator_data.polri.RenangTablePolri
import com.rifqidev.x_posetracker.data.calculator_data.polri.Run12MinuteTablePolri
import com.rifqidev.x_posetracker.data.calculator_data.polri.ShuttleRunTablePolri
import com.rifqidev.x_posetracker.data.calculator_data.polri.SitUpTablePolri
import com.rifqidev.x_posetracker.utils.ScoreCalculator

class CalculatorPolriViewModel(
    mApplication: Application
) : ViewModel() {

    private val _gender = MutableLiveData(0)

    fun setGender(gender: Int) {
        _gender.value = gender
    }

    // RUNNING POLRI

    private val _runningPolri = MutableLiveData(0)
    val runningPolri: LiveData<Int> = _runningPolri

    fun calculateRunningPolri(distance: Int) {

        val table =
            if (_gender.value == 0)
                Run12MinuteTablePolri.male
            else
                Run12MinuteTablePolri.female

        _runningPolri.value =
            ScoreCalculator.getScore(
                value = distance,
                table = table
            )
    }

    // PULLUP POLRI

    private val _pullUpPolri = MutableLiveData(0)
    val pullUpPolri: LiveData<Int> = _pullUpPolri

    fun calculatePullUpPolri(count: Int) {
        val table =
            if (_gender.value == 0)
                PullUpChinningTablePolri.male
            else
                PullUpChinningTablePolri.female

        _pullUpPolri.value =
            ScoreCalculator.getScore(
                value = count,
                table = table
            )
    }

    // PUSHUP POLRI

    private val _pushUpPolri = MutableLiveData(0)
    val pushUpPolri: LiveData<Int> = _pushUpPolri

    fun calculatePushUpPolri(count: Int) {
        val table =
            if (_gender.value == 0)
                PushUpTablePolri.male
            else
                PushUpTablePolri.female

        _pushUpPolri.value =
            ScoreCalculator.getScore(
                value = count,
                table = table
            )
    }

    // SITUP POLRI

    private val _sitUpPolri = MutableLiveData(0)
    val sitUpPolri: LiveData<Int> = _sitUpPolri

    fun calculateSitUpPolri(count: Int) {
        val table =
            if (_gender.value == 0)
                SitUpTablePolri.male
            else
                SitUpTablePolri.female

        _sitUpPolri.value =
            ScoreCalculator.getScore(
                value = count,
                table = table
            )
    }

    // SHUTTLE RUN POLRI

    private val _shuttleRunPolri = MutableLiveData(0)
    val shuttleRunPolri: LiveData<Int> = _shuttleRunPolri

    fun calculateShuttleRunPolri(count: Double) {
        val table =
            if (_gender.value == 0)
                ShuttleRunTablePolri.male
            else
                ShuttleRunTablePolri.female

        _shuttleRunPolri.value =
            ScoreCalculator.getScoreDouble(
                value = count,
                table = table
            )
    }

    // RENANG POLRI

    private val _renangPolri = MutableLiveData(0)
    val renangPolri: LiveData<Int> = _renangPolri

    fun calculateRenangPolri(count: Double) {
        val table =
            if (_gender.value == 0)
                RenangTablePolri.male
            else
                RenangTablePolri.female

        _renangPolri.value =
            ScoreCalculator.getScoreDouble(
                value = count,
                table = table
            )
    }

    // FINAL RESULT

    private val _fitnessBResult = MutableLiveData(0.0)
    val fitnessBResult: LiveData<Double> = _fitnessBResult

    private val _finalResult = MutableLiveData(0.0)
    val finalResult: LiveData<Double> = _finalResult

    fun calculateFitnessBResult() {
        _fitnessBResult.value = ((_pullUpPolri.value?.toDouble() ?: 0.0) +
                (_pushUpPolri.value?.toDouble() ?: 0.0) +
                (_sitUpPolri.value?.toDouble() ?: 0.0) +
                (_shuttleRunPolri.value?.toDouble() ?: 0.0)) / 4
    }

    fun calculateFinalResult() {
        _finalResult.value = ((_runningPolri.value?.toDouble() ?: 0.0) +
                (_renangPolri.value?.toDouble() ?: 0.0) +
                (_fitnessBResult.value ?: 0.0)) / 3
    }
}