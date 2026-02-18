package com.example.decena

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*

class TasksViewModel : ViewModel() {

    private val _tasks = MutableLiveData<List<Task>>(emptyList())
    val tasks: LiveData<List<Task>> = _tasks

    private val _selectedDate = MutableLiveData<Long>(System.currentTimeMillis())
    val selectedDate: LiveData<Long> = _selectedDate

    private lateinit var databaseHelper: TaskDatabaseHelper
    private val mutex = Mutex()

    fun initializeDatabase(helper: TaskDatabaseHelper) {
        this.databaseHelper = helper
        loadTasksForSelectedDate()
    }

    fun setSelectedDate(dateInMillis: Long) {
        _selectedDate.value = dateInMillis
        loadTasksForSelectedDate()
    }

    fun loadTasksForSelectedDate() {
        val date = _selectedDate.value ?: return

        viewModelScope.launch {
            try {
                val tasks = withContext(Dispatchers.IO) {
                    databaseHelper.getTasksForDate(date)
                }
                _tasks.postValue(tasks)
                println("🔄 ViewModel loaded ${tasks.size} tasks")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addTask(task: Task) {
        viewModelScope.launch {
            mutex.withLock {
                try {
                    // Add task to database
                    val id = withContext(Dispatchers.IO) {
                        databaseHelper.addTask(task)
                    }
                    println("➕ Task added with ID: $id")

                    // Immediately reload tasks
                    loadTasksForSelectedDate()

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun updateTaskCompletion(taskId: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            mutex.withLock {
                try {
                    withContext(Dispatchers.IO) {
                        databaseHelper.updateTaskCompletion(taskId, isCompleted)
                    }
                    loadTasksForSelectedDate()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            mutex.withLock {
                try {
                    withContext(Dispatchers.IO) {
                        databaseHelper.deleteTask(task.id)
                    }
                    loadTasksForSelectedDate()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}