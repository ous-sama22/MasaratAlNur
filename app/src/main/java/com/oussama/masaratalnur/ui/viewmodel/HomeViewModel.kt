package com.oussama.masaratalnur.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.oussama.masaratalnur.data.model.CategoryListUiState
import com.oussama.masaratalnur.data.repository.ContentRepository
import com.oussama.masaratalnur.data.model.ContentResult // Import ContentResult
import com.oussama.masaratalnur.di.ServiceLocator
import kotlinx.coroutines.flow.map // Use Flow's map operator

class HomeViewModel : ViewModel() {

    // Get repository via ServiceLocator (Manual DI)
    private val contentRepository: ContentRepository = ServiceLocator.provideContentRepository()

    // Rename LiveData and update logic
    val categoryListState: LiveData<CategoryListUiState> = // Renamed from homeState
        contentRepository.getAllCategories() // Call new repo function
            .map { result ->
                when (result) {
                    is ContentResult.Loading -> CategoryListUiState.Loading
                    is ContentResult.Error -> CategoryListUiState.Error(
                        result.exception.message ?: "Failed to load categories" // Updated error message context
                    )
                    is ContentResult.Success -> {
                        if (result.data.isEmpty()) {
                            CategoryListUiState.Empty
                        } else {
                            // Ensure data contains Categories
                            CategoryListUiState.Success(result.data)
                        }
                    }
                }
            }
            .asLiveData()

    // Function to explicitly trigger a re-fetch (or rely on Flow's nature)
    // Firestore's callbackFlow *should* re-emit if data changes,
    // but a manual trigger might be useful for explicit refresh/retry.
    // Simplest retry: If repo uses callbackFlow, simply having an active observer should retry on error recovery.
    // Let's rely on the observer being active for retry for now.
    // If you need explicit refresh, you'd typically use StateFlow + trigger function.
    // So, remove the retry button logic calling refreshCategories for now.
    fun refreshCategories() {
        // This is tricky with callbackFlow + asLiveData.
        // A better approach for manual refresh involves StateFlow or other triggers.
        // For now, let's assume re-subscribing (navigating back/forth) or underlying repo retry handles it.
        Log.d("HomeViewModel", "refreshCategories called - relying on active observer for now.")
    }

    // Optional: Add functions later for handling topic clicks if logic is needed here
    // fun onTopicClicked(topicId: String) { ... }
}