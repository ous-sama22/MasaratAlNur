package com.oussama.masaratalnur.data.model

import com.google.firebase.firestore.PropertyName // For potential Firestore field name mapping

data class User(
    // Use default values for Firestore deserialization robustness
    val uid: String = "",
    val email: String = "",
    @get:PropertyName("displayName") @set:PropertyName("displayName") // Example if Firestore field is different
    var displayName: String? = null, // Allow null if not always set
    var totalXP: Int = 0,
    var currentStreak: Int = 0
    // Add other fields like lastActivityDate (Timestamp or String), completedLessonIds (List<String>) later
) {
    // Add a no-argument constructor for Firestore deserialization if needed,
    // though Kotlin data classes often work directly if properties have defaults.
    //constructor() : this("", "", null, 0, 0)
}