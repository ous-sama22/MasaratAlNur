package com.oussama.masaratalnur.ui.auth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels // Use ktx delegate
import androidx.navigation.fragment.findNavController
import com.oussama.masaratalnur.R
import com.oussama.masaratalnur.databinding.FragmentSignupBinding
import com.oussama.masaratalnur.data.model.AuthUiState // Import UI State
import com.oussama.masaratalnur.ui.viewmodel.AuthViewModel // Import ViewModel

class SignUpFragment : Fragment() {

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!

    // Get ViewModel instance (shared with LoginFragment)
    // Note: Using by viewModels() gets a ViewModel scoped to this Fragment.
    // If you needed a ViewModel scoped to the *Activity* (AuthActivity)
    // to share state between Login/SignUp, you'd use by activityViewModels().
    // For this simple auth flow, fragment-scoped is likely fine.
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.buttonSignup.setOnClickListener {
            handleSignUpAttempt()
        }

        binding.textGoToLogin.setOnClickListener {
            // Simple navigation
            findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
        }
    }

    private fun observeViewModel() {
        authViewModel.uiState.observe(viewLifecycleOwner) { state ->
            showLoading(state is AuthUiState.Loading)

            when (state) {
                is AuthUiState.Idle -> { }
                is AuthUiState.Loading -> { }
                is AuthUiState.Error -> {
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                    authViewModel.resetUiStateToIdle()
                }
                is AuthUiState.NavigationToLogin -> {
                    // Signup successful, navigate back to Login screen
                    Toast.makeText(context, getString(R.string.signup_success), Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
                    // No need to reset state here as we navigate away
                }
                // Other states not relevant to SignUpFragment
                else -> {}
            }
        }
    }

    private fun handleSignUpAttempt() {
        // Clear previous errors
        binding.inputLayoutEmailSignup.error = null
        binding.inputLayoutPasswordSignup.error = null
        binding.inputLayoutConfirmPassword.error = null

        val email = binding.inputEditEmailSignup.text.toString().trim()
        val password = binding.inputEditPasswordSignup.text.toString().trim()
        val confirmPassword = binding.inputEditConfirmPassword.text.toString().trim()

        // --- Client-side Validation ---
        var isValid = true
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.inputLayoutEmailSignup.error = getString(R.string.error_invalid_email)
            isValid = false
        }
        if (password.isEmpty()) {
            binding.inputLayoutPasswordSignup.error = getString(R.string.error_field_required)
            isValid = false
        } else if (password.length < 6) {
            binding.inputLayoutPasswordSignup.error = getString(R.string.error_invalid_password)
            isValid = false
        }
        if (confirmPassword.isEmpty()) {
            binding.inputLayoutConfirmPassword.error = getString(R.string.error_field_required)
            isValid = false
        }
        if (password != confirmPassword) {
            binding.inputLayoutConfirmPassword.error = getString(R.string.error_password_mismatch)
            isValid = false
        }
        // --- End Validation ---

        if (isValid) {
            // Call ViewModel
            authViewModel.signUpWithEmailPassword(email, password)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBarSignup.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.buttonSignup.isEnabled = !isLoading
        binding.textGoToLogin.isEnabled = !isLoading
        binding.inputEditEmailSignup.isEnabled = !isLoading
        binding.inputEditPasswordSignup.isEnabled = !isLoading
        binding.inputEditConfirmPassword.isEnabled = !isLoading
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}