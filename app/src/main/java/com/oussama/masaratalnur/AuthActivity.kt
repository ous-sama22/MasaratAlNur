package com.oussama.masaratalnur

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.oussama.masaratalnur.databinding.ActivityAuthBinding // Import ViewBinding class

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding // Declare binding variable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater) // Inflate the layout
        setContentView(binding.root) // Set the content view using binding.root

        // The NavHostFragment will be added via XML layout
    }
}