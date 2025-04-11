package com.oussama.masaratalnur.ui.admin.category

import android.util.Log
import androidx.lifecycle.*
import com.oussama.masaratalnur.data.model.Category
import com.oussama.masaratalnur.data.repository.ContentRepository
import com.oussama.masaratalnur.data.model.ContentResult
import com.oussama.masaratalnur.di.ServiceLocator
import com.oussama.masaratalnur.ui.admin.category.model.AdminCategoryDetailState
import com.oussama.masaratalnur.ui.admin.category.model.AdminCategoryFormEvent
import com.oussama.masaratalnur.ui.admin.category.model.AdminCategoryListState
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch




class AdminCategoryViewModel : ViewModel() {

    private val repository: ContentRepository = ServiceLocator.provideContentRepository()

    // For List Fragment
    private val _listState = MutableLiveData<AdminCategoryListState>()
    val listState: LiveData<AdminCategoryListState> get() = _listState

    // For Add/Edit Fragment Form Status
    private val _formEvent = MutableLiveData<AdminCategoryFormEvent>(AdminCategoryFormEvent.Idle)
    val formEvent: LiveData<AdminCategoryFormEvent> get() = _formEvent

    // --- LiveData for Single Category Detail (for Edit screen) ---
    private val _categoryDetailState = MutableLiveData<AdminCategoryDetailState>()
    val categoryDetailState: LiveData<AdminCategoryDetailState> get() = _categoryDetailState

    // --- Job to track the category detail collection ---
    private var categoryDetailJob: Job? = null

    // --- List Logic ---
    init {
        loadCategories() // Load initially
    }

    fun loadCategories() {
        _listState.value = AdminCategoryListState.Loading
        viewModelScope.launch {
            // Collect only the first result for simplicity, or handle Flow properly
            repository.getAllCategories().collect { result ->
                when (result) {
                    is ContentResult.Loading -> _listState.postValue(AdminCategoryListState.Loading) // Keep loading
                    is ContentResult.Error -> _listState.postValue(AdminCategoryListState.Error(result.exception.message ?: "Error"))
                    is ContentResult.Success -> {
                        _listState.postValue(
                            if (result.data.isEmpty()) AdminCategoryListState.Empty
                            else AdminCategoryListState.Success(result.data)
                        )
                    }
                }
                // If using collect, flow might keep emitting. Need to handle this if not desired.
                // Maybe use .first() or take(1) if only initial load needed? Or manage collection manually.
                // For now, let's assume this collect works okay for basic list display.
            }
        }
    }

    // --- Detail Logic (for Edit screen) ---
    fun loadCategoryDetails(categoryId: String?) {
        if (categoryId == null || categoryId.isBlank()) {
            _categoryDetailState.value = AdminCategoryDetailState.Error("Invalid Category ID for edit.")
            return
        }

        _categoryDetailState.value = AdminCategoryDetailState.Loading
        _categoryDetailState.value = AdminCategoryDetailState.Loading
        // Cancel any previous detail loading job
        categoryDetailJob?.cancel()
        // Start and store the new job
        categoryDetailJob = viewModelScope.launch {
            Log.d("AdminCategoryViewModel", "Starting collection for Category ID: $categoryId")
            repository.getCategory(categoryId)
                .distinctUntilChanged()
                .catch { e -> // Catch exceptions within the flow collection itself
                    Log.e("AdminCategoryViewModel", "Exception collecting category details for $categoryId", e)
                    _categoryDetailState.postValue(AdminCategoryDetailState.Error(e.message ?: "Collection error"))
                }
                .collect { result ->
                    Log.d("AdminCategoryViewModel", "Received result for Category ID $categoryId: $result")
                    // Map ContentResult to DetailState
                    when (result) {
                        is ContentResult.Loading -> _categoryDetailState.postValue(AdminCategoryDetailState.Loading) // Should ideally be handled before collect if possible
                        is ContentResult.Error -> {
                            if (result.exception is NoSuchElementException) {
                                _categoryDetailState.postValue(AdminCategoryDetailState.NotFound)
                            } else {
                                _categoryDetailState.postValue(AdminCategoryDetailState.Error(result.exception.message ?: "Failed to load details"))
                            }
                        }
                        is ContentResult.Success -> _categoryDetailState.postValue(AdminCategoryDetailState.Success(result.data))
                    }
                }
        }
        // Log when the job completes or is cancelled (for debugging)
        categoryDetailJob?.invokeOnCompletion { throwable ->
            Log.d("AdminCategoryViewModel", "Category detail job for $categoryId completed/cancelled. Cause: ${throwable?.message}")
        }

    }

    // --- Form Logic (Add/Edit/Delete) ---

    fun saveCategory(category: Category) {
        resetDetailStateToIdle()
        _formEvent.value = AdminCategoryFormEvent.Loading
        viewModelScope.launch {
            val result: Result<Unit> = if (category.id.isBlank()) {
                // Adding new category
                repository.addCategory(category)
            } else {
                // Updating existing category
                repository.updateCategory(category)
            }

            if (result.isSuccess) {
                _formEvent.postValue(AdminCategoryFormEvent.Success)
            } else {
                _formEvent.postValue(AdminCategoryFormEvent.Error(result.exceptionOrNull()?.message ?: "Save failed"))
            }
        }
    }

    fun deleteCategory(categoryId: String) {
        Log.d("AdminCategoryViewModel", "deleteCategory called for ID: $categoryId")
        // 1. Cancel the detail listener job immediately
        categoryDetailJob?.cancel()
        Log.d("AdminCategoryViewModel", "Cancelled category detail job.")
        // 2. Reset the detail state so the UI doesn't linger on old data
        _categoryDetailState.value = AdminCategoryDetailState.Idle
        Log.d("AdminCategoryViewModel", "Reset category detail state to Idle.")
        // 3. Set Form state to Loading
        _formEvent.value = AdminCategoryFormEvent.Loading
        viewModelScope.launch {
            val result = repository.deleteCategory(categoryId)
            if (result.isSuccess) {
                _formEvent.postValue(AdminCategoryFormEvent.DeleteSuccess) // Specific success state for delete
            } else {
                _formEvent.postValue(AdminCategoryFormEvent.Error(result.exceptionOrNull()?.message ?: "Delete failed"))
            }
        }
    }

    fun resetFormEvent() {
        // Only reset if not currently loading
        if (_formEvent.value !is AdminCategoryFormEvent.Loading) _formEvent.value = AdminCategoryFormEvent.Idle
    }

    fun resetDetailStateToIdle() {
        // Cancel job if resetting state manually
        categoryDetailJob?.cancel()
        if (_categoryDetailState.value != AdminCategoryDetailState.Idle) {
            _categoryDetailState.value = AdminCategoryDetailState.Idle
            Log.d("AdminCategoryViewModel", "Resetting Detail State to Idle (manually)")
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Ensure the job is cancelled when the ViewModel is destroyed
        categoryDetailJob?.cancel()
        Log.d("AdminCategoryViewModel", "ViewModel cleared, cancelling detail job.")
    }
}