package com.oussama.masaratalnur.data.repository

import com.oussama.masaratalnur.data.model.Category
import com.oussama.masaratalnur.data.model.ContentResult
import com.oussama.masaratalnur.data.model.Topic
// Import Lesson, Quiz later when needed
import kotlinx.coroutines.flow.Flow

interface ContentRepository {

    // Renamed: Function to get all categories, ordered by 'order'
    fun getAllCategories(): Flow<ContentResult<List<Category>>> // Renamed & Type Changed

    // New: Function to get topics for a specific category, ordered by 'order'
    fun getTopicsForCategory(categoryId: String): Flow<ContentResult<List<Topic>>>

    // Functions to get lessons for a topic, specific lesson, quizzes etc. will be added later
    // fun getLessonsForTopic(topicId: String): Flow<ContentResult<List<Lesson>>>
    // fun getLesson(lessonId: String): Flow<ContentResult<Lesson>>
    // fun getQuiz(quizId: String): Flow<ContentResult<Quiz>>
}