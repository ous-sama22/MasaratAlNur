package com.oussama.masaratalnur.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.oussama.masaratalnur.data.repository.ContentRepository
import com.oussama.masaratalnur.data.model.ContentResult // Import ContentResult
import com.oussama.masaratalnur.data.model.HomeUiState
import com.oussama.masaratalnur.di.ServiceLocator
import kotlinx.coroutines.flow.map // Use Flow's map operator

class HomeViewModel : ViewModel() {

    // Get repository via ServiceLocator (Manual DI)
    private val contentRepository: ContentRepository = ServiceLocator.provideContentRepository()

    // Fetch topics and transform the ContentResult Flow into HomeUiState LiveData
    val homeState: LiveData<HomeUiState> = contentRepository.getAllTopics()
        .map { result -> // Use Flow's map operator to transform ContentResult to HomeUiState
            when (result) {
                is ContentResult.Loading -> HomeUiState.Loading
                is ContentResult.Error -> HomeUiState.Error(result.exception.message ?: "Failed to load topics")
                is ContentResult.Success -> {
                    if (result.data.isEmpty()) {
                        HomeUiState.Empty // Specific state for empty list
                    } else {
                        HomeUiState.Success(result.data)
                    }
                }
            }
        }
        .asLiveData() // Convert the transformed Flow<HomeUiState> to LiveData

    // Optional: Add functions later for handling topic clicks if logic is needed here
    // fun onTopicClicked(topicId: String) { ... }
}