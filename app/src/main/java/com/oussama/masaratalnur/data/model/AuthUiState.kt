package com.oussama.masaratalnur.data.model


// Sealed class to represent UI state/events for Auth screens
sealed class AuthUiState {
    object Idle : AuthUiState() // Initial state
    object Loading : AuthUiState() // Show progress indicator
    data class Error(val message: String) : AuthUiState() // Show error message
    object NavigationToMain : AuthUiState() // Trigger navigation to MainActivity
    object NavigationToLogin : AuthUiState() // Trigger navigation back to Login (e.g., after signup)
    object NavigationToAuth : AuthUiState() // Trigger navigation from Profile back to Auth on logout << NEW
    object PasswordResetEmailSent : AuthUiState() // Show confirmation for password reset
}
