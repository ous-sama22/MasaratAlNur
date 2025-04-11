package com.oussama.masaratalnur.ui.admin.category.model

// Events for Add/Edit screen outcomes
sealed class AdminCategoryFormEvent {
    object Idle : AdminCategoryFormEvent()
    object Loading : AdminCategoryFormEvent()
    object Success : AdminCategoryFormEvent()
    data class Error(val message: String) : AdminCategoryFormEvent()
    object DeleteSuccess : AdminCategoryFormEvent() // Specific event for delete success
}