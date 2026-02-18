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

        // --- Existing Profile Logic ---
        val profileIcon = view.findViewById<ImageView>(R.id.imgProfile)
        profileIcon.setOnClickListener {
            (activity as? MainActivity)?.navigateToProfile()
        }

        // --- NEW: Shared ViewModel Logic ---
        // 1. Get the Shared ViewModel
        val sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        // 2. Find the CalendarView (Make sure your XML has this ID!)
        val calendarView = view.findViewById<CalendarView>(R.id.calendarView)

        // 3. Listen for date changes
        calendarView?.setOnDateChangeListener { _, year, month, dayOfMonth ->
            // Convert the month number (0-11) to a name (January, February...)
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth)

            val monthName = SimpleDateFormat("MMMM", Locale.getDefault()).format(cal.time)

            // Update the Shared ViewModel!
            sharedViewModel.setMonth(monthName)
        }
    }
}