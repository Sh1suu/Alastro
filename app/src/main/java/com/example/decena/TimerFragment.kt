package com.example.decena

import android.app.AlertDialog
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
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
import androidx.lifecycle.ViewModelProvider
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

    // Get the shared Timer Brain
    private lateinit var timerViewModel: TimerViewModel

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

        // Initialize ViewModel scoped to the entire Activity so it survives tab switching!
        timerViewModel = ViewModelProvider(requireActivity()).get(TimerViewModel::class.java)

        setupObservers()

        // Button Listeners
        btnStartContainer.setOnClickListener {
            timerViewModel.toggleTimer()
        }

        btnReset.setOnClickListener {
            timerViewModel.resetTimer()
            Toast.makeText(context, "Timer Reset", Toast.LENGTH_SHORT).show()
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

    private fun setupObservers() {
        timerViewModel.timeLeftInMillis.observe(viewLifecycleOwner) { millis ->
            updateCountDownText(millis)
        }

        timerViewModel.isRunning.observe(viewLifecycleOwner) { isRunning ->
            if (isRunning) {
                iconStart.setImageResource(android.R.drawable.ic_media_pause)
            } else {
                iconStart.setImageResource(android.R.drawable.ic_media_play)
            }
        }

        timerViewModel.isWorkSession.observe(viewLifecycleOwner) { isWork ->
            if (isWork) {
                timerRootLayout.setBackgroundColor(Color.parseColor("#A61D1D")) // Red
            } else {
                timerRootLayout.setBackgroundColor(Color.parseColor("#2E7D32")) // Green
            }
        }

        timerViewModel.totalCycles.observe(viewLifecycleOwner) { total ->
            tvCycles.text = total.toString()
        }

        // Sync interval and initial preset texts perfectly when fragment reloads
        tvIntervals.text = timerViewModel.intervalMinutes.toString()
        val mins = timerViewModel.initialTimeInMillis / 60000
        val secs = (timerViewModel.initialTimeInMillis % 60000) / 1000
        tvPreset.text = String.format(Locale.getDefault(), "%02d:%02d", mins, secs)

        timerViewModel.timerEvent.observe(viewLifecycleOwner) { event ->
            event?.let {
                playAlarmSound()
                when (it) {
                    TimerViewModel.TimerEvent.WORK_FINISHED -> {
                        Toast.makeText(context, "Break Time!", Toast.LENGTH_SHORT).show()
                    }
                    TimerViewModel.TimerEvent.BREAK_FINISHED -> {
                        val cycle = timerViewModel.currentCycle.value ?: 1
                        Toast.makeText(context, "Focus Time! Cycle $cycle", Toast.LENGTH_SHORT).show()
                    }
                    TimerViewModel.TimerEvent.ALL_CYCLES_COMPLETED -> {
                        Toast.makeText(context, "All cycles complete!", Toast.LENGTH_LONG).show()
                    }
                }
                timerViewModel.clearEvent() // Reset event so it doesn't trigger twice
            }
        }
    }

    private fun updateCountDownText(millis: Long) {
        val hours = (millis / 1000) / 3600
        val minutes = ((millis / 1000) % 3600) / 60
        val seconds = (millis / 1000) % 60
        tvTimer.text = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun playAlarmSound() {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(context, R.raw.alarm_sound)
        mediaPlayer?.start()
    }

    private fun showTimePresetMenu() {
        val context = requireContext()
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.HORIZONTAL
        layout.gravity = Gravity.CENTER
        layout.setPadding(50, 50, 50, 50)

        val minPicker = NumberPicker(context).apply {
            minValue = 0
            maxValue = 99
            value = (timerViewModel.initialTimeInMillis / 60000).toInt()
        }

        val secPicker = NumberPicker(context).apply {
            minValue = 0
            maxValue = 59
            value = ((timerViewModel.initialTimeInMillis % 60000) / 1000).toInt()
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
                    timerViewModel.setInitialTime(totalMillis)
                    tvPreset.text = String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
                    timerViewModel.resetTimer()
                } else {
                    Toast.makeText(context, "Timer must be greater than 0", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showNumberInputDialog(title: String, target: TextView) {
        val context = requireContext()
        val picker = NumberPicker(context).apply {
            if (title == "Cycles") {
                minValue = 1
                maxValue = 20
                value = timerViewModel.totalCycles.value ?: 4
            } else {
                minValue = 1
                maxValue = 60
                value = timerViewModel.intervalMinutes
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

                if (title == "Cycles") {
                    timerViewModel.setTotalCycles(selectedValue)
                } else {
                    timerViewModel.setIntervalMinutes(selectedValue)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        // Notice we do NOT cancel the timer here anymore!
        // The ViewModel keeps it alive.
    }
}