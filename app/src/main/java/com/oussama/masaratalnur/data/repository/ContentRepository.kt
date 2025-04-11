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

    // Category CRUD
    suspend fun addCategory(category: Category): Result<Unit> // Return result for success/fail
    suspend fun updateCategory(category: Category): Result<Unit>
    suspend fun deleteCategory(categoryId: String): Result<Unit>
    fun getCategory(categoryId: String): Flow<ContentResult<Category>> // Return Flow for potential real-time updates on edit screen

    // Functions to get lessons for a topic, specific lesson, quizzes etc. will be added later
    // fun getLessonsForTopic(topicId: String): Flow<ContentResult<List<Lesson>>>
    // fun getLesson(lessonId: String): Flow<ContentResult<Lesson>>
    // fun getQuiz(quizId: String): Flow<ContentResult<Quiz>>
}