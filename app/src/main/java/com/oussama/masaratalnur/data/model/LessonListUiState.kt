package com.oussama.masaratalnur.data.model

// Define UI State for Lesson List
sealed class LessonListUiState {
    object Loading : LessonListUiState()
    data class Success(val lessons: List<Lesson>) : LessonListUiState()
    data class Error(val message: String) : LessonListUiState()
    object Empty : LessonListUiState()
}