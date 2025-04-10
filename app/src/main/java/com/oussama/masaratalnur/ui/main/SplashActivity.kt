package com.oussama.masaratalnur.ui.main

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.oussama.masaratalnur.ui.auth.AuthActivity
import com.oussama.masaratalnur.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen") // Suppress Lint warning for custom splash screen without recommended API < 31 handling (we'll keep it simple for now)
class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth // Declare FirebaseAuth instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // No need to set content view if it's just a brief check
        setContentView(R.layout.activity_splash)

        auth = Firebase.auth // Initialize Firebase Auth

        // Use lifecycleScope to launch a coroutine that's tied to the Activity's lifecycle
        lifecycleScope.launch {

            // Check authentication status
            if (auth.currentUser == null) {
                delay(1500) // Wait for 1.5 seconds (adjust as needed)

                Log.d("SplashActivity", "User not logged in. Navigating to AuthActivity (Placeholder: MainActivity).")
                startActivity(Intent(this@SplashActivity, AuthActivity::class.java))

            } else {
                delay(500) // Wait for 0.5 seconds (adjust as needed)

                // Logged in - Go to MainActivity
                Log.d("SplashActivity", "User logged in. Navigating to MainActivity.")
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            }
            // Finish SplashActivity so the user can't navigate back to it
            finish()
        }
    }
}