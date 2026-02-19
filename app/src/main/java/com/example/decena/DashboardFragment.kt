package com.example.decena

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {

    private lateinit var btnMonthSelector: TextView
    private lateinit var imgProfile: ImageView

    private lateinit var tvGreeting: TextView

    private lateinit var tasksContainer: LinearLayout
    private lateinit var scrollView: ScrollView
    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var viewModel: TasksViewModel
    private lateinit var databaseHelper: TaskDatabaseHelper
    private lateinit var sharedViewModel: SharedViewModel

    private var selectedDate: Calendar = Calendar.getInstance()
    private val monthFormatter = SimpleDateFormat("MMMM", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {

            initializeViews(view)
            setupRecyclerView()
            setupViewModels()
            setupClickListeners(view)
            updateGreetingAndQuotes() // Now this will find "Francis"!

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun initializeViews(view: View) {
        btnMonthSelector = view.findViewById(R.id.btnMonthSelector)
        imgProfile = view.findViewById(R.id.imgProfile)

        tvGreeting = view.findViewById(R.id.tvGreeting)


        // Find ScrollView
        scrollView = view.findViewById(R.id.scrollView)

        // Get the LinearLayout inside ScrollView
        tasksContainer = scrollView.getChildAt(0) as LinearLayout

        // Clear the static includes
        tasksContainer.removeAllViews()
    }

    private fun setupRecyclerView() {
        recyclerView = RecyclerView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            layoutManager = LinearLayoutManager(requireContext())
        }

        tasksContainer.addView(recyclerView)

        taskAdapter = TaskAdapter(
            tasks = emptyList(),
            onTaskCheckedListener = { task, isChecked ->
                viewModel.updateTaskCompletion(task.id, isChecked)
            },
            onTaskEditListener = { task ->
                // Navigate to Tasks tab for editing
                (activity as? MainActivity)?.navigateToTasks()
            },
            onTaskDeleteListener = { task ->
                viewModel.deleteTask(task)
                Snackbar.make(requireView(), "Task deleted", Snackbar.LENGTH_SHORT).show()
            }
        )
        recyclerView.adapter = taskAdapter
    }

    private fun setupViewModels() {
        databaseHelper = TaskDatabaseHelper(requireContext())
        val factory = TasksViewModelFactory(databaseHelper)
        viewModel = ViewModelProvider(requireActivity(), factory).get(TasksViewModel::class.java)

        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        viewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            taskAdapter.updateTasks(tasks)
        }

        viewModel.selectedDate.observe(viewLifecycleOwner) { dateInMillis ->
            selectedDate.timeInMillis = dateInMillis
            updateMonthDisplay()
        }

        sharedViewModel.currentMonth.observe(viewLifecycleOwner) { newMonth ->
            btnMonthSelector.text = newMonth
        }
    }

    private fun setupClickListeners(view: View) {
        btnMonthSelector.setOnClickListener {
            try {
                (activity as? MainActivity)?.navigateToCalendar()
            } catch (e: Exception) {
                Toast.makeText(context, "Calendar clicked", Toast.LENGTH_SHORT).show()
            }
        }

        imgProfile.setOnClickListener {
            try {
                (activity as? MainActivity)?.navigateToProfile()
            } catch (e: Exception) {
                Toast.makeText(context, "Profile clicked", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateMonthDisplay() {
        val month = monthFormatter.format(selectedDate.time)
        sharedViewModel.setMonth(month)
    }

    private fun updateGreetingAndQuotes() {
        // 1. Get the "SharedPreferences" (The app's mini storage)
        // We use a file named "UserPrefs"
        val sharedPref = requireActivity().getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)

        // 2. Try to find a name saved under the key "saved_name"
        // If no name is found (like the first time you run it), use "Student" as default
        val userName = sharedPref.getString("saved_name", "Student")

        // 3. Get the current time
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        // 4. Create the greeting logic
        val greetingText = when (hour) {
            in 0..11 -> "Good Morning, $userName! ☀️"
            in 12..17 -> "Good Afternoon, $userName! 🌤️"
            else -> "Good Evening, $userName! 🌙"
        }

        // 5. Set the text
        tvGreeting.text = greetingText

    }

    override fun onResume() {
        super.onResume()
        // This forces the greeting to update every time you open this tab!
        updateGreetingAndQuotes()
    }
}