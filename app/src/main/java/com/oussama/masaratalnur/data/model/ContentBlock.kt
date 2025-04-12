package com.oussama.masaratalnur.data.model

data class ContentBlock(
    val type: String = "text", // e.g., "text", "image", "header", "quote", "reference"
    val value_ar: String = "", // Arabic content value
    val imageUrl: String? = null, // Optional URL if type is "image"
    val order: Int = 0 // Order within the lesson
    // Add other potential fields like attribution, style hints etc.
)