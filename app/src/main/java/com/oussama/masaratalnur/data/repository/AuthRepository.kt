package com.oussama.masaratalnur.data.repository

import com.google.android.gms.auth.api.signin.GoogleSignInAccount // For Google Sign In result
import com.google.firebase.auth.FirebaseUser
import com.oussama.masaratalnur.data.model.AuthResult
import kotlinx.coroutines.flow.Flow


interface AuthRepository {

    // Get current authenticated user (can return null)
    fun getCurrentAuthUser(): FirebaseUser?

    // Observe auth state changes (useful for automatic login/logout handling)
    // Returns a Flow emitting the FirebaseUser or null
    fun observeAuthState(): Flow<FirebaseUser?> // Assuming import kotlinx.coroutines.flow.Flow

    // Sign up with Email/Password
    fun signUpWithEmailPassword(email: String, pass: String): Flow<AuthResult>

    // Sign in with Email/Password
    fun signInWithEmailPassword(email: String, pass: String): Flow<AuthResult>

    // Sign in with Google Credential
    fun signInWithGoogle(account: GoogleSignInAccount): Flow<AuthResult> // Pass Google account info

    // Send Password Reset Email
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> // Use Kotlin Result for simple success/failure

    // Sign Out
    suspend fun signOut() // Simple suspend function for sign out
}
