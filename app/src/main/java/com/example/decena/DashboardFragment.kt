package com.example.decena

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

        // Find the profile icon and set the click listener
        val profileIcon = view.findViewById<ImageView>(R.id.imgProfile)
        profileIcon.setOnClickListener {
            // Use the helper function we created in MainActivity
            (activity as? MainActivity)?.navigateToProfile()
        }
    }
}