package com.oussama.masaratalnur.ui.auth

import android.app.Activity // Import Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels // Use ktx delegate
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.oussama.masaratalnur.ui.main.MainActivity // Import MainActivity
import com.oussama.masaratalnur.R
import com.oussama.masaratalnur.databinding.FragmentLoginBinding
import com.oussama.masaratalnur.data.model.AuthUiState // Import UI State
import com.oussama.masaratalnur.ui.viewmodel.AuthViewModel // Import ViewModel

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    // Get ViewModel instance
    private val authViewModel: AuthViewModel by viewModels()

    // --- Google Sign In variables ---
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- Configure Google Sign In (Needed here to launch intent) ---
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        // --- Register ActivityResultLauncher for Google Sign In ---
        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) { // Check if result is OK
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleGoogleSignInResult(task)
            } else {
                // Handle cancellation or other errors from the Google Sign In activity itself
                Log.w("LoginFragment", "Google Sign In Activity cancelled or failed.")
                Toast.makeText(context, "Google Sign In cancelled", Toast.LENGTH_SHORT).show()
                // Ensure loading state is reset if needed
                showLoading(false) // Explicitly hide loading on cancellation
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.buttonLogin.setOnClickListener {
            handleEmailPasswordLoginAttempt() // Call helper function
        }

        binding.buttonGoogleSignin.setOnClickListener {
            handleGoogleSignInAttempt() // Call helper function
        }

        binding.textForgotPassword.setOnClickListener {
            handleForgotPasswordAttempt() // Call helper function
        }

        binding.textGoToSignup.setOnClickListener {
            // Simple navigation - No ViewModel needed for this
            findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
        }
    }

    // --- ViewModel Observation ---
    private fun observeViewModel() {
        authViewModel.uiState.observe(viewLifecycleOwner) { state ->
            // Update UI based on the state received from ViewModel
            showLoading(state is AuthUiState.Loading) // Show loading indicator

            when (state) {
                is AuthUiState.Idle -> {
                    // Optional: Reset UI fields or error states if needed
                }
                is AuthUiState.Loading -> {
                    // Handled by showLoading above
                }
                is AuthUiState.Error -> {
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                    authViewModel.resetUiStateToIdle() // Reset state after showing error
                }
                is AuthUiState.NavigationToMain -> {
                    Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show() // Optional feedback
                    navigateToMainActivity()
                    // No need to reset state here as we are navigating away
                }
                is AuthUiState.PasswordResetEmailSent -> {
                    Toast.makeText(context, getString(R.string.forgot_password_email_sent), Toast.LENGTH_LONG).show()
                    authViewModel.resetUiStateToIdle() // Reset state after showing message
                }
                // Other states like NavigationToLogin or NavigationToAuth are not handled here
                else -> {
                    // Ignore other states not relevant to LoginFragment
                }
            }
        }
    }

    // --- Action Handlers (Call ViewModel) ---

    private fun handleEmailPasswordLoginAttempt() {
        // Clear previous errors
        binding.inputLayoutEmail.error = null
        binding.inputLayoutPassword.error = null

        val email = binding.inputEditEmail.text.toString().trim()
        val password = binding.inputEditPassword.text.toString().trim()

        // Basic client-side validation (optional, ViewModel can also validate)
        var isValid = true
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.inputLayoutEmail.error = getString(R.string.error_invalid_email)
            isValid = false
        }
        if (password.isEmpty()) {
            binding.inputLayoutPassword.error = getString(R.string.error_field_required)
            isValid = false
        }

        if (isValid) {
            // Call ViewModel function
            authViewModel.signInWithEmailPassword(email, password)
        }
    }

    private fun handleGoogleSignInAttempt() {
        Log.d("LoginFragment", "Initiating Google Sign-In flow via ActivityResultLauncher.")
        // ViewModel will set Loading state when it receives the result
        googleSignInLauncher.launch(googleSignInClient.signInIntent)
    }

    private fun handleForgotPasswordAttempt() {
        binding.inputLayoutEmail.error = null // Clear error first
        val email = binding.inputEditEmail.text.toString().trim()
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.inputLayoutEmail.error = getString(R.string.forgot_password_enter_email)
            return
        }
        // Call ViewModel function
        authViewModel.sendPasswordResetEmail(email)
    }


    // --- Google Sign In Result Handling ---
    private fun handleGoogleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)!!
            Log.d("LoginFragment", "Google Sign In successful (UI), passing to ViewModel: ${account.email}")
            // Pass account to ViewModel to handle Firebase auth
            authViewModel.signInWithGoogle(account) // Call ViewModel
        } catch (e: ApiException) {
            showLoading(false) // Explicitly stop loading on immediate API exception
            Log.w("LoginFragment", "Google sign in failed (UI)", e)
            Toast.makeText(context, "Google Sign In Failed: ${e.statusCode}", Toast.LENGTH_SHORT).show()
            authViewModel.resetUiStateToIdle() // Reset VM state if Google signin itself fails
        }
    }

    // --- UI Helper Functions ---

    private fun showLoading(isLoading: Boolean) {
        binding.progressBarLogin.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.buttonLogin.isEnabled = !isLoading
        binding.buttonGoogleSignin.isEnabled = !isLoading
        binding.textForgotPassword.isEnabled = !isLoading
        binding.textGoToSignup.isEnabled = !isLoading
        binding.inputEditEmail.isEnabled = !isLoading
        binding.inputEditPassword.isEnabled = !isLoading
    }

    private fun navigateToMainActivity() {
        val intent = Intent(requireActivity(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clean up binding
    }
}