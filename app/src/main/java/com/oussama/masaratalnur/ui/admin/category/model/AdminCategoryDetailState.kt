package com.oussama.masaratalnur.ui.admin.category.model

import com.oussama.masaratalnur.data.model.Category

// Add state for the single category being edited/viewed
sealed class AdminCategoryDetailState {
    object Idle : AdminCategoryDetailState()
    object Loading : AdminCategoryDetailState()
    data class Success(val category: Category) : AdminCategoryDetailState()
    data class Error(val message: String) : AdminCategoryDetailState()
    object NotFound : AdminCategoryDetailState() // Specific state for not found
}