package com.example.decena

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tasks, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            initializeViews(view)
            setupRecyclerView()
            setupViewModel()
            setupClickListeners(view)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun initializeViews(view: View) {
        tasksContainer = view.findViewById(R.id.tasksContainer)
        btnAddTask = view.findViewById(R.id.btnAddTask)
        imgProfile = view.findViewById(R.id.imgProfile)

        // Get the first child (Today text)
        if (tasksContainer.childCount > 0) {
            todayTextView = tasksContainer.getChildAt(0) as TextView
        } else {
            todayTextView = TextView(requireContext()).apply {
                text = "Today"
                textSize = 18f
                setTextColor(resources.getColor(android.R.color.black, null))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            tasksContainer.addView(todayTextView)
        }
    }

    private fun setupRecyclerView() {
        recyclerView = RecyclerView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            layoutManager = LinearLayoutManager(requireContext())
        }

        // Clear all views and add back the Today text and RecyclerView
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
                viewModel.deleteTask(task)
                Snackbar.make(requireView(), "Task deleted", Snackbar.LENGTH_SHORT).show()
            }
        )
        recyclerView.adapter = taskAdapter
    }

    private fun setupViewModel() {
        databaseHelper = TaskDatabaseHelper(requireContext())
        val factory = TasksViewModelFactory(databaseHelper)
        viewModel = ViewModelProvider(requireActivity(), factory).get(TasksViewModel::class.java)

        // Observe tasks - this will trigger whenever tasks change
        viewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            Log.d("TasksFragment", "Received ${tasks.size} tasks")
            updateTasksList(tasks)
        }

        // Observe selected date
        viewModel.selectedDate.observe(viewLifecycleOwner) { dateInMillis ->
            selectedDate.timeInMillis = dateInMillis
            updateDateDisplay()
            Log.d("TasksFragment", "Date changed to: $dateInMillis")
        }
    }

    private fun setupClickListeners(view: View) {
        btnAddTask.setOnClickListener {
            showTaskDialog(null)
        }

        imgProfile.setOnClickListener {
            try {
                (activity as? MainActivity)?.navigateToProfile()
            } catch (e: Exception) {
                Toast.makeText(context, "Profile clicked", Toast.LENGTH_SHORT).show()
            }
        }

        todayTextView.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                selectedDate.set(year, month, dayOfMonth)
                viewModel.setSelectedDate(selectedDate.timeInMillis)
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
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
        // Update the "Today" text with task count
        val baseText = if (todayTextView.text.contains("(")) {
            todayTextView.text.toString().substringBefore(" (")
        } else {
            todayTextView.text.toString()
        }
        todayTextView.text = "$baseText (${tasks.size} tasks)"

        // Update adapter
        taskAdapter.updateTasks(tasks)

        // Force RecyclerView to refresh
        recyclerView.post {
            taskAdapter.notifyDataSetChanged()
        }

        Log.d("TasksFragment", "UI updated with ${tasks.size} tasks")
    }

    private fun showTaskDialog(existingTask: Task?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_task, null)

        val etTaskName = dialogView.findViewById<EditText>(R.id.etTaskName)
        val btnDate = dialogView.findViewById<Button>(R.id.btnPickDate)
        val btnTime = dialogView.findViewById<Button>(R.id.btnPickTime)

        var selectedDateInMillis = selectedDate.timeInMillis
        var selectedDateStr = ""
        var selectedTimeStr = ""

        if (existingTask != null) {
            etTaskName.setText(existingTask.title)

            val calendar = Calendar.getInstance().apply {
                timeInMillis = existingTask.date
            }
            selectedDateInMillis = existingTask.date
            selectedDateStr = dateFormatter.format(calendar.time)
            btnDate.text = selectedDateStr
            selectedTimeStr = existingTask.time
            btnTime.text = selectedTimeStr
        } else {
            // For new tasks, set default date to selected date
            selectedDateStr = dateFormatter.format(Date(selectedDateInMillis))
            btnDate.text = selectedDateStr
        }

        btnDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, year, month, day ->
                calendar.set(year, month, day)
                selectedDateInMillis = calendar.timeInMillis
                selectedDateStr = dateFormatter.format(calendar.time)
                btnDate.text = selectedDateStr
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(requireContext(), { _, hour, minute ->
                val amPm = if (hour >= 12) "PM" else "AM"
                val hour12 = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour
                val minStr = String.format("%02d", minute)
                selectedTimeStr = "$hour12:$minStr $amPm"
                btnTime.text = selectedTimeStr
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
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

                if (selectedDateStr.isEmpty()) {
                    selectedDateStr = dateFormatter.format(Date(selectedDateInMillis))
                }

                if (selectedTimeStr.isEmpty()) {
                    selectedTimeStr = "12:00 PM"
                }

                if (existingTask == null) {
                    // CREATE NEW TASK
                    val newTask = Task(
                        title = taskText,
                        description = "",
                        date = selectedDateInMillis,
                        time = selectedTimeStr,
                        priority = "Medium",
                        category = "General",
                        isCompleted = false
                    )
                    Log.d("TasksFragment", "Adding new task: $taskText")
                    viewModel.addTask(newTask)
                    Snackbar.make(requireView(), "Task added: $taskText", Snackbar.LENGTH_SHORT).show()
                } else {
                    // UPDATE EXISTING TASK
                    val updatedTask = existingTask.copy(
                        title = taskText,
                        date = selectedDateInMillis,
                        time = selectedTimeStr
                    )
                    Log.d("TasksFragment", "Updating task: $taskText")
                    viewModel.deleteTask(existingTask)
                    viewModel.addTask(updatedTask)
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
        val tomorrow = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
        }
        return calendar.get(Calendar.YEAR) == tomorrow.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == tomorrow.get(Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(calendar: Calendar): Boolean {
        val yesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }
        return calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)
    }
}