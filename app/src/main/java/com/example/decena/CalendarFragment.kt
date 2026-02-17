package com.example.decena  // <--- UPDATED: This now matches your project!

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class CalendarFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // The 'R' should turn from Red to Purple/Black now
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }
}