package com.example.decena

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar

class DashboardFragment : Fragment() {

    private lateinit var btnMonthSelector: TextView
    private lateinit var imgProfile: ImageView
    private lateinit var tvGreeting: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var dashboardAdapter: DashboardTaskAdapter
    private lateinit var viewModel: TasksViewModel
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            initializeViews(view)
            setupRecyclerView()
            setupViewModels()
            setupClickListeners()
            updateGreetingAndQuotes() // Call greeting on creation
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun initializeViews(view: View) {
        btnMonthSelector = view.findViewById(R.id.btnMonthSelector)
        imgProfile = view.findViewById(R.id.imgProfile)
        tvGreeting = view.findViewById(R.id.tvGreeting)
        recyclerView = view.findViewById(R.id.dashboardRecyclerView)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        // Start with an empty list, ViewModel will populate it instantly
        dashboardAdapter = DashboardTaskAdapter(emptyList())
        recyclerView.adapter = dashboardAdapter
    }

    private fun setupViewModels() {
        val databaseHelper = TaskDatabaseHelper(requireContext())
        val factory = TasksViewModelFactory(databaseHelper)
        viewModel = ViewModelProvider(requireActivity(), factory).get(TasksViewModel::class.java)
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        // Observe ALL upcoming tasks for the Timeline
        viewModel.allTasks.observe(viewLifecycleOwner) { tasks ->
            // Filter out completed tasks so the dashboard only shows what's pending
            val pendingTasks = tasks.filter { !it.isCompleted }
            dashboardAdapter.updateTasks(pendingTasks)
        }

        // Update the header text (e.g. "February >")
        sharedViewModel.currentMonth.observe(viewLifecycleOwner) { newMonth ->
            btnMonthSelector.text = "$newMonth >"
        }
    }

    private fun setupClickListeners() {
        btnMonthSelector.setOnClickListener {
            (activity as? MainActivity)?.navigateToCalendar()
        }

        imgProfile.setOnClickListener {
            (activity as? MainActivity)?.navigateToProfile()
        }
    }

    private fun updateGreetingAndQuotes() {
        // 1. Get the "SharedPreferences" (The app's mini storage)
        val sharedPref = requireActivity().getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)

        // 2. Try to find a name saved under the key "saved_name"
        // If no name is found, use "Student" as default
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