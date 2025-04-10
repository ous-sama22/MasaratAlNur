package com.oussama.masaratalnur.data.model

import com.google.firebase.auth.FirebaseUser

// Define a sealed class/interface for representing auth results more explicitly
sealed class AuthResult {
    data class Success(val user: FirebaseUser) : AuthResult()
    data class Error(val exception: Exception) : AuthResult()
    object Loading : AuthResult() // Optional: For representing loading state if Repo manages it
}