package com.example.decena

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(
    private var tasks: List<Task>,
    private val onTaskCheckedListener: (Task, Boolean) -> Unit,
    private val onTaskEditListener: (Task) -> Unit,
    private val onTaskDeleteListener: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cbTask: CheckBox = itemView.findViewById(R.id.cbTask)
        val tvTaskTitle: TextView = itemView.findViewById(R.id.tvTaskTitle)
        val tvTaskDate: TextView = itemView.findViewById(R.id.tvTaskDate)
        val imgMore: ImageView = itemView.findViewById(R.id.imgMore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task_row, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]

        holder.tvTaskTitle.text = task.title
        holder.tvTaskDate.text = formatTaskDateTime(task.date, task.time)
        holder.cbTask.isChecked = task.isCompleted

        if (task.isCompleted) {
            holder.tvTaskTitle.paintFlags = holder.tvTaskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.tvTaskTitle.alpha = 0.5f
            holder.tvTaskDate.alpha = 0.5f
        } else {
            holder.tvTaskTitle.paintFlags = holder.tvTaskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.tvTaskTitle.alpha = 1.0f
            holder.tvTaskDate.alpha = 1.0f
        }

        // Remove previous listener to avoid memory leaks
        holder.cbTask.setOnCheckedChangeListener(null)
        holder.cbTask.setOnCheckedChangeListener { _, isChecked ->
            onTaskCheckedListener(task, isChecked)
        }

        holder.imgMore.setOnClickListener { view ->
            showPopupMenu(view, task, holder.adapterPosition)
        }
    }

    private fun showPopupMenu(anchor: View, task: Task, position: Int) {
        val popup = PopupMenu(anchor.context, anchor)
        popup.menu.add("Edit")
        popup.menu.add("Delete")
        popup.menu.add(if (task.isCompleted) "Mark Pending" else "Mark Complete")

        popup.setOnMenuItemClickListener { item ->
            when (item.title) {
                "Edit" -> {
                    onTaskEditListener(task)
                    true
                }
                "Delete" -> {
                    onTaskDeleteListener(task)
                    true
                }
                "Mark Complete", "Mark Pending" -> {
                    onTaskCheckedListener(task, !task.isCompleted)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    override fun getItemCount() = tasks.size

    fun updateTasks(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged() // Force a complete refresh
    }

    private fun formatTaskDateTime(dateInMillis: Long, time: String): String {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = dateInMillis
        }

        val today = Calendar.getInstance()
        val tomorrow = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
        }
        val yesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }

        return when {
            isSameDay(calendar, today) -> "Today, $time"
            isSameDay(calendar, tomorrow) -> "Tomorrow, $time"
            isSameDay(calendar, yesterday) -> "Yesterday, $time"
            else -> {
                val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                "${dateFormat.format(calendar.time)}, $time"
            }
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}