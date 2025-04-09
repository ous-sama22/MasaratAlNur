package com.oussama.masaratalnur

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController // Import NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth // Import Firebase auth ktx
import com.google.firebase.ktx.Firebase // Import Firebase ktx
import com.oussama.masaratalnur.databinding.FragmentLoginBinding // Import ViewBinding class

class LoginFragment : Fragment() {

    // Declare ViewBinding and FirebaseAuth variables
    // Use nullable type with backing field for safety during fragment lifecycle
    private var _binding: FragmentLoginBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!! // Non-null assertion operator: Use carefully!

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment using ViewBinding
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root // Return the root view from the binding
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase Auth
        auth = Firebase.auth

        // --- Setup Click Listeners (Placeholders for now) ---

        binding.buttonLogin.setOnClickListener {
            handleEmailPasswordLogin()
        }

        binding.buttonGoogleSignin.setOnClickListener {
            handleGoogleSignIn()
        }

        binding.textGoToSignup.setOnClickListener {
            // Navigate to SignUpFragment using Navigation Component Action
            findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
            // We will define this action ID 'action_loginFragment_to_signUpFragment' in the nav graph
        }
    }

    // --- Placeholder Functions for Logic ---

    private fun handleEmailPasswordLogin() {
        val email = binding.inputEditEmail.text.toString().trim()
        val password = binding.inputEditPassword.text.toString().trim()

        // TODO: Add input validation (check if email/password are empty/valid)
        // TODO: Show progress bar
        // TODO: Call Firebase Auth signInWithEmailAndPassword
        // TODO: Handle success (navigate to MainActivity)
        // TODO: Handle failure (show error message, hide progress bar)
        showLoading(true) // Example
        // Simulate network call
        view?.postDelayed({ showLoading(false)}, 2000)

        // Temporary navigation for testing structure
        // if(email.isNotEmpty() && password.isNotEmpty()){
        //    startActivity(Intent(requireActivity(), MainActivity::class.java))
        //    requireActivity().finish()
        // } else {
        //    binding.inputLayoutEmail.error = "Enter email"
        // }
    }

    private fun handleGoogleSignIn() {
        // TODO: Configure Google Sign In Client
        // TODO: Launch the Google Sign In Intent
        // TODO: Handle the result in onActivityResult (or using Activity Result API)
        // TODO: Exchange Google token for Firebase credential
        // TODO: Sign in to Firebase with credential
        // TODO: Handle success/failure
        showLoading(true)
        view?.postDelayed({ showLoading(false)}, 1500)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBarLogin.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.buttonLogin.isEnabled = !isLoading
        binding.buttonGoogleSignin.isEnabled = !isLoading
        // Optionally disable text fields too
        binding.inputEditEmail.isEnabled = !isLoading
        binding.inputEditPassword.isEnabled = !isLoading
    }


    // --- Fragment Lifecycle ---

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up the binding instance to avoid memory leaks
        _binding = null
    }
}