package com.oussama.masaratalnur

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
import androidx.navigation.fragment.findNavController // Import NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
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

//    private lateinit var googleSignInClient: GoogleSignInClient
//    private val RC_SIGN_IN = 9001 // Request code for Google Sign In

    // Declare the ActivityResultLauncher
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) { // Use onCreate to register the launcher
        super.onCreate(savedInstanceState)

        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleGoogleSignInResult(task) // Pass the task to a helper function
        }
    }

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

        // --- Configure Google Sign In ---
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // Initialize the client here
        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        // --- End Google Sign In Configuration ---


        binding.buttonLogin.setOnClickListener {
            handleEmailPasswordLogin()
        }

        binding.buttonGoogleSignin.setOnClickListener {
            Log.d("LoginFragment", "Initiating Google Sign-In flow via ActivityResultLauncher.")
            showLoading(true)
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }

        binding.textGoToSignup.setOnClickListener {
            // Navigate to SignUpFragment using Navigation Component Action
            findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
            // We will define this action ID 'action_loginFragment_to_signUpFragment' in the nav graph
        }
    }

    private fun handleEmailPasswordLogin() {
        val email = binding.inputEditEmail.text.toString().trim()
        val password = binding.inputEditPassword.text.toString().trim()

        // --- Input Validation ---
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.inputLayoutEmail.error = getString(R.string.error_invalid_email)
            return
        } else {
            binding.inputLayoutEmail.error = null // Clear error
        }

        if (password.isEmpty()) {
            binding.inputLayoutPassword.error = getString(R.string.error_field_required)
            return
        } else {
            binding.inputLayoutPassword.error = null // Clear error
        }
        // --- End Validation ---

        showLoading(true)

        // --- Firebase Sign In Call ---
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                showLoading(false) // Hide progress bar
                if (task.isSuccessful) {
                    // Sign in success, navigate to the MainActivity
                    Log.d("LoginFragment", "signInWithEmail:success")

                    // Navigate to MainActivity
                    val intent = Intent(requireActivity(), MainActivity::class.java)
                    // Clear the back stack so user can't go back to AuthActivity
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    requireActivity().finish() // Finish AuthActivity

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("LoginFragment", "signInWithEmail:failure", task.exception)
                    val errorMessage = task.exception?.message ?: getString(R.string.error_unknown)
                    Toast.makeText(context, getString(R.string.error_login_failed, errorMessage), Toast.LENGTH_LONG).show()
                    // Consider specific errors like FirebaseAuthInvalidUserException (no user), FirebaseAuthInvalidCredentialsException (wrong password)
                }
            }
        // --- End Firebase Call ---
    }

    private fun handleGoogleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            // Google Sign In was successful, authenticate with Firebase
            val account = completedTask.getResult(ApiException::class.java)!! // Non-null asserted, handle potential null if needed
            Log.d("LoginFragment", "Google Sign In successful, getting Firebase credential for: ${account.email}")
            firebaseAuthWithGoogle(account.idToken!!) // Pass the ID token (non-null asserted)
        } catch (e: ApiException) {
            // Google Sign In failed, update UI appropriately
            showLoading(false) // Hide loading on failure
            Log.w("LoginFragment", "Google sign in failed", e)
            Toast.makeText(context, "Google Sign In Failed: ${e.statusCode}", Toast.LENGTH_SHORT).show()
            // Common status codes:
            // CommonStatusCodes.SIGN_IN_CANCELLED -> User cancelled
            // CommonStatusCodes.NETWORK_ERROR -> Network issue
            // CommonStatusCodes.SIGN_IN_REQUIRED -> Need to sign in (shouldn't happen here usually)
        }
    }

    // Add this function to LoginFragment.kt
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                showLoading(false) // Hide loading indicator
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("LoginFragment", "Firebase signInWithCredential (Google):success")
                    // Navigate to MainActivity
                    val intent = Intent(requireActivity(), MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    requireActivity().finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("LoginFragment", "Firebase signInWithCredential (Google):failure", task.exception)
                    Toast.makeText(context, "Firebase Authentication Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
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