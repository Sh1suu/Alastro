package com.example.decena

import android.app.AlertDialog
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.util.Locale

class TimerFragment : Fragment() {
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var timerRootLayout: View
    private lateinit var tvTimer: TextView
    private lateinit var tvPreset: TextView
    private lateinit var tvCycles: TextView
    private lateinit var tvIntervals: TextView
    private lateinit var iconStart: ImageView
    private lateinit var btnStartContainer: View
    private lateinit var btnReset: ImageView

    // Logic Variables
    private var countDownTimer: CountDownTimer? = null
    private var isRunning = false
    private var initialTimeInMillis: Long = 5 * 60 * 1000
    private var timeLeftInMillis: Long = initialTimeInMillis
    private var isWorkSession = true
    private var currentCycle = 1
    private var totalCycles = 4

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_timer, container, false)

        // Bind Views
        timerRootLayout = view.findViewById(R.id.timerRootLayout)
        tvTimer = view.findViewById(R.id.tvTimer)
        tvPreset = view.findViewById(R.id.tvTimePreset)
        tvCycles = view.findViewById(R.id.tvCycles)
        tvIntervals = view.findViewById(R.id.tvIntervals)
        iconStart = view.findViewById(R.id.iconStart)
        btnStartContainer = view.findViewById(R.id.btnStartContainer)
        btnReset = view.findViewById(R.id.btnReset)
        val imgProfile = view.findViewById<ImageView>(R.id.imgProfile)

        // Initialize Display
        updateCountDownText()

        // Button Listeners
        btnStartContainer.setOnClickListener {
            if (isRunning) pauseTimer() else startTimer()
        }

        btnReset.setOnClickListener {
            resetTimer()
        }

        view.findViewById<View>(R.id.btnEditTime).setOnClickListener {
            showTimePresetMenu()
        }

        view.findViewById<View>(R.id.btnEditCycles).setOnClickListener {
            showNumberInputDialog("Cycles", tvCycles)
        }

        view.findViewById<View>(R.id.btnEditIntervals).setOnClickListener {
            showNumberInputDialog("Intervals", tvIntervals)
        }

        imgProfile.setOnClickListener {
            (activity as? MainActivity)?.navigateToProfile()
        }

        return view
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateCountDownText()
            }

            override fun onFinish() {
                playAlarmSound()
                if (isWorkSession) {
                    isWorkSession = false
                    startNextSession()
                } else {
                    if (currentCycle < totalCycles) {
                        currentCycle++
                        isWorkSession = true
                        startNextSession()
                    } else {
                        Toast.makeText(context, "All cycles complete!", Toast.LENGTH_LONG).show()
                        resetTimer()
                    }
                }
            }
        }.start()
        isRunning = true
        iconStart.setImageResource(android.R.drawable.ic_media_pause)
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        isRunning = false
        iconStart.setImageResource(android.R.drawable.ic_media_play)
    }

    private fun resetTimer() {
        countDownTimer?.cancel()
        isRunning = false
        isWorkSession = true
        currentCycle = 1
        timeLeftInMillis = initialTimeInMillis
        updateCountDownText()
        iconStart.setImageResource(android.R.drawable.ic_media_play)
        timerRootLayout.setBackgroundColor(Color.parseColor("#A61D1D"))
        Toast.makeText(context, "Timer Reset", Toast.LENGTH_SHORT).show()
    }

    private fun startNextSession() {
        val timerMillis: Long
        if (isWorkSession) {
            timerMillis = initialTimeInMillis
            timerRootLayout.setBackgroundColor(Color.parseColor("#A61D1D"))
            Toast.makeText(context, "Focus Time! Cycle $currentCycle", Toast.LENGTH_SHORT).show()
        } else {
            val intervalMinutes = tvIntervals.text.toString().toIntOrNull() ?: 5
            timerMillis = (intervalMinutes * 60 * 1000).toLong()
            timerRootLayout.setBackgroundColor(Color.parseColor("#2E7D32"))
            Toast.makeText(context, "Break Time!", Toast.LENGTH_SHORT).show()
        }
        runTimer(timerMillis)
    }

    private fun runTimer(timeInMillis: Long) {
        countDownTimer?.cancel()
        timeLeftInMillis = timeInMillis
        updateCountDownText()
        startTimer()
    }

    private fun updateCountDownText() {
        val hours = (timeLeftInMillis / 1000) / 3600
        val minutes = ((timeLeftInMillis / 1000) % 3600) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        tvTimer.text = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun playAlarmSound() {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(context, R.raw.alarm_sound)
        mediaPlayer?.start()
    }

    // --- MAIN TIMER PRESET (Minutes & Seconds Wheels) ---
    private fun showTimePresetMenu() {
        val context = requireContext()

        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.HORIZONTAL
        layout.gravity = Gravity.CENTER
        layout.setPadding(50, 50, 50, 50)

        val minPicker = NumberPicker(context).apply {
            minValue = 0
            maxValue = 99
            value = (initialTimeInMillis / 60000).toInt()
        }

        val secPicker = NumberPicker(context).apply {
            minValue = 0
            maxValue = 59
            value = ((initialTimeInMillis % 60000) / 1000).toInt()
        }

        val minLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            addView(TextView(context).apply { text = "Min" })
            addView(minPicker)
            setPadding(0, 0, 30, 0)
        }

        val secLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            addView(TextView(context).apply { text = "Sec" })
            addView(secPicker)
            setPadding(30, 0, 0, 0)
        }

        layout.addView(minLayout)
        layout.addView(secLayout)

        AlertDialog.Builder(context)
            .setTitle("Set Work Timer")
            .setView(layout)
            .setPositiveButton("Set") { _, _ ->
                val mins = minPicker.value
                val secs = secPicker.value
                val totalMillis = (mins * 60 * 1000L) + (secs * 1000L)

                if (totalMillis > 0) {
                    initialTimeInMillis = totalMillis
                    tvPreset.text = String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
                    resetTimer()
                } else {
                    Toast.makeText(context, "Timer must be greater than 0", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // --- CYCLES & INTERVALS PRESET (Scrollable Wheel) ---
    private fun showNumberInputDialog(title: String, target: TextView) {
        val context = requireContext()

        val picker = NumberPicker(context).apply {
            if (title == "Cycles") {
                minValue = 1
                maxValue = 20
                // Pulls current number from text, defaults to 4
                value = target.text.toString().toIntOrNull() ?: 4
            } else {
                // Intervals (Break time in minutes)
                minValue = 1
                maxValue = 60
                // Pulls current number from text, defaults to 5
                value = target.text.toString().toIntOrNull() ?: 5
            }
        }

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(50, 50, 50, 50)
            addView(picker)
        }

        AlertDialog.Builder(context)
            .setTitle("Set $title")
            .setView(layout)
            .setPositiveButton("OK") { _, _ ->
                val selectedValue = picker.value
                target.text = selectedValue.toString()

                // Update the logic variable specifically for Cycles
                if (title == "Cycles") {
                    totalCycles = selectedValue
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }
}