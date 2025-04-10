package com.oussama.masaratalnur.ui.home // Adjust package name if different

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible // Import for easy visibility toggling
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels // Import ktx delegate
import androidx.navigation.fragment.findNavController // Import for navigation later
import androidx.recyclerview.widget.LinearLayoutManager // Import LayoutManager
import com.oussama.masaratalnur.R
import com.oussama.masaratalnur.data.model.Topic
import com.oussama.masaratalnur.databinding.FragmentHomeBinding
import com.oussama.masaratalnur.ui.adapter.TopicAdapter // Import adapter
import com.oussama.masaratalnur.data.model.HomeUiState // Import UI State
import com.oussama.masaratalnur.ui.viewmodel.HomeViewModel // Import ViewModel
import com.oussama.masaratalnur.util.TopicClickListener

class HomeFragment : Fragment(), TopicClickListener { // Implement the click listener interface

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Get ViewModel instance
    private val homeViewModel: HomeViewModel by viewModels()

    // Declare adapter instance
    private lateinit var topicAdapter: TopicAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        // Initialize the adapter, passing 'this' as the click listener
        topicAdapter = TopicAdapter(this)

        binding.recyclerViewTopics.apply { // Assuming RecyclerView ID in fragment_home.xml is recyclerViewTopics
            layoutManager = LinearLayoutManager(context) // Use a linear layout
            adapter = topicAdapter
            // Optional: Add item decoration for spacing later
            // addItemDecoration(...)
        }
    }

    private fun observeViewModel() {
        homeViewModel.homeState.observe(viewLifecycleOwner) { state ->
            // Handle different UI states
            binding.progressBarHome.isVisible = state is HomeUiState.Loading // Show progress bar when loading
            binding.recyclerViewTopics.isVisible = state is HomeUiState.Success // Show recycler view on success
            binding.textEmptyState.isVisible = state is HomeUiState.Empty // Show empty message if list is empty
            binding.textErrorState.isVisible = state is HomeUiState.Error // Show error message on error

            when (state) {
                is HomeUiState.Success -> {
                    Log.d("HomeFragment", "Received ${state.topics.size} topics")
                    // Submit the list to the ListAdapter
                    topicAdapter.submitList(state.topics)
                }
                is HomeUiState.Error -> {
                    Log.e("HomeFragment", "Error loading topics: ${state.message}")
                    binding.textErrorState.text = state.message // Display specific error
                    // Optionally add a retry button
                }
                is HomeUiState.Empty -> {
                    Log.d("HomeFragment", "Received empty topic list")
                    binding.textEmptyState.text = getString(R.string.home_empty_message) // Define string
                }
                is HomeUiState.Loading -> {
                    Log.d("HomeFragment", "Loading topics...")
                }
            }
        }
    }

    // --- TopicClickListener Implementation ---
    override fun onTopicClicked(topic: Topic) {
        Toast.makeText(context, "Clicked on: ${topic.title_ar}", Toast.LENGTH_SHORT).show()
        Log.d("HomeFragment", "Topic clicked: ID=${topic.id}, Title=${topic.title_ar}")
        // TODO: Navigate to LessonListFragment, passing topic.id
        // val action = HomeFragmentDirections.actionHomeFragmentToLessonListFragment(topic.id)
        // findNavController().navigate(action)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        // Important: Remove adapter reference to avoid leaks if RecyclerView is reused
        // binding.recyclerViewTopics.adapter = null // Good practice, though binding cleans up views
        _binding = null
    }
}