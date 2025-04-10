package com.oussama.masaratalnur.data.repository

import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.oussama.masaratalnur.data.model.AuthResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlin.Result // Ensure Kotlin Result is imported

class AuthRepositoryImpl : AuthRepository {

    private val auth: FirebaseAuth = Firebase.auth

    override fun getCurrentAuthUser(): FirebaseUser? {
        return auth.currentUser
    }

    override fun observeAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser).isSuccess // Emit current user on change
        }
        auth.addAuthStateListener(authStateListener)
        // Close removes the listener when the Flow collector cancels
        awaitClose { auth.removeAuthStateListener(authStateListener) }
    }


    override fun signUpWithEmailPassword(email: String, pass: String): Flow<AuthResult> = flow {
        emit(AuthResult.Loading) // Emit Loading first
        try {
            val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
            emit(AuthResult.Success(authResult.user!!)) // Emit Success
        } catch (e: Exception) {
            Log.w("AuthRepositoryImpl", "signUpWithEmailPassword failed", e)
            emit(AuthResult.Error(e)) // Emit Error
        }
    }

    override fun signInWithEmailPassword(email: String, pass: String): Flow<AuthResult> = flow {
        emit(AuthResult.Loading) // Emit Loading first
        try {
            val authResult = auth.signInWithEmailAndPassword(email, pass).await()
            emit(AuthResult.Success(authResult.user!!)) // Emit Success
        } catch (e: Exception) {
            Log.w("AuthRepositoryImpl", "signInWithEmailPassword failed", e)
            emit(AuthResult.Error(e)) // Emit Error
        }
    }

    override fun signInWithGoogle(account: GoogleSignInAccount): Flow<AuthResult> = flow {
        emit(AuthResult.Loading) // Emit Loading first
        try {
            val idToken = account.idToken
            if (idToken == null) {
                Log.w("AuthRepositoryImpl", "GoogleSignInAccount missing idToken.")
                emit(AuthResult.Error(IllegalStateException("GoogleSignInAccount missing idToken.")))
                return@flow // Stop the flow here
            }
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            emit(AuthResult.Success(authResult.user!!)) // Emit Success
        } catch (e: Exception) {
            Log.w("AuthRepositoryImpl", "signInWithGoogle failed", e)
            emit(AuthResult.Error(e)) // Emit Error
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w("AuthRepositoryImpl", "sendPasswordResetEmail failed", e)
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        try {
            // Note: Signing out Google client should happen closer to UI/ViewModel
            // as repository shouldn't ideally depend on UI components like GoogleSignInClient
            auth.signOut()
            Log.d("AuthRepositoryImpl", "Firebase signOut successful.")
        } catch (e: Exception) {
            // SignOut itself doesn't usually throw, but added for completeness
            Log.e("AuthRepositoryImpl", "Error during Firebase signOut", e)
        }
    }
}