package com.example.decena

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import java.util.Calendar

class TasksFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tasks, container, false)
        val tasksContainer = view.findViewById<LinearLayout>(R.id.tasksContainer)
        val btnAddTask = view.findViewById<View>(R.id.btnAddTask)

        // Add some default tasks
        addNewTask(inflater, tasksContainer, "Study Math & Science", "Today, 4:00 PM")
        addNewTask(inflater, tasksContainer, "Clean the Bathroom", "Tomorrow, 10:00 AM")

        btnAddTask.setOnClickListener {
            showTaskDialog(inflater, tasksContainer, null) // null means "Create New"
        }
        // Find the profile icon by its ID
        val profileIcon = view.findViewById<ImageView>(R.id.imgProfile)
        profileIcon.setOnClickListener {
            // Use the cleaner helper function from MainActivity
            (activity as? MainActivity)?.navigateToProfile()
        }
        return view
    }

    // This function handles BOTH Adding new tasks and Editing old ones
    private fun showTaskDialog(
        inflater: LayoutInflater,
        container: LinearLayout,
        existingView: View? // If this is NOT null, we are editing!
    ) {
        val dialogView = inflater.inflate(R.layout.dialog_add_task, null)
        val etTaskName = dialogView.findViewById<EditText>(R.id.etTaskName)
        val btnDate = dialogView.findViewById<Button>(R.id.btnPickDate)
        val btnTime = dialogView.findViewById<Button>(R.id.btnPickTime)

        // Variables to store selected data
        var selectedDate = ""
        var selectedTime = ""

        // If Editing, pre-fill the data
        if (existingView != null) {
            val currentTitle = existingView.findViewById<TextView>(R.id.tvTaskTitle).text.toString()
            val currentDate = existingView.findViewById<TextView>(R.id.tvTaskDate).text.toString()
            etTaskName.setText(currentTitle)
            btnDate.text = "Change Date" // Simplified for now
            btnTime.text = "Change Time"
        }

        // 1. Date Picker Logic
        btnDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, year, month, day ->
                // Format: Jan 12, 2024
                selectedDate = "${getMonthName(month)} $day, $year"
                btnDate.text = selectedDate
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        // 2. Time Picker Logic
        btnTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(requireContext(), { _, hour, minute ->
                // Format: 14:30
                val amPm = if (hour >= 12) "PM" else "AM"
                val hour12 = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour
                val minStr = if (minute < 10) "0$minute" else "$minute"
                selectedTime = "$hour12:$minStr $amPm"
                btnTime.text = selectedTime
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
        }

        AlertDialog.Builder(context)
            .setTitle(if (existingView == null) "New Task" else "Edit Task")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val taskText = etTaskName.text.toString()
                val fullDateTime = "$selectedDate $selectedTime".trim()
                val finalDate = if (fullDateTime.isEmpty()) "No Deadline" else fullDateTime

                if (taskText.isNotEmpty()) {
                    if (existingView == null) {
                        // CREATE NEW
                        addNewTask(inflater, container, taskText, finalDate)
                    } else {
                        // UPDATE EXISTING
                        val tvTitle = existingView.findViewById<TextView>(R.id.tvTaskTitle)
                        val tvDate = existingView.findViewById<TextView>(R.id.tvTaskDate)
                        tvTitle.text = taskText
                        tvDate.text = finalDate
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addNewTask(inflater: LayoutInflater, container: LinearLayout, title: String, date: String) {
        val taskView = inflater.inflate(R.layout.item_task_row, container, false)

        val tvTitle = taskView.findViewById<TextView>(R.id.tvTaskTitle)
        val tvDate = taskView.findViewById<TextView>(R.id.tvTaskDate)
        val checkBox = taskView.findViewById<CheckBox>(R.id.cbTask)
        val imgMore = taskView.findViewById<ImageView>(R.id.imgMore)

        tvTitle.text = title
        tvDate.text = date

        // Strike-through logic
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                tvTitle.paintFlags = tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                tvTitle.alpha = 0.5f
            } else {
                tvTitle.paintFlags = tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                tvTitle.alpha = 1.0f
            }
        }

        // KEBAB MENU LOGIC (Edit / Delete)
        imgMore.setOnClickListener {
            val popup = PopupMenu(context, imgMore)
            popup.menu.add("Edit")
            popup.menu.add("Delete")

            popup.setOnMenuItemClickListener { item ->
                when (item.title) {
                    "Delete" -> {
                        container.removeView(taskView)
                    }
                    "Edit" -> {
                        showTaskDialog(inflater, container, taskView)
                    }
                }
                true
            }
            popup.show()
        }

        container.addView(taskView)
    }

    // Helper to get Month Name
    private fun getMonthName(month: Int): String {
        val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        return months[month]
    }
}