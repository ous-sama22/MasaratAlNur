package com.oussama.masaratalnur.ui.admin.category.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.oussama.masaratalnur.R
import com.oussama.masaratalnur.data.model.Category
import com.oussama.masaratalnur.databinding.FragmentAdminCategoryListBinding
import com.oussama.masaratalnur.ui.admin.AdminActivity
import com.oussama.masaratalnur.ui.admin.adapter.AdminCategoryAdapter
import com.oussama.masaratalnur.ui.admin.adapter.AdminListClickListener
import com.oussama.masaratalnur.ui.admin.category.AdminCategoryViewModel
import com.oussama.masaratalnur.ui.admin.category.model.AdminCategoryListState

class AdminCategoryListFragment : Fragment(), AdminListClickListener<Category> {

    private var _binding: FragmentAdminCategoryListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminCategoryViewModel by viewModels()
    private lateinit var categoryAdapter: AdminCategoryAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminCategoryListBinding.inflate(inflater, container, false)
        (activity as? AdminActivity)?.supportActionBar?.title = getString(R.string.admin_categories_title)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()

        binding.fabAddCategory.setOnClickListener {
            // Navigate to Add/Edit screen with no categoryId (signals Add mode)
            val action = AdminCategoryListFragmentDirections.actionAdminCategoryListFragmentToAdminAddEditCategoryFragment(null)
            findNavController().navigate(action)
        }
    }

    private fun setupRecyclerView() {
        categoryAdapter = AdminCategoryAdapter(this) // Pass listener
        binding.recyclerViewAdminCategories.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = categoryAdapter
            // Add dividers
            addItemDecoration(
                MaterialDividerItemDecoration(
                    requireContext(),
                    LinearLayoutManager.VERTICAL
                )
            )
        }
    }

    private fun observeViewModel() {
        viewModel.listState.observe(viewLifecycleOwner) { state ->
            binding.progressBarAdminCategories.isVisible = state is AdminCategoryListState.Loading
            binding.recyclerViewAdminCategories.isVisible = state is AdminCategoryListState.Success
            binding.textViewAdminListEmpty.isVisible = state is AdminCategoryListState.Empty
            binding.textViewAdminListError.isVisible = state is AdminCategoryListState.Error

            when (state) {
                is AdminCategoryListState.Success -> categoryAdapter.submitList(state.categories)
                is AdminCategoryListState.Error -> binding.textViewAdminListError.text = state.message
                is AdminCategoryListState.Empty -> binding.textViewAdminListEmpty.text = "No categories found." // Use string resource
                is AdminCategoryListState.Loading -> { /* Handled by progress bar */ }
            }
        }
    }

    // Implementation of AdminListClickListener
    override fun onEditClicked(item: Category) {
        // Navigate to Add/Edit screen, passing the categoryId for Edit mode
        val action = AdminCategoryListFragmentDirections.actionAdminCategoryListFragmentToAdminAddEditCategoryFragment(item.id)
        findNavController().navigate(action)
    }

    override fun onDeleteClicked(item: Category) {
        // Show confirmation dialog before calling ViewModel delete
        showDeleteConfirmationDialog(item)
    }

    private fun showDeleteConfirmationDialog(category: Category) {
        // Use MaterialAlertDialogBuilder
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.confirm_delete_title))
            .setMessage(getString(R.string.confirm_delete_message))
            .setNegativeButton(getString(R.string.button_cancel), null) // Dismisses dialog
            .setPositiveButton(getString(R.string.button_confirm)) { _, _ ->
                viewModel.deleteCategory(category.id) // Call delete on confirmation
                // Optionally show a temporary loading/toast here, or observe formEvent for delete success/error
            }
            .show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerViewAdminCategories.adapter = null // Clear adapter
        _binding = null
    }
}