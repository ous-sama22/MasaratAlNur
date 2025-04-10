package com.oussama.masaratalnur.data.repository

import com.oussama.masaratalnur.data.model.ContentResult
import com.oussama.masaratalnur.data.model.Topic
// Import Lesson, Quiz later when needed
import kotlinx.coroutines.flow.Flow

interface ContentRepository {

    // Function to get all topics, ordered by the 'order' field
    fun getAllTopics(): Flow<ContentResult<List<Topic>>>

    // Functions to get lessons for a topic, specific lesson, quizzes etc. will be added later
    // fun getLessonsForTopic(topicId: String): Flow<ContentResult<List<Lesson>>>
    // fun getLesson(lessonId: String): Flow<ContentResult<Lesson>>
    // fun getQuiz(quizId: String): Flow<ContentResult<Quiz>>
}