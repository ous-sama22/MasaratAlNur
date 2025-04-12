package com.oussama.masaratalnur.ui.viewmodel

import androidx.lifecycle.* // Import necessary lifecycle components
import com.oussama.masaratalnur.data.model.ContentResult
import com.oussama.masaratalnur.data.model.Topic
import com.oussama.masaratalnur.data.model.TopicListUiState
import com.oussama.masaratalnur.data.repository.ContentRepository
import com.oussama.masaratalnur.di.ServiceLocator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.* // Import flow operators

// Opt-in for experimental flatMapLatest
@ExperimentalCoroutinesApi
class TopicListViewModel(
    savedStateHandle: SavedStateHandle // Inject SavedStateHandle
) : ViewModel() {

    private val contentRepository: ContentRepository = ServiceLocator.provideContentRepository() // Need this in ServiceLocator

    // Get categoryId safely from SavedStateHandle (passed via navigation args)
    private val categoryIdFlow: StateFlow<String> = savedStateHandle.getStateFlow("categoryId", "") // Key must match nav graph argument name

    // Use categoryIdFlow to trigger fetching topics whenever it changes (or has a valid value)
    // flatMapLatest ensures that if categoryId changes rapidly, only the latest fetch is active.
    val topicListState: LiveData<TopicListUiState> = categoryIdFlow
        .filter { it.isNotBlank() } // Only proceed if categoryId is valid
        .flatMapLatest { catId -> // Use flatMapLatest
            contentRepository.getTopicsForCategory(catId) // Fetch topics
                .map { result -> // Map ContentResult to TopicListUiState
                    when (result) {
                        is ContentResult.Loading -> TopicListUiState.Loading
                        is ContentResult.Success -> {
                            if (result.data.isEmpty()) TopicListUiState.Empty
                            else TopicListUiState.Success(result.data)
                        }
                        is ContentResult.Error -> TopicListUiState.Error(
                            result.exception.message ?: "Failed to load topics"
                        )
                    }
                }
        }.asLiveData() // Convert the final Flow<TopicListUiState> to LiveData

    // No explicit refresh needed here as it reacts to categoryId,
    // but could add one if manual refresh is desired later.
}
