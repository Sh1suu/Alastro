package com.example.decena

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.decena.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Load Home by default
        loadFragment(DashboardFragment())
        updateNavState(1) // 1 = Home

        // 2. Click Listeners
        binding.btnNavHome.setOnClickListener {
            loadFragment(DashboardFragment())
            updateNavState(1)
        }

        binding.btnNavCalendar.setOnClickListener {
            loadFragment(CalendarFragment())
            updateNavState(2)
        }

        // These don't have fragments yet, but we can animate the buttons!
        // 3. Tasks Button (Checkbox Icon)
        binding.btnNavTasks.setOnClickListener {
            loadFragment(TasksFragment()) // <--- Load the new fragment!
            updateNavState(3)
        }
        binding.btnNavTimer.setOnClickListener { updateNavState(4) }
    }

    private fun updateNavState(selected: Int) {
        // Reset all to default white text, hidden background
        val white = ContextCompat.getColor(this, android.R.color.white)
        val red = ContextCompat.getColor(this, R.color.accent_red)

        // Reset All
        binding.bgHomeActive.visibility = View.GONE
        binding.bgCalendarActive.visibility = View.GONE
        binding.bgTasksActive.visibility = View.GONE
        binding.bgTimerActive.visibility = View.GONE

        binding.iconHome.setColorFilter(white)
        binding.iconCalendar.setColorFilter(white)
        binding.iconTasks.setColorFilter(white)
        binding.iconTimer.setColorFilter(white)

        // Activate Selected
        when (selected) {
            1 -> {
                binding.bgHomeActive.visibility = View.VISIBLE
                binding.iconHome.setColorFilter(red)
            }
            2 -> {
                binding.bgCalendarActive.visibility = View.VISIBLE
                binding.iconCalendar.setColorFilter(red)
            }
            3 -> {
                binding.bgTasksActive.visibility = View.VISIBLE
                binding.iconTasks.setColorFilter(red)
            }
            4 -> {
                binding.bgTimerActive.visibility = View.VISIBLE
                binding.iconTimer.setColorFilter(red)
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainer, fragment)
        transaction.commit()
    }
}