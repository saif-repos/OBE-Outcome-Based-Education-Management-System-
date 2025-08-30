package com.example.obe_mngt_sys.ACTIVITIES

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.obe_mngt_sys.R
import com.example.obe_mngt_sys.ACTIVITIES.Login

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        // Delay for 4 milliseconds before navigating to the login screen
        Handler(Looper.getMainLooper()).postDelayed({
            // Start the LoginActivity
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish() // Finish the splash screen activity
        }, 5000) // 5000 milliseconds = 5 seconds



    }
}
