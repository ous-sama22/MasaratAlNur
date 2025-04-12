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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager // Import LayoutManager
import com.oussama.masaratalnur.R
import com.oussama.masaratalnur.data.model.Category
import com.oussama.masaratalnur.data.model.CategoryListUiState
import com.oussama.masaratalnur.data.model.Topic
import com.oussama.masaratalnur.databinding.FragmentHomeBinding
import com.oussama.masaratalnur.ui.adapter.TopicAdapter // Import adapter
import com.oussama.masaratalnur.data.model.HomeUiState // Import UI State
import com.oussama.masaratalnur.ui.adapter.CategoryAdapter
import com.oussama.masaratalnur.ui.viewmodel.HomeViewModel // Import ViewModel
import com.oussama.masaratalnur.util.CategoryClickListener
import com.oussama.masaratalnur.util.TopicClickListener

class HomeFragment : Fragment(), CategoryClickListener  { // Implement the click listener interface

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Get ViewModel instance
    private val homeViewModel: HomeViewModel by viewModels()

    // Declare adapter instance
    private lateinit var categoryAdapter: CategoryAdapter

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
        setupRetryButton()
    }

    private fun setupRecyclerView() {
        // Initialize the adapter, passing 'this' as the click listener
        categoryAdapter = CategoryAdapter(this)

        binding.recyclerViewCategories.apply { // Use correct ID
            // Use GridLayoutManager, get spanCount from resources if desired or keep hardcoded
            val spanCount = resources.getInteger(R.integer.category_grid_span_count)
            layoutManager = GridLayoutManager(context, spanCount)
            adapter = categoryAdapter
        }

    }

    private fun observeViewModel() {
        homeViewModel.categoryListState.observe(viewLifecycleOwner) { state -> // Observe renamed state
            binding.progressBarCategories.isVisible = state is CategoryListUiState.Loading
            binding.recyclerViewCategories.isVisible = state is CategoryListUiState.Success
            binding.textEmptyCategories.isVisible = state is CategoryListUiState.Empty
            binding.textErrorCategories.isVisible = state is CategoryListUiState.Error
            binding.buttonRetryCategories.isVisible = state is CategoryListUiState.Error // Show retry on error

            when (state) {
                is CategoryListUiState.Success -> {
                    Log.d("HomeFragment", "Received ${state.categories.size} categories")
                    categoryAdapter.submitList(state.categories) // Submit categories list
                }
                is CategoryListUiState.Error -> {
                    Log.e("HomeFragment", "Error loading categories: ${state.message}")
                    binding.textErrorCategories.text = state.message
                }
                is CategoryListUiState.Empty -> {
                    Log.d("HomeFragment", "Received empty category list")
                    binding.textEmptyCategories.text = getString(R.string.categories_empty_message)
                }
                is CategoryListUiState.Loading -> {
                    Log.d("HomeFragment", "Loading categories...")
                }
            }
        }
    }

    // Add retry button logic
    private fun setupRetryButton() {
        binding.buttonRetryCategories.setOnClickListener {
            Log.d("HomeFragment", "Retry button clicked")
            homeViewModel.refreshCategories() // We need to add this to HomeViewModel
        }
    }

    // --- CategoryClickListener Implementation ---
    override fun onCategoryClicked(category: Category) { // Method renamed
        Toast.makeText(context, "Clicked on: ${category.title_ar}", Toast.LENGTH_SHORT).show()
        Log.d("HomeFragment", "Category clicked: ID=${category.id}, Title=${category.title_ar}")

        val action = HomeFragmentDirections.actionHomeFragmentToTopicListFragment(categoryId = category.id)
        findNavController().navigate(action)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        // Important: Remove adapter reference to avoid leaks if RecyclerView is reused
        // binding.recyclerViewTopics.adapter = null // Good practice, though binding cleans up views
        _binding = null
    }
}