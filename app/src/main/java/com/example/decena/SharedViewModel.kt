package com.example.decena

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    // 1. This holds the text "February", "March", etc.
    private val _currentMonth = MutableLiveData<String>("February")
    val currentMonth: LiveData<String> = _currentMonth

    // 2. Function to change the text
    fun setMonth(monthName: String) {
        _currentMonth.value = monthName
    }
}