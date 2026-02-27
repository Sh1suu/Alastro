package com.example.decena

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Calendar

class TasksViewModel : ViewModel() {

    private val _tasks = MutableLiveData<List<Task>>(emptyList())
    val tasks: LiveData<List<Task>> = _tasks

    private val _allTasks = MutableLiveData<List<Task>>(emptyList())
    val allTasks: LiveData<List<Task>> = _allTasks

    private val _selectedDate = MutableLiveData<Long>(System.currentTimeMillis())
    val selectedDate: LiveData<Long> = _selectedDate

    private lateinit var databaseHelper: TaskDatabaseHelper
    private val mutex = Mutex()

    fun initializeDatabase(helper: TaskDatabaseHelper) {
        this.databaseHelper = helper
        loadTasksForSelectedDate()
        loadAllTasks()
    }

    fun setSelectedDate(dateInMillis: Long) {
        _selectedDate.value = dateInMillis
        loadTasksForSelectedDate()
    }

    fun loadTasksForSelectedDate() {
        val date = _selectedDate.value ?: return
        viewModelScope.launch {
            try {
                val tasks = withContext(Dispatchers.IO) { databaseHelper.getTasksForDate(date) }
                _tasks.postValue(tasks)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun loadAllTasks() {
        viewModelScope.launch {
            try {
                val tasks = withContext(Dispatchers.IO) {
                    val all = databaseHelper.getAllTasks()
                    val calendar = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val startOfToday = calendar.timeInMillis
                    all.filter { it.date >= startOfToday }
                        .sortedWith(compareBy({ it.date }, { it.time }))
                }
                _allTasks.postValue(tasks)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun addTask(task: Task, context: Context) {
        viewModelScope.launch {
            mutex.withLock {
                val id = withContext(Dispatchers.IO) { databaseHelper.addTask(task) }
                scheduleAlarm(context, task.title, task.date, id.toInt())
                loadTasksForSelectedDate()
                loadAllTasks() // <-- Instantly updates the dashboard
            }
        }
    }

    fun updateTaskCompletion(taskId: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            mutex.withLock {
                withContext(Dispatchers.IO) { databaseHelper.updateTaskCompletion(taskId, isCompleted) }
                loadTasksForSelectedDate()
                loadAllTasks()
            }
        }
    }

    fun deleteTask(task: Task, context: Context) {
        viewModelScope.launch {
            mutex.withLock {
                withContext(Dispatchers.IO) { databaseHelper.deleteTask(task.id) }
                cancelAlarm(context, task.id)
                loadTasksForSelectedDate()
                loadAllTasks() // <-- Guaranteed to clear it from dashboard
            }
        }
    }

    fun updateTask(task: Task, context: Context) {
        viewModelScope.launch {
            mutex.withLock {
                withContext(Dispatchers.IO) { databaseHelper.updateTask(task) }
                scheduleAlarm(context, task.title, task.date, task.id) // Update Alarm
                loadTasksForSelectedDate()
                loadAllTasks()
            }
        }
    }

    // --- ALARM LOGIC ---
    private fun scheduleAlarm(context: Context, title: String, exactTimeInMillis: Long, taskId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
            putExtra("EXTRA_TASK_TITLE", title)
            putExtra("EXTRA_TASK_ID", taskId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, taskId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Ensure the time hasn't already passed
        if (exactTimeInMillis > System.currentTimeMillis()) {
            try {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, exactTimeInMillis, pendingIntent)
            } catch (e: SecurityException) {
                // Fallback for Android 12+ if exact alarm permission is disabled
                alarmManager.set(AlarmManager.RTC_WAKEUP, exactTimeInMillis, pendingIntent)
            }
        }
    }

    private fun cancelAlarm(context: Context, taskId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TaskAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, taskId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}