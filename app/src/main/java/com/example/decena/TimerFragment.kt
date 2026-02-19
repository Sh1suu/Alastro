package com.example.decena

import android.app.AlertDialog
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
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

    private fun showTimePresetMenu() {
        val input = EditText(context)
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        AlertDialog.Builder(context).setTitle("Set Minutes").setView(input)
            .setPositiveButton("Set") { _, _ ->
                val mins = input.text.toString().toIntOrNull() ?: 5
                tvPreset.text = "$mins minutes"
                initialTimeInMillis = (mins * 60 * 1000).toLong()
                resetTimer()
            }.show()
    }

    private fun showNumberInputDialog(title: String, target: TextView) {
        val input = EditText(context)
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        AlertDialog.Builder(context).setTitle("Set $title").setView(input)
            .setPositiveButton("OK") { _, _ ->
                target.text = input.text.toString()
                if (title == "Cycles") totalCycles = input.text.toString().toIntOrNull() ?: 4
            }.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }
}