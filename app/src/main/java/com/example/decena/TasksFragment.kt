package com.example.decena

import android.Manifest
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*

class TasksFragment : Fragment() {

    private lateinit var tasksContainer: LinearLayout
    private lateinit var btnAddTask: View
    private lateinit var imgProfile: ImageView
    private lateinit var todayTextView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var viewModel: TasksViewModel
    private lateinit var databaseHelper: TaskDatabaseHelper

    private var selectedDate: Calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    // Request Notification Permission for Android 13+
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(context, "Task reminders disabled", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tasks, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ask for notification permission immediately upon opening Tasks tab
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        try {
            initializeViews(view)
            setupRecyclerView()
            setupViewModel()
            setupClickListeners()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun initializeViews(view: View) {
        tasksContainer = view.findViewById(R.id.tasksContainer)
        btnAddTask = view.findViewById(R.id.btnAddTask)
        imgProfile = view.findViewById(R.id.imgProfile)
        todayTextView = view.findViewById(R.id.tvTodayHeader)
    }

    private fun setupRecyclerView() {
        recyclerView = RecyclerView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            layoutManager = LinearLayoutManager(requireContext())
        }

        tasksContainer.removeAllViews()
        tasksContainer.addView(todayTextView)
        tasksContainer.addView(recyclerView)

        taskAdapter = TaskAdapter(
            tasks = emptyList(),
            onTaskCheckedListener = { task, isChecked ->
                viewModel.updateTaskCompletion(task.id, isChecked)
            },
            onTaskEditListener = { task ->
                showTaskDialog(task)
            },
            onTaskDeleteListener = { task ->
                viewModel.deleteTask(task, requireContext()) // <-- Passing context to delete alarm
                Snackbar.make(requireView(), "Task deleted", Snackbar.LENGTH_SHORT).show()
            }
        )
        recyclerView.adapter = taskAdapter
    }

    private fun setupViewModel() {
        databaseHelper = TaskDatabaseHelper(requireContext())
        val factory = TasksViewModelFactory(databaseHelper)
        viewModel = ViewModelProvider(requireActivity(), factory).get(TasksViewModel::class.java)

        viewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            updateTasksList(tasks)
        }

        viewModel.selectedDate.observe(viewLifecycleOwner) { dateInMillis ->
            selectedDate.timeInMillis = dateInMillis
            updateDateDisplay()
        }
    }

    private fun setupClickListeners() {
        btnAddTask.setOnClickListener {
            showTaskDialog(null)
        }

        imgProfile.setOnClickListener {
            (activity as? MainActivity)?.navigateToProfile()
        }

        todayTextView.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                selectedDate.set(year, month, dayOfMonth)
                viewModel.setSelectedDate(selectedDate.timeInMillis)
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateDisplay() {
        val dateText = when {
            isToday(selectedDate) -> "Today"
            isTomorrow(selectedDate) -> "Tomorrow"
            isYesterday(selectedDate) -> "Yesterday"
            else -> dateFormatter.format(selectedDate.time)
        }
        todayTextView.text = dateText
    }

    private fun updateTasksList(tasks: List<Task>) {
        val baseText = if (todayTextView.text.contains("(")) {
            todayTextView.text.toString().substringBefore(" (")
        } else {
            todayTextView.text.toString()
        }
        todayTextView.text = "$baseText (${tasks.size} tasks)"
        taskAdapter.updateTasks(tasks)
    }

    private fun showTaskDialog(existingTask: Task?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_task, null)

        val etTaskName = dialogView.findViewById<EditText>(R.id.etTaskName)
        val btnDate = dialogView.findViewById<Button>(R.id.btnPickDate)
        val btnTime = dialogView.findViewById<Button>(R.id.btnPickTime)

        // Using a central calendar to build the absolute exact millisecond for the Alarm
        val alarmCalendar = Calendar.getInstance()
        var selectedDateStr = ""
        var selectedTimeStr = ""

        if (existingTask != null) {
            etTaskName.setText(existingTask.title)
            alarmCalendar.timeInMillis = existingTask.date
            selectedDateStr = dateFormatter.format(alarmCalendar.time)
            btnDate.text = selectedDateStr
            selectedTimeStr = existingTask.time
            btnTime.text = selectedTimeStr
        } else {
            // Default to the currently viewed day in the Tasks tab, but current time of day
            alarmCalendar.set(Calendar.YEAR, selectedDate.get(Calendar.YEAR))
            alarmCalendar.set(Calendar.MONTH, selectedDate.get(Calendar.MONTH))
            alarmCalendar.set(Calendar.DAY_OF_MONTH, selectedDate.get(Calendar.DAY_OF_MONTH))
            selectedDateStr = dateFormatter.format(alarmCalendar.time)
            btnDate.text = selectedDateStr
        }

        btnDate.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    alarmCalendar.set(Calendar.YEAR, year)
                    alarmCalendar.set(Calendar.MONTH, month)
                    alarmCalendar.set(Calendar.DAY_OF_MONTH, day)
                    selectedDateStr = dateFormatter.format(alarmCalendar.time)
                    btnDate.text = selectedDateStr
                },
                alarmCalendar.get(Calendar.YEAR),
                alarmCalendar.get(Calendar.MONTH),
                alarmCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        btnTime.setOnClickListener {
            TimePickerDialog(
                requireContext(),
                { _, hour, minute ->
                    alarmCalendar.set(Calendar.HOUR_OF_DAY, hour)
                    alarmCalendar.set(Calendar.MINUTE, minute)
                    alarmCalendar.set(Calendar.SECOND, 0)

                    val amPm = if (hour >= 12) "PM" else "AM"
                    val hour12 = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour
                    val minStr = String.format(Locale.getDefault(), "%02d", minute)
                    selectedTimeStr = "$hour12:$minStr $amPm"
                    btnTime.text = selectedTimeStr
                },
                alarmCalendar.get(Calendar.HOUR_OF_DAY),
                alarmCalendar.get(Calendar.MINUTE),
                false
            ).show()
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (existingTask == null) "New Task" else "Edit Task")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val taskText = etTaskName.text.toString().trim()

                if (taskText.isEmpty()) {
                    Toast.makeText(requireContext(), "Task name is required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (selectedTimeStr.isEmpty()) {
                    selectedTimeStr = "12:00 PM" // Default fallback
                    alarmCalendar.set(Calendar.HOUR_OF_DAY, 12)
                    alarmCalendar.set(Calendar.MINUTE, 0)
                    alarmCalendar.set(Calendar.SECOND, 0)
                }

                // The exact precise time marker combining Date and Time!
                val exactTimeInMillis = alarmCalendar.timeInMillis

                if (existingTask == null) {
                    val newTask = Task(
                        title = taskText,
                        description = "",
                        date = exactTimeInMillis,
                        time = selectedTimeStr,
                        priority = "Medium",
                        category = "General",
                        isCompleted = false
                    )
                    viewModel.addTask(newTask, requireContext())
                    Snackbar.make(requireView(), "Task added: $taskText", Snackbar.LENGTH_SHORT).show()
                } else {
                    val updatedTask = existingTask.copy(
                        title = taskText,
                        date = exactTimeInMillis,
                        time = selectedTimeStr
                    )
                    viewModel.updateTask(updatedTask, requireContext())
                    Snackbar.make(requireView(), "Task updated", Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun isToday(calendar: Calendar): Boolean {
        val today = Calendar.getInstance()
        return calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
    }

    private fun isTomorrow(calendar: Calendar): Boolean {
        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        return calendar.get(Calendar.YEAR) == tomorrow.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == tomorrow.get(Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(calendar: Calendar): Boolean {
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        return calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)
    }
}