package com.oussama.masaratalnur.ui.admin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.oussama.masaratalnur.R
import com.oussama.masaratalnur.databinding.ActivityAdminBinding

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Toolbar
        setSupportActionBar(binding.toolbarAdmin)

        // Setup Navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_admin) as NavHostFragment
        navController = navHostFragment.navController

        // Define top-level destinations (for AppBarConfiguration)
        // The start destination of admin_nav_graph should be here
        appBarConfiguration = AppBarConfiguration(setOf(R.id.adminDashboardFragment)) // Assuming this is the start ID

        // Connect ActionBar/Toolbar with NavController
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    // Handle Up navigation button in the Toolbar
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}