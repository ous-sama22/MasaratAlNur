package com.oussama.masaratalnur.data.model

import com.google.firebase.firestore.PropertyName // For potential Firestore field name mapping

data class User(
    // Use default values for Firestore deserialization robustness
    val uid: String = "",
    val email: String = "",
    @get:PropertyName("displayName") @set:PropertyName("displayName") // Example if Firestore field is different
    var displayName: String? = null, // Allow null if not always set
    var totalXP: Int = 0,
    var currentStreak: Int = 0,
    val role: String = ROLE_USER // Add role field with default
    // Add other fields like lastActivityDate (Timestamp or String), completedLessonIds (List<String>) later
){
    // Companion object for role constants
    companion object {
        const val ROLE_USER = "user"
        const val ROLE_EDITOR = "editor" // Optional intermediate role
        const val ROLE_ADMIN = "admin"
    }
}