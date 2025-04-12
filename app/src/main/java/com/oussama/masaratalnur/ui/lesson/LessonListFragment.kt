package com.oussama.masaratalnur.ui.lesson

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
// No need for navArgs if using SavedStateHandle in ViewModel
import com.oussama.masaratalnur.R
import com.oussama.masaratalnur.data.model.Lesson
import com.oussama.masaratalnur.data.model.LessonListUiState
import com.oussama.masaratalnur.databinding.FragmentLessonListBinding // Correct binding
import com.oussama.masaratalnur.ui.adapter.LessonAdapter // Correct adapter
import com.oussama.masaratalnur.ui.viewmodel.LessonListViewModel // Correct ViewModel
import com.oussama.masaratalnur.util.LessonClickListener // Correct listener
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class LessonListFragment : Fragment(), LessonClickListener {

    private var _binding: FragmentLessonListBinding? = null
    private val binding get() = _binding!!

    private val lessonListViewModel: LessonListViewModel by viewModels()
    private lateinit var lessonAdapter: LessonAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLessonListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
        // TODO: Setup Toolbar title using Topic info (might need modification)
    }

    private fun setupRecyclerView() {
        lessonAdapter = LessonAdapter(this)
        binding.recyclerViewLessons.adapter = lessonAdapter
        // LayoutManager is set in XML, but can be set here too
    }

    private fun observeViewModel() {
        lessonListViewModel.lessonListState.observe(viewLifecycleOwner) { state ->
            binding.progressBarLessons.isVisible = state is LessonListUiState.Loading
            binding.recyclerViewLessons.isVisible = state is LessonListUiState.Success
            binding.textEmptyLessons.isVisible = state is LessonListUiState.Empty
            binding.textErrorLessons.isVisible = state is LessonListUiState.Error

            when (state) {
                is LessonListUiState.Success -> {
                    Log.d("LessonListFragment", "Received ${state.lessons.size} lessons")
                    lessonAdapter.submitList(state.lessons)
                }
                is LessonListUiState.Error -> {
                    Log.e("LessonListFragment", "Error loading lessons: ${state.message}")
                    binding.textErrorLessons.text = state.message
                }
                is LessonListUiState.Empty -> {
                    Log.d("LessonListFragment", "Received empty lesson list")
                    binding.textEmptyLessons.text = getString(R.string.lessons_empty_message) // Define string
                }
                is LessonListUiState.Loading -> {
                    Log.d("LessonListFragment", "Loading lessons...")
                }
            }
        }
    }

    // --- LessonClickListener Implementation ---
    override fun onLessonClick(lesson: Lesson) {
        Toast.makeText(context, "Clicked Lesson: ${lesson.title_ar}", Toast.LENGTH_SHORT).show()
        Log.d("LessonListFragment", "Lesson clicked: ID=${lesson.id}, Title=${lesson.title_ar}")
        // TODO: Navigate to LessonViewerFragment, passing lesson.id
        // val action = LessonListFragmentDirections.actionLessonListFragmentToLessonViewerFragment(lessonId = lesson.id)
        // findNavController().navigate(action)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}