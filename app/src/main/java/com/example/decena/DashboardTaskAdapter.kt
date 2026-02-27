package com.example.decena

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*

class DashboardTaskAdapter(private var tasks: List<Task>) : RecyclerView.Adapter<DashboardTaskAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateCard: MaterialCardView = view.findViewById(R.id.dateCard)
        val dateColorHeader: LinearLayout = view.findViewById(R.id.dateColorHeader)
        val tvDayName: TextView = view.findViewById(R.id.tvDayName)
        val tvDayNumber: TextView = view.findViewById(R.id.tvDayNumber)
        val tvTaskTitle: TextView = view.findViewById(R.id.tvTaskTitle)
        val tvTaskTime: TextView = view.findViewById(R.id.tvTaskTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_timeline_task, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = tasks[position]

        // Formatter for "Mon" and "8"
        val dayNameFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val dayNumFormat = SimpleDateFormat("d", Locale.getDefault())
        val date = Date(task.date)

        holder.tvDayName.text = dayNameFormat.format(date)
        holder.tvDayNumber.text = dayNumFormat.format(date)
        holder.tvTaskTitle.text = task.title
        holder.tvTaskTime.text = task.time

        // 1. Grouping Logic: Hide date box if the previous task is on the exact same day
        val isFirstOfDate = if (position == 0) true else {
            !isSameDay(task.date, tasks[position - 1].date)
        }

        if (isFirstOfDate) {
            holder.dateCard.visibility = View.VISIBLE
        } else {
            holder.dateCard.visibility = View.INVISIBLE // Hides it but keeps the empty space!
        }

        // 2. Highlighting Logic: Make the closest upcoming date RED
        // Since the list is sorted, the very first task in the list is the closest date
        val closestDate = tasks.firstOrNull()?.date ?: 0L
        val isClosestDate = isSameDay(task.date, closestDate)

        if (isClosestDate) {
            holder.dateColorHeader.setBackgroundColor(Color.parseColor("#A61D1D")) // accent_red
            holder.dateCard.strokeColor = Color.parseColor("#A61D1D")
        } else {
            holder.dateColorHeader.setBackgroundColor(Color.parseColor("#A9A9A9")) // Grey
            holder.dateCard.strokeColor = Color.parseColor("#A9A9A9")
        }
    }

    override fun getItemCount() = tasks.size

    fun updateTasks(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }

    private fun isSameDay(time1: Long, time2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = time1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = time2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}