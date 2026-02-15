package com.example.decena

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.decena.databinding.ActivityMainBinding
// Note: If 'databinding' is red, we might need to enable it in build.gradle (see below)

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Load the Dashboard Fragment by default when app starts
        loadFragment(DashboardFragment())
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        // 'fragmentContainer' is the ID we will give to the empty space in activity_main.xml
        transaction.replace(R.id.fragmentContainer, fragment)
        transaction.commit()
    }
}