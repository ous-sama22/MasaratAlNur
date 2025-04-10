package com.oussama.masaratalnur.data.model

// Define specific UI state for Home screen
sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val topics: List<Topic>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
    object Empty : HomeUiState() // State for when topics are successfully fetched but the list is empty
}