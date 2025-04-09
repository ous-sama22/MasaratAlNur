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

        // --- Input Validation (Keep the existing validation code) ---
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { // Improved email validation
            binding.inputLayoutEmailSignup.error = getString(R.string.error_invalid_email)
            return
        } else {
            binding.inputLayoutEmailSignup.error = null
        }
        // ... other validation for password, confirmPassword, mismatch ...
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

        showLoading(true) // Show progress bar

        // --- Firebase Sign Up Call ---
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task -> // Use requireActivity() for lifecycle context
                showLoading(false) // Hide progress bar regardless of outcome
                if (task.isSuccessful) {
                    // Sign in success
                    Log.d("SignUpFragment", "createUserWithEmail:success")
                    // Optional: Send verification email
                    // auth.currentUser?.sendEmailVerification()?.addOnCompleteListener { verificationTask -> ... }

                    Toast.makeText(context, getString(R.string.signup_success), Toast.LENGTH_SHORT).show()

                    // Navigate to Login screen after successful signup
                    // Or potentially directly to MainActivity if desired (but login is common)
                    findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("SignUpFragment", "createUserWithEmail:failure", task.exception)
                    // Try to show a more specific error
                    val errorMessage = task.exception?.message ?: getString(R.string.error_unknown) // Need error_unknown string
                    Toast.makeText(context, getString(R.string.error_signup_failed, errorMessage), Toast.LENGTH_LONG).show()
                    // You might want to parse task.exception to provide more specific feedback
                    // e.g., FirebaseAuthUserCollisionException means email already exists
                    // FirebaseAuthWeakPasswordException etc.
                }
            }
        // --- End Firebase Call ---
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