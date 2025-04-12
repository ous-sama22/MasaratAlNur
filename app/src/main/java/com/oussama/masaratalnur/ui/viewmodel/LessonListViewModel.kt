package com.oussama.masaratalnur.ui.viewmodel

import androidx.lifecycle.*
import com.oussama.masaratalnur.data.model.ContentResult
import com.oussama.masaratalnur.data.model.Lesson // Import Lesson model
import com.oussama.masaratalnur.data.model.LessonListUiState
import com.oussama.masaratalnur.data.repository.ContentRepository
import com.oussama.masaratalnur.di.ServiceLocator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@ExperimentalCoroutinesApi
class LessonListViewModel(
    savedStateHandle: SavedStateHandle // Inject SavedStateHandle
) : ViewModel() {

    private val contentRepository: ContentRepository = ServiceLocator.provideContentRepository()

    // Get topicId from SavedStateHandle (passed via navigation args)
    // Key "topicId" must match the argument name we define in the nav graph
    private val topicIdFlow: StateFlow<String> = savedStateHandle.getStateFlow("topicId", "")

    // Fetch lessons based on topicId
    val lessonListState: LiveData<LessonListUiState> = topicIdFlow
        .filter { it.isNotBlank() } // Proceed only if topicId is valid
        .flatMapLatest { topicId ->
            contentRepository.getLessonsForTopic(topicId) // Fetch lessons for this topic
                .map { result -> // Map ContentResult to LessonListUiState
                    when (result) {
                        is ContentResult.Loading -> LessonListUiState.Loading
                        is ContentResult.Success -> {
                            if (result.data.isEmpty()) LessonListUiState.Empty
                            else LessonListUiState.Success(result.data)
                        }
                        is ContentResult.Error -> LessonListUiState.Error(
                            result.exception.message ?: "Failed to load lessons"
                        )
                    }
                }
        }.asLiveData()
}