package com.example.decena

import android.app.AlertDialog
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.util.Locale

class TimerFragment : Fragment() {

    private lateinit var tvTimer: TextView
    private lateinit var tvPreset: TextView
    private lateinit var tvCycles: TextView
    private lateinit var tvIntervals: TextView
    private lateinit var iconStart: ImageView
    private lateinit var btnStartContainer: View

    // Logic Variables
    private var countDownTimer: CountDownTimer? = null
    private var isRunning = false
    private var initialTimeInMillis: Long = 5 * 60 * 1000 // Default 5 mins
    private var timeLeftInMillis: Long = initialTimeInMillis

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_timer, container, false)

        // Bind Views
        tvTimer = view.findViewById(R.id.tvTimer)
        tvPreset = view.findViewById(R.id.tvTimePreset)
        tvCycles = view.findViewById(R.id.tvCycles)
        tvIntervals = view.findViewById(R.id.tvIntervals)
        iconStart = view.findViewById(R.id.iconStart)
        btnStartContainer = view.findViewById(R.id.btnStartContainer)

        val btnEditTime = view.findViewById<View>(R.id.btnEditTime)
        val btnEditCycles = view.findViewById<View>(R.id.btnEditCycles)
        val btnEditIntervals = view.findViewById<View>(R.id.btnEditIntervals)

        // Initialize Display
        updateCountDownText()

        // --- BUTTON LISTENERS ---

        // 1. Play/Pause Button
        btnStartContainer.setOnClickListener {
            if (isRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }

        // 2. Edit Time (Dropdown Menu)
        btnEditTime.setOnClickListener {
            showTimePresetMenu(btnEditTime)
        }

        // 3. Edit Cycles (Number Input)
        btnEditCycles.setOnClickListener {
            showNumberInputDialog("Cycles", tvCycles)
        }

        // 4. Edit Intervals (Number Input)
        btnEditIntervals.setOnClickListener {
            showNumberInputDialog("Intervals", tvIntervals)
        }

        return view

        val profileIcon = view.findViewById<ImageView>(R.id.imgProfile)
        profileIcon.setOnClickListener {
            (activity as? MainActivity)?.navigateToProfile()
        }
    }

    // --- TIMER LOGIC ---

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateCountDownText()
            }

            override fun onFinish() {
                isRunning = false
                iconStart.setImageResource(android.R.drawable.ic_media_play)
                timeLeftInMillis = 0
                updateCountDownText()
                Toast.makeText(context, "Timer Finished!", Toast.LENGTH_SHORT).show()
            }
        }.start()

        isRunning = true
        iconStart.setImageResource(android.R.drawable.ic_media_pause) // Change to Pause Icon
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        isRunning = false
        iconStart.setImageResource(android.R.drawable.ic_media_play) // Change back to Play Icon
    }

    private fun updateCountDownText() {
        val hours = (timeLeftInMillis / 1000) / 3600
        val minutes = ((timeLeftInMillis / 1000) % 3600) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
        tvTimer.text = timeFormatted
    }

    // --- POPUP MENUS & DIALOGS ---

    private fun showTimePresetMenu(view: View) {
        val popup = PopupMenu(context, view)
        // Add options: Title
        popup.menu.add("5 minutes")
        popup.menu.add("10 minutes")
        popup.menu.add("25 minutes (Pomodoro)")
        popup.menu.add("60 minutes")

        popup.setOnMenuItemClickListener { item ->
            tvPreset.text = item.title

            // Set the time based on selection
            val minutes = when (item.title) {
                "5 minutes" -> 5
                "10 minutes" -> 10
                "25 minutes (Pomodoro)" -> 25
                "60 minutes" -> 60
                else -> 5
            }

            // Reset timer with new time
            pauseTimer()
            initialTimeInMillis = (minutes * 60 * 1000).toLong()
            timeLeftInMillis = initialTimeInMillis
            updateCountDownText()
            true
        }
        popup.show()
    }

    private fun showNumberInputDialog(title: String, targetTextView: TextView) {
        val input = EditText(context)
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        input.setPadding(50, 50, 50, 50)
        input.hint = "Enter number"

        AlertDialog.Builder(context)
            .setTitle("Set $title")
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                val text = input.text.toString()
                if (text.isNotEmpty()) {
                    targetTextView.text = text
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}