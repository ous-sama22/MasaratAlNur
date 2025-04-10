package com.oussama.masaratalnur.data.model

// Define a result wrapper for content fetching, similar to AuthResult
sealed class ContentResult<out T> {
    data class Success<T>(val data: T) : ContentResult<T>()
    data class Error(val exception: Exception) : ContentResult<Nothing>()
    object Loading : ContentResult<Nothing>()
}