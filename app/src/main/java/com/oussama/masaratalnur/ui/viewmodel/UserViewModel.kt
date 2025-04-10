package com.oussama.masaratalnur.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData // Import extension function
import androidx.lifecycle.viewModelScope // Import viewModelScope
import com.oussama.masaratalnur.data.model.User
import com.oussama.masaratalnur.data.repository.UserRepository
import com.oussama.masaratalnur.data.repository.UserRepositoryImpl // Import the implementation
import kotlinx.coroutines.launch // Import launch

class UserViewModel : ViewModel() {

    // TODO: Inject UserRepository instead of instantiating directly (Manual DI or Hilt later)
    private val userRepository: UserRepository = UserRepositoryImpl()

    // Expose user data as LiveData using the Flow from the repository
    // .asLiveData() automatically handles collecting the Flow within viewModelScope
    // and posting updates to the LiveData object.
    val user: LiveData<User?> = userRepository.getCurrentUser().asLiveData()

    // Example function in ViewModel to trigger an update (won't be used by ProfileFragment directly yet)
    fun updateUserDisplayName(userId: String, newName: String) {
        viewModelScope.launch {
            val result = userRepository.updateUserData(userId, mapOf("displayName" to newName))
            if (result.isFailure) {
                // Handle error - maybe expose an error LiveData?
                println("Error updating display name: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    // Add functions for updating XP, Streak etc. later, calling the repository's update methods
}