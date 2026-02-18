package com.example.decena

import androidx.lifecycle.ViewModelProvider
import android.widget.TextView
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment

class DashboardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Just return the view here
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Get the Shared Storage Box (ViewModel)
        val sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        // 2. Find your Text View (The "February ▶" text)
        val monthSelector = view.findViewById<TextView>(R.id.btnMonthSelector)

        // 3. OBSERVE changes: When the data changes, update the text!
        sharedViewModel.currentMonth.observe(viewLifecycleOwner) { newMonth ->
            monthSelector.text = "$newMonth"
        }

        // --- NEW: CLICK LISTENER TO GO TO CALENDAR ---
        monthSelector.setOnClickListener {
            // This calls the helper function in MainActivity to switch tabs
            (activity as? MainActivity)?.navigateToCalendar()
        }

        // 4. Find the profile icon and set the click listener
        val profileIcon = view.findViewById<ImageView>(R.id.imgProfile)
        profileIcon.setOnClickListener {
            // Use the helper function we created in MainActivity
            (activity as? MainActivity)?.navigateToProfile()
        }
    }
}