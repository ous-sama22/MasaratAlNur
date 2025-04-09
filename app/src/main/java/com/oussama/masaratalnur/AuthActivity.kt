package com.oussama.masaratalnur

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.oussama.masaratalnur.databinding.ActivityAuthBinding

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var firebaseAuth: FirebaseAuth

    // ActivityResultLauncher for the FirebaseUI Activity
    private val signInLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val response = IdpResponse.fromResultIntent(result.data)

            if (result.resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                val user = FirebaseAuth.getInstance().currentUser
                Log.i("AuthActivity", "Sign in successful: ${user?.uid}, Name: ${user?.displayName}, Email: ${user?.email}")
                Toast.makeText(this, "Sign in successful!", Toast.LENGTH_SHORT).show()
                // TODO: Add user data to Firestore if it's a new user (We'll do this later)
                navigateToMain()
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check error.
                if (response == null) {
                    Log.w("AuthActivity", "Sign in cancelled by user.")
                    Toast.makeText(this, "Sign in cancelled", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("AuthActivity", "Sign in failed: Code: ${response.error?.errorCode}, Message: ${response.error?.message}", response.error)
                    Toast.makeText(this, "Sign in failed: ${response.error?.message}", Toast.LENGTH_LONG).show()
                }
                // Decide what to do on failure/cancellation. Finishing closes the app temporarily.
                // You might want to stay on AuthActivity to allow retry without restarting.
                finish() // Example: close if failed/cancelled for now
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        // Normally SplashActivity would check login status first.
        // If AuthActivity is launched directly and user is already logged in, go to Main.
        if (firebaseAuth.currentUser != null) {
            Log.i("AuthActivity", "User already signed in (${firebaseAuth.currentUser?.uid}), navigating to Main.")
            navigateToMain()
        } else {
            // No user signed in, launch FirebaseUI
            Log.i("AuthActivity", "No user signed in, launching FirebaseUI flow.")
            launchSignInFlow()
        }
    }

    private fun launchSignInFlow() {
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // Create and launch sign-in intent
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            // Optional: Customize theme and logo
            .setLogo(R.mipmap.ic_launcher) // Replace with your actual logo resource ID if you have one
            // .setTheme(R.style.Theme_MasaratAlNur) // Make sure this theme exists in styles.xml
            // .setIsSmartLockEnabled(false, true) // Optional: Disable auto sign-in, but allow saving
            .build()

        signInLauncher.launch(signInIntent)
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        // Clear the activity stack so back button doesn't return to AuthActivity
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Close AuthActivity
    }
}