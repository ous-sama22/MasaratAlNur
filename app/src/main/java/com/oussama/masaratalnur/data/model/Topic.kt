package com.oussama.masaratalnur.data.model

import com.google.firebase.firestore.DocumentId // Import to potentially capture document ID

data class Topic(
    @DocumentId // Annotation to automatically populate this field with the document ID
    val id: String = "", // Unique ID for the topic (can be Firestore auto-ID)
    val categoryId: String = "",
    val title_ar: String = "", // Title in Arabic
    val description_ar: String = "", // Description in Arabic
    val order: Int = 0, // For sorting/display order
    val imageUrl: String? = null // Optional URL for a topic image
    // Add any other relevant fields, e.g., estimated time, difficulty level
)