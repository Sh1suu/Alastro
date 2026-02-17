package com.example.decena

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // 1. Hide the top status bar/action bar
        supportActionBar?.hide()

        // 2. Setup the Animation (Inside onCreate)
        val imgLogo = findViewById<ImageView>(R.id.imgLogo)
        imgLogo.alpha = 0f
        imgLogo.animate().setDuration(1000).alpha(1f).start()

        // 3. Set the timer to switch to MainActivity
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 2000)
    } // Ensure this bracket closes onCreate
} // Ensure this bracket closes the Class