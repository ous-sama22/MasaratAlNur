package com.oussama.masaratalnur.data.model

// Define UI State for Topic List
sealed class TopicListUiState {
    object Loading : TopicListUiState()
    data class Success(val topics: List<Topic>) : TopicListUiState()
    data class Error(val message: String) : TopicListUiState()
    object Empty : TopicListUiState() // State for when there are no topics
}