package com.oussama.masaratalnur.data.repository

import com.oussama.masaratalnur.data.model.Category
import com.oussama.masaratalnur.data.model.ContentResult
import com.oussama.masaratalnur.data.model.Lesson
import com.oussama.masaratalnur.data.model.Topic
// Import Lesson, Quiz later when needed
import kotlinx.coroutines.flow.Flow

interface ContentRepository {

    // --- Categories ---
    fun getAllCategories(): Flow<ContentResult<List<Category>>>
    fun getCategory(categoryId: String): Flow<ContentResult<Category>>
    suspend fun addCategory(category: Category): Result<Unit>
    suspend fun updateCategory(category: Category): Result<Unit>
    suspend fun deleteCategory(categoryId: String): Result<Unit>

    // --- Topics ---
    fun getTopicsForCategory(categoryId: String): Flow<ContentResult<List<Topic>>>
    fun getAllTopics(): Flow<ContentResult<List<Topic>>> // Get ALL topics (for admin?)
    fun getTopic(topicId: String): Flow<ContentResult<Topic>> // Get a single topic by its ID
    suspend fun addTopic(topic: Topic): Result<Unit>
    suspend fun updateTopic(topic: Topic): Result<Unit>
    suspend fun deleteTopic(topicId: String): Result<Unit>

    // --- Lessons ---
    fun getLessonsForTopic(topicId: String): Flow<ContentResult<List<Lesson>>> // Get lessons for a specific topic
    fun getAllLessons(): Flow<ContentResult<List<Lesson>>> // Get ALL lessons (for admin?)
    fun getLesson(lessonId: String): Flow<ContentResult<Lesson>> // Get a single lesson by its ID
    suspend fun addLesson(lesson: Lesson): Result<Unit>
    suspend fun updateLesson(lesson: Lesson): Result<Unit>
    suspend fun deleteLesson(lessonId: String): Result<Unit>
}