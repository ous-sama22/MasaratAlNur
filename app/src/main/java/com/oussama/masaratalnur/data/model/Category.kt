package com.oussama.masaratalnur.data.model

import com.google.firebase.firestore.DocumentId

data class Category(
    @DocumentId
    val id: String = "",
    val title_ar: String = "",
    val description_ar: String = "",
    val order: Int = 0,
    val imageUrl: String? = null,
    val status: ContentStatus = ContentStatus.PUBLISHED

)