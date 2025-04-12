package com.oussama.masaratalnur.ui.topic

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels // Use delegate
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.oussama.masaratalnur.R
import com.oussama.masaratalnur.data.model.Topic
import com.oussama.masaratalnur.data.model.TopicListUiState
import com.oussama.masaratalnur.databinding.FragmentTopicListBinding
import com.oussama.masaratalnur.ui.adapter.TopicAdapter // Use existing adapter
import com.oussama.masaratalnur.ui.viewmodel.TopicListViewModel // Import ViewModel
import com.oussama.masaratalnur.util.TopicClickListener // Import click listener
import kotlinx.coroutines.ExperimentalCoroutinesApi // Import for ViewModel annotation

@ExperimentalCoroutinesApi // Needed because ViewModel uses Experimental API
class TopicListFragment : Fragment(), TopicClickListener { // Implement click listener

    private var _binding: FragmentTopicListBinding? = null
    private val binding get() = _binding!!


    // Use viewModels delegate to get ViewModel instance
    private val topicListViewModel: TopicListViewModel by viewModels()

    private lateinit var topicAdapter: TopicAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTopicListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        // TODO: Setup Toolbar title if needed using category info (might need another ViewModel call or pass category name via args)
    }

    private fun setupRecyclerView() {
        topicAdapter = TopicAdapter(this) // Pass 'this' as click listener
        binding.recyclerViewTopics.apply {
            // Use LinearLayoutManager or GridLayoutManager as desired
            layoutManager = LinearLayoutManager(context)
            adapter = topicAdapter
        }
    }

    private fun observeViewModel() {
        topicListViewModel.topicListState.observe(viewLifecycleOwner) { state ->
            binding.progressBarTopics.isVisible = state is TopicListUiState.Loading
            binding.recyclerViewTopics.isVisible = state is TopicListUiState.Success
            binding.textEmptyTopics.isVisible = state is TopicListUiState.Empty
            binding.textErrorTopics.isVisible = state is TopicListUiState.Error
            // Add Retry button visibility/logic if desired

            when (state) {
                is TopicListUiState.Success -> {
                    Log.d("TopicListFragment", "Received ${state.topics.size} topics")
                    topicAdapter.submitList(state.topics)
                }
                is TopicListUiState.Error -> {
                    Log.e("TopicListFragment", "Error loading topics: ${state.message}")
                    binding.textErrorTopics.text = state.message
                    // Optionally show retry button
                }
                is TopicListUiState.Empty -> {
                    Log.d("TopicListFragment", "Received empty topic list")
                    binding.textEmptyTopics.text = getString(R.string.topics_empty_message) // Define this string
                }
                is TopicListUiState.Loading -> {
                    Log.d("TopicListFragment", "Loading topics...")
                }
            }
        }
    }

    // --- TopicClickListener Implementation ---
    override fun onTopicClicked(topic: Topic) {
        Toast.makeText(context, "Clicked Topic: ${topic.title_ar}", Toast.LENGTH_SHORT).show()
        Log.d("TopicListFragment", "Topic clicked: ID=${topic.id}, Title=${topic.title_ar}")

         val action = TopicListFragmentDirections.actionTopicListFragmentToLessonListFragment(topicId = topic.id)
         findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}