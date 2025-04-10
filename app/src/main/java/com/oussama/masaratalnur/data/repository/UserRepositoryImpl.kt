package com.oussama.masaratalnur.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.oussama.masaratalnur.data.model.User
import kotlinx.coroutines.channels.awaitClose // For Flow cleanup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow // Good way to wrap listener-based APIs
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await // For suspending await on Tasks

class UserRepositoryImpl : UserRepository {

    private val auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = Firebase.firestore

    override fun getCurrentUser(): Flow<User?> = callbackFlow {
        var snapshotListenerRegistration: ListenerRegistration? = null // Keep track of Firestore listener

        // Observe Auth State changes using the AuthRepository or directly
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val currentUid = firebaseAuth.currentUser?.uid
            Log.d("UserRepositoryImpl", "Auth state changed. Current UID: $currentUid")

            // Remove previous listener if it exists, before starting a new one or emitting null
            snapshotListenerRegistration?.remove()

            if (currentUid == null) {
                // User signed out, emit null
                Log.d("UserRepositoryImpl", "User signed out, emitting null user data.")
                trySend(null).isSuccess
            } else {
                // User signed in, setup Firestore listener for this UID
                val userDocRef = db.collection("users").document(currentUid)
                snapshotListenerRegistration = userDocRef.addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        // Firestore specific error (like network issue, or maybe brief permission denial)
                        Log.w("UserRepositoryImpl", "Firestore snapshot listener error for UID $currentUid", error)
                        // Decide how to handle - close with error, or just emit null? Emitting null might be safer.
                        trySend(null).isSuccess // Emit null on listener error
                        // close(error) // Optionally close flow on persistent errors
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val user = snapshot.toObject<User>()
                        Log.d("UserRepositoryImpl", "User data emitted via snapshot listener: $user")
                        trySend(user).isSuccess
                    } else {
                        Log.d("UserRepositoryImpl", "User document for $currentUid is null or doesn't exist.")
                        trySend(null) // Emit null if document doesn't exist for the logged-in user
                    }
                }
            }
        }

        // Register the auth state listener
        auth.addAuthStateListener(authStateListener)

        // This block is called when the Flow collector cancels
        awaitClose {
            Log.d("UserRepositoryImpl", "Closing listeners (awaitClose).")
            // Remove both listeners
            auth.removeAuthStateListener(authStateListener)
            snapshotListenerRegistration?.remove()
        }
    }.distinctUntilChanged() // Optional: prevent emitting the same User object multiple times if snapshot updates don't change relevant fields

    // Implementation for the example update function
    override suspend fun updateUserData(userId: String, data: Map<String, Any>): Result<Unit> {
        return try {
            db.collection("users").document(userId).update(data).await()
            Log.d("UserRepositoryImpl", "User data updated successfully for $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("UserRepositoryImpl", "Error updating user data for $userId", e)
            Result.failure(e)
        }
    }


    // Inside UserRepositoryImpl class

    override suspend fun createUserDocumentIfNotExists(firebaseUser: com.google.firebase.auth.FirebaseUser): Result<Unit> {
        val userDocRef = db.collection("users").document(firebaseUser.uid)
        return try {
            val snapshot = userDocRef.get().await() // Check if document exists
            if (!snapshot.exists()) {
                Log.d("UserRepositoryImpl", "User document for ${firebaseUser.uid} does not exist. Creating...")
                // Create a default User object
                val newUser = User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email ?: "unknown@example.com", // Get email from FirebaseUser
                    displayName = generateDefaultDisplayName(firebaseUser.email), // Create default name
                    totalXP = 0,
                    currentStreak = 0
                    // Initialize other fields with defaults
                )
                userDocRef.set(newUser).await() // Use set() to create the document
                Log.d("UserRepositoryImpl", "User document created successfully.")
                Result.success(Unit)
            } else {
                Log.d("UserRepositoryImpl", "User document for ${firebaseUser.uid} already exists.")
                // Optional: Update existing document with potentially new info? (e.g., if display name changed in Google)
                // For now, just succeed if it exists.
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Log.e("UserRepositoryImpl", "Error checking/creating user document for ${firebaseUser.uid}", e)
            Result.failure(e)
        }
    }

    // Helper function to generate a default display name from email
    private fun generateDefaultDisplayName(email: String?): String {
        return email?.substringBefore('@')?.replace(".", " ")?.replace("_", " ")?.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString()
        } ?: "User" // Default if email is null somehow
    }

}