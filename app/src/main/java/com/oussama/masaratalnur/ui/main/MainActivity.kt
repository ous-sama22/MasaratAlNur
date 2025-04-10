package com.oussama.masaratalnur.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.oussama.masaratalnur.R
import com.oussama.masaratalnur.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding // Declare binding variable
    private lateinit var navController: NavController // Declare NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- Setup Navigation ---
        // Find the NavHostFragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_main) as NavHostFragment
        // Get the NavController
        navController = navHostFragment.navController

        // Connect BottomNavigationView with NavController
        binding.bottomNavigation.setupWithNavController(navController)
        // --- End Navigation Setup ---

    }

    // Optional: Handle Up button if you add an ActionBar/Toolbar later
    // override fun onSupportNavigateUp(): Boolean {
    //     return navController.navigateUp() || super.onSupportNavigateUp()
    // }
}