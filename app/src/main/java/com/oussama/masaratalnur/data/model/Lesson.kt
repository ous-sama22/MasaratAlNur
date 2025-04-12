package com.oussama.masaratalnur.data.model

import com.google.firebase.firestore.DocumentId

data class Lesson(
    @DocumentId
    val id: String = "",
    val topicId: String = "", // Reference to parent Topic
    val title_ar: String = "",
    val order: Int = 0, // Order within the topic
    // Store content as a list of structured blocks
    val contentBlocks: List<ContentBlock> = emptyList(),
    val xpAward: Int = 10, // Default XP for completing the lesson
    val quizId: String? = null, // Optional ID of a quiz directly following this lesson
    val status: ContentStatus = ContentStatus.PUBLISHED // Publishing status
    // Add other potential fields: estimated time, author, revision date etc.
)