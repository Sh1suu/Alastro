package com.example.decena

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TimerViewModel : ViewModel() {
    private var countDownTimer: CountDownTimer? = null

    var initialTimeInMillis: Long = 5 * 60 * 1000L
        private set

    var intervalMinutes: Int = 5
        private set

    private val _timeLeftInMillis = MutableLiveData<Long>(initialTimeInMillis)
    val timeLeftInMillis: LiveData<Long> = _timeLeftInMillis

    private val _isRunning = MutableLiveData<Boolean>(false)
    val isRunning: LiveData<Boolean> = _isRunning

    private val _isWorkSession = MutableLiveData<Boolean>(true)
    val isWorkSession: LiveData<Boolean> = _isWorkSession

    private val _currentCycle = MutableLiveData<Int>(1)
    val currentCycle: LiveData<Int> = _currentCycle

    private val _totalCycles = MutableLiveData<Int>(4)
    val totalCycles: LiveData<Int> = _totalCycles

    // This handles playing the alarm sound and Toasts
    private val _timerEvent = MutableLiveData<TimerEvent?>()
    val timerEvent: LiveData<TimerEvent?> = _timerEvent

    enum class TimerEvent {
        WORK_FINISHED, BREAK_FINISHED, ALL_CYCLES_COMPLETED
    }

    fun clearEvent() {
        _timerEvent.value = null
    }

    fun setInitialTime(millis: Long) {
        initialTimeInMillis = millis
        if (_isRunning.value == false) {
            _timeLeftInMillis.value = millis
        }
    }

    fun setTotalCycles(cycles: Int) {
        _totalCycles.value = cycles
    }

    fun setIntervalMinutes(minutes: Int) {
        intervalMinutes = minutes
    }

    fun toggleTimer() {
        if (_isRunning.value == true) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        val time = _timeLeftInMillis.value ?: initialTimeInMillis
        countDownTimer = object : CountDownTimer(time, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _timeLeftInMillis.value = millisUntilFinished
            }

            override fun onFinish() {
                handleTimerFinish()
            }
        }.start()
        _isRunning.value = true
    }

    fun pauseTimer() {
        countDownTimer?.cancel()
        _isRunning.value = false
    }

    fun resetTimer() {
        countDownTimer?.cancel()
        _isRunning.value = false
        _isWorkSession.value = true
        _currentCycle.value = 1
        _timeLeftInMillis.value = initialTimeInMillis
    }

    private fun handleTimerFinish() {
        val currentlyWork = _isWorkSession.value ?: true
        val cycle = _currentCycle.value ?: 1
        val total = _totalCycles.value ?: 4

        if (currentlyWork) {
            // Work finished, start break
            _isWorkSession.value = false
            _timerEvent.value = TimerEvent.WORK_FINISHED
            runTimer((intervalMinutes * 60 * 1000).toLong())
        } else {
            // Break finished
            if (cycle < total) {
                _currentCycle.value = cycle + 1
                _isWorkSession.value = true
                _timerEvent.value = TimerEvent.BREAK_FINISHED
                runTimer(initialTimeInMillis)
            } else {
                // All done
                _timerEvent.value = TimerEvent.ALL_CYCLES_COMPLETED
                resetTimer()
            }
        }
    }

    private fun runTimer(timeInMillis: Long) {
        _timeLeftInMillis.value = timeInMillis
        startTimer()
    }

    override fun onCleared() {
        super.onCleared()
        // Only cancel if the app is completely closed
        countDownTimer?.cancel()
    }
}