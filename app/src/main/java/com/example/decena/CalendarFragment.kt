package com.example.decena

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider // Needed for SharedViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Existing profile icon logic
        val profileIcon = view.findViewById<ImageView>(R.id.imgProfile)
        profileIcon.setOnClickListener {
            (activity as? MainActivity)?.navigateToProfile()
        }

        // Get SharedViewModel and TasksViewModel
        val sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        val tasksViewModel = ViewModelProvider(requireActivity())[TasksViewModel::class.java]

        val calendarView = view.findViewById<CalendarView>(R.id.calendarView)
        calendarView?.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth)

            // Update month name for dashboard header
            val monthName = SimpleDateFormat("MMMM", Locale.getDefault()).format(cal.time)
            sharedViewModel.setMonth(monthName)

            // Update selected date so tasks refresh for this day
            tasksViewModel.setSelectedDate(cal.timeInMillis)
        }
    }
}