package com.example.decena

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView // Don't forget this import!
import androidx.fragment.app.Fragment

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

        // 1. Find the profile icon using the ID we added in Step 1
        val profileIcon = view.findViewById<ImageView>(R.id.imgProfile)

        // 2. Set the click listener
        profileIcon.setOnClickListener {
            // This calls the helper function we added to MainActivity
            (activity as? MainActivity)?.navigateToProfile()
        }
    }
}