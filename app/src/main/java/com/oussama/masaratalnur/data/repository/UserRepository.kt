package com.oussama.masaratalnur.data.repository

import com.oussama.masaratalnur.data.model.User
import kotlinx.coroutines.flow.Flow // Using Flow for asynchronous stream

interface UserRepository {

    // Function to get the current user's data as a Flow
    // Flow allows observing real-time updates from Firestore if needed
    fun getCurrentUser(): Flow<User?> // Returns Flow emitting User or null if not logged in/found

    // Function to get user data once (alternative)
    // suspend fun getCurrentUserOnce(): User? // Could add this if needed

    // Functions to update user data will be added later (e.g., updateXP, updateStreak)
    suspend fun updateUserData(userId: String, data: Map<String, Any>): Result<Unit> // Example update function

    suspend fun createUserDocumentIfNotExists(firebaseUser: com.google.firebase.auth.FirebaseUser): Result<Unit>
}
