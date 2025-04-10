package com.oussama.masaratalnur.ui.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels // Use ktx delegate
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.oussama.masaratalnur.ui.auth.AuthActivity
import com.oussama.masaratalnur.R
import com.oussama.masaratalnur.data.model.AuthUiState
import com.oussama.masaratalnur.databinding.FragmentProfileBinding
// Import the ViewModels using the new package structure
import com.oussama.masaratalnur.ui.viewmodel.AuthViewModel
import com.oussama.masaratalnur.ui.viewmodel.UserViewModel

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    // Get ViewModels using ktx delegate
    private val authViewModel: AuthViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels() // Add UserViewModel instance

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupLogoutButton() // Keep logout separate
        observeViewModel() // Set up observers
    }

    private fun setupLogoutButton() {
        binding.buttonLogout.setOnClickListener {
            Log.d("ProfileFragment", "Logout button clicked, calling AuthViewModel.")
            // Get GoogleSignInClient if needed
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

            authViewModel.logout(googleSignInClient) // Call AuthViewModel logout
        }
    }


    private fun observeViewModel() {
        // --- Observe Logout Completion (from AuthViewModel) ---
        authViewModel.uiState.observe(viewLifecycleOwner) { state ->
            if (state is AuthUiState.NavigationToAuth){
                Log.d("ProfileFragment", "Observed logout completion, navigating.")
                navigateToAuthActivity() // Use helper function
                // Optional: Reset the event state in ViewModel if needed
                // authViewModel.onLogoutCompleteHandled()
            }
        }

        // --- Observe User Data (from UserViewModel) ---
        userViewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                Log.d("ProfileFragment", "Observed user data: ${user.email}, XP: ${user.totalXP}, Streak: ${user.currentStreak}")
                binding.textUserEmail.text = user.email ?: getString(R.string.email_not_available) // Add string if needed
                binding.textDisplayNameValue.text = user.displayName ?: getString(R.string.unknown_user) // Use default if null
                binding.textXpValue.text = getString(R.string.xp_format, user.totalXP)

                // Display streak with format
                if (user.currentStreak > 0) {
                    binding.textStreakValue.text = getString(R.string.streak_format, user.currentStreak)
                } else {
                    binding.textStreakValue.text = getString(R.string.streak_format_zero)
                }

            } else {
                Log.d("ProfileFragment", "Observed null user data.")
                binding.textUserEmail.text = getString(R.string.user_not_found)
                binding.textDisplayNameValue.text = getString(R.string.unknown_user)
                binding.textXpValue.text = getString(R.string.xp_format, 0)
                binding.textStreakValue.text = getString(R.string.streak_format_zero)
            }
        }

    }

    private fun navigateToAuthActivity() {
        val intent = Intent(requireActivity(), AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish() // Finish MainActivity
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}