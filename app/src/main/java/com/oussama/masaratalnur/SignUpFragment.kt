package com.oussama.masaratalnur

import android.os.Bundle
import android.util.Log // Import Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast // Import Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController // Import NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth // Import Firebase auth ktx
import com.google.firebase.ktx.Firebase // Import Firebase ktx
import com.oussama.masaratalnur.databinding.FragmentSignupBinding // Import the correct ViewBinding class

class SignUpFragment : Fragment() {

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth

        binding.buttonSignup.setOnClickListener {
            handleSignUp()
        }

        binding.textGoToLogin.setOnClickListener {
            // Navigate back to LoginFragment using Navigation Component Action
            findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
            // We will define this action ID in the nav graph
        }
    }

    private fun handleSignUp() {
        val email = binding.inputEditEmailSignup.text.toString().trim()
        val password = binding.inputEditPasswordSignup.text.toString().trim()
        val confirmPassword = binding.inputEditConfirmPassword.text.toString().trim()

        // --- Input Validation ---
        if (email.isEmpty()) {
            binding.inputLayoutEmailSignup.error = getString(R.string.error_field_required) // Need this string
            return
        } else {
            binding.inputLayoutEmailSignup.error = null // Clear error
        }

        if (password.isEmpty()) {
            binding.inputLayoutPasswordSignup.error = getString(R.string.error_field_required)
            return
        } else if (password.length < 6) { // Firebase requires minimum 6 chars
            binding.inputLayoutPasswordSignup.error = getString(R.string.error_invalid_password)
            return
        } else {
            binding.inputLayoutPasswordSignup.error = null
        }

        if (confirmPassword.isEmpty()) {
            binding.inputLayoutConfirmPassword.error = getString(R.string.error_field_required)
            return
        } else {
            binding.inputLayoutConfirmPassword.error = null
        }

        if (password != confirmPassword) {
            binding.inputLayoutConfirmPassword.error = getString(R.string.error_password_mismatch)
            return
        } else {
            binding.inputLayoutConfirmPassword.error = null // Clear mismatch error too
        }
        // --- End Validation ---


        showLoading(true)
        binding.root.postDelayed({ showLoading(false) }, 2000) // Simulate network

        // TODO: Call Firebase createUserWithEmailAndPassword
        // TODO: Handle success (maybe show message, then navigate to Login or directly to Main?)
        // TODO: Handle failure (show specific error message from Firebase Exception, hide progress bar)

    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBarSignup.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.buttonSignup.isEnabled = !isLoading
        binding.textGoToLogin.isEnabled = !isLoading // Prevent clicking link while loading
        // Optionally disable text fields too
        binding.inputEditEmailSignup.isEnabled = !isLoading
        binding.inputEditPasswordSignup.isEnabled = !isLoading
        binding.inputEditConfirmPassword.isEnabled = !isLoading
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}