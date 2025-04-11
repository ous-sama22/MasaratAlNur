package com.oussama.masaratalnur.ui.admin.category.model

import com.oussama.masaratalnur.data.model.Category

// State for the category list screen
sealed class AdminCategoryListState {
    object Loading : AdminCategoryListState()
    data class Success(val categories: List<Category>) : AdminCategoryListState()
    data class Error(val message: String) : AdminCategoryListState()
    object Empty : AdminCategoryListState()
}