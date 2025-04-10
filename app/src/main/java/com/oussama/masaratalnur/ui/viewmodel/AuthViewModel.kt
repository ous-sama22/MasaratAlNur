package com.oussama.masaratalnur.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.oussama.masaratalnur.data.model.AuthResult // Correct package
import com.oussama.masaratalnur.data.model.AuthUiState
import com.oussama.masaratalnur.data.repository.AuthRepository
import com.oussama.masaratalnur.data.repository.UserRepository
import com.oussama.masaratalnur.di.ServiceLocator
import kotlinx.coroutines.launch
import kotlin.Result

class AuthViewModel : ViewModel() {

    private val authRepository: AuthRepository = ServiceLocator.provideAuthRepository()
    private val userRepository: UserRepository = ServiceLocator.provideUserRepository()

    // --- LiveData for Unified UI State ---
    private val _uiState = MutableLiveData<AuthUiState>(AuthUiState.Idle)
    val uiState: LiveData<AuthUiState> get() = _uiState


    fun signUpWithEmailPassword(email: String, pass: String) {
        // Don't set Loading here, Repo flow will emit it
        viewModelScope.launch {
            authRepository.signUpWithEmailPassword(email, pass).collect { result -> // Collect the flow
                when (result) {
                    is AuthResult.Success -> {
                        _uiState.value = AuthUiState.Loading
                        Log.d("AuthViewModel", "Sign up success for ${result.user.email}")
                        val docResult = userRepository.createUserDocumentIfNotExists(result.user)
                        if (docResult.isSuccess) {
                            // Sign out immediately after successful signup AND doc creation?
                            // Usually NO. You want them logged in to proceed, but navigate to LOGIN screen.
                            // Let's keep the original logic here: navigate to Login.
                            // If they fail doc creation, we sign out below.
                            Log.d("AuthViewModel", "User document ensured after signup.")
                            _uiState.postValue(AuthUiState.NavigationToLogin) // Still navigate to Login

                        } else {
                            Log.e("AuthViewModel", "Signup success BUT failed user doc creation", docResult.exceptionOrNull())
                            // *** Auto Sign Out on Doc Creation Failure ***
                            authRepository.signOut() // Sign the user back out
                            Log.w("AuthViewModel", "User automatically signed out due to profile setup failure after signup.")
                            _uiState.postValue(AuthUiState.Error("Account created, but profile setup failed. Please try signing in.")) // Updated error
                        }
                    }
                    is AuthResult.Error -> {
                        _uiState.postValue(AuthUiState.Error(result.exception.message ?: "Sign up failed"))
                    }
                    is AuthResult.Loading -> {
                        _uiState.postValue(AuthUiState.Loading) // Update UI state when Repo emits Loading
                    }
                }
            }
        }
    }

    fun signInWithEmailPassword(email: String, pass: String) {
        viewModelScope.launch {
            authRepository.signInWithEmailPassword(email, pass).collect { result -> // Collect the flow
                handleSignInResult(result) // Pass result to common handler
            }
        }
    }

    fun signInWithGoogle(account: GoogleSignInAccount) {
        viewModelScope.launch {
            authRepository.signInWithGoogle(account).collect { result -> // Collect the flow
                handleSignInResult(result) // Pass result to common handler
            }
        }
    }

    // Modify handleSignInResult slightly to handle Loading state from Flow
    private suspend fun handleSignInResult(result: AuthResult) {
        when (result) {
            is AuthResult.Success -> {
                _uiState.value = AuthUiState.Loading // Still loading while checking user doc
                Log.d("AuthViewModel", "Sign in success for ${result.user.email}")
                val docResult = userRepository.createUserDocumentIfNotExists(result.user)
                if (docResult.isSuccess) {
                    Log.d("AuthViewModel", "User document ensured after signin.")
                    _uiState.postValue(AuthUiState.NavigationToMain)
                } else {
                    Log.e("AuthViewModel", "Signin success BUT failed user doc creation", docResult.exceptionOrNull())
                    // *** Auto Sign Out on Doc Creation Failure ***
                    authRepository.signOut() // Sign the user back out
                    Log.w("AuthViewModel", "User automatically signed out due to profile setup failure.")
                    _uiState.postValue(AuthUiState.Error("Login successful, but profile setup failed. Please try again.")) // Updated error message
                }
            }
            is AuthResult.Error -> {
                _uiState.postValue(AuthUiState.Error(result.exception.message ?: "Sign in failed"))
            }
            is AuthResult.Loading -> {
                _uiState.postValue(AuthUiState.Loading) // Update UI state
            }
        }
    }
    fun sendPasswordResetEmail(email: String) {
        // ... (logic remains the same, posts Loading, PasswordResetEmailSent or Error to _uiState)
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val result: Result<Unit> = authRepository.sendPasswordResetEmail(email)
            if (result.isSuccess) {
                _uiState.postValue(AuthUiState.PasswordResetEmailSent)
            } else {
                _uiState.postValue(AuthUiState.Error(result.exceptionOrNull()?.message ?: "Failed to send reset email"))
            }
        }
    }


    // --- Logout (Modified) ---
    fun logout(googleSignInClient: GoogleSignInClient? = null) {
        // Optionally set Loading state if logout takes time or involves async cleanup
        _uiState.value = AuthUiState.Loading // Indicate logout process starts
        Log.d("AuthViewModel", "Logout requested.")
        viewModelScope.launch {
            googleSignInClient?.signOut()?.addOnCompleteListener {
                Log.d("AuthViewModel", "Google Client signed out.")
                // Potential timing issue if signout finishes after repo.signOut,
                // better to await if strict order matters, but often okay.
            }

            authRepository.signOut() // Call repo sign out
            Log.d("AuthViewModel", "Firebase sign out completed via Repo.")

            // Signal completion via UI State
            _uiState.postValue(AuthUiState.NavigationToAuth) // Trigger navigation back to Auth
        }
    }

    // --- REMOVE onLogoutCompleteHandled ---
    // fun onLogoutCompleteHandled() { ... }

    // Function to reset UI state after handling an event (like showing error/message)
    fun resetUiStateToIdle() { // Renamed for clarity
        if (_uiState.value !is AuthUiState.Loading) { // Don't reset if still loading
            _uiState.value = AuthUiState.Idle
        }
    }
}