package com.oussama.masaratalnur

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope // Import lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth // Import Firebase auth ktx
import com.google.firebase.ktx.Firebase // Import Firebase ktx
import kotlinx.coroutines.delay // Import delay
import kotlinx.coroutines.launch // Import launch

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
            delay(1500) // Wait for 1.5 seconds (adjust as needed)

            // Check authentication status AFTER the delay
            if (auth.currentUser == null) {

                Log.d("SplashActivity", "User not logged in. Navigating to AuthActivity (Placeholder: MainActivity).")
                startActivity(Intent(this@SplashActivity, AuthActivity::class.java))

            } else {
                // Logged in - Go to MainActivity
                Log.d("SplashActivity", "User logged in. Navigating to MainActivity.")
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            }
            // Finish SplashActivity so the user can't navigate back to it
            finish()
        }
    }
}