package com.oussama.masaratalnur.data.model

sealed class CategoryListUiState { // Renamed from HomeUiState
    object Loading : CategoryListUiState()
    data class Success(val categories: List<Category>) : CategoryListUiState() // Type changed to Category
    data class Error(val message: String) : CategoryListUiState()
    object Empty : CategoryListUiState()
}