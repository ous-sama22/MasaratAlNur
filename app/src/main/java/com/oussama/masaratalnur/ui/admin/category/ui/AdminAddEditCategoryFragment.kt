package com.oussama.masaratalnur.ui.admin.category.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.oussama.masaratalnur.R
import com.oussama.masaratalnur.data.model.Category
import com.oussama.masaratalnur.databinding.FragmentAdminAddEditCategoryBinding
import com.oussama.masaratalnur.ui.admin.AdminActivity
import com.oussama.masaratalnur.ui.admin.category.AdminCategoryViewModel
import com.oussama.masaratalnur.ui.admin.category.model.AdminCategoryDetailState
import com.oussama.masaratalnur.ui.admin.category.model.AdminCategoryFormEvent

class AdminAddEditCategoryFragment : Fragment() {

    private var _binding: FragmentAdminAddEditCategoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminCategoryViewModel by viewModels()
    private val args: AdminAddEditCategoryFragmentArgs by navArgs() // Safe Args delegate

    private var categoryToEdit: Category? = null // Store the loaded category for updates/delete


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminAddEditCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews() // Setup listeners first
        observeViewModel() // Start observing form events and detail state

        val categoryId = args.categoryId
        if (categoryId == null) {
            // --- ADD Mode ---
            (activity as? AdminActivity)?.supportActionBar?.title =
                getString(R.string.admin_add_category)
            binding.buttonAdminDelete.visibility = View.GONE // Hide delete
        } else {
            // --- EDIT Mode ---
            (activity as? AdminActivity)?.supportActionBar?.title =
                getString(R.string.admin_edit_category)
            // Load the category details
            viewModel.loadCategoryDetails(categoryId)
            binding.buttonAdminDelete.visibility = View.VISIBLE // Show delete


        }
    }


    private fun setupViews() {
        binding.buttonAdminSave.setOnClickListener {
            saveCategoryData()
        }
        // Listener for the Delete button within the Edit screen
        binding.buttonAdminDelete.setOnClickListener {
            Log.d("AddEditCategory", "Delete button clicked Number one")
            // Ensure we have loaded the category data before allowing delete
            categoryToEdit?.let { category ->
                Log.d("AddEditCategory", "Delete button clicked for category ID: ${category.id}")
                showDeleteConfirmationDialog(category) // Show confirmation dialog
            } ?: run {
                // Should not happen if button is only visible in edit mode after loading, but good practice
                Log.w("AddEditCategory", "Delete button clicked but categoryToEdit is null.")
                Toast.makeText(context, "Cannot delete, category data not loaded.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        // Observe Form Submission Events
        viewModel.formEvent.observe(viewLifecycleOwner) { event ->

            val isFormLoading = event is AdminCategoryFormEvent.Loading
            setFormEnabled(!isFormLoading) // Ensure form enabled state reflects form submission loading too
            binding.progressBarAdminForm.isVisible = isFormLoading // Use form progress bar here

            when (event) {
                is AdminCategoryFormEvent.Success, is AdminCategoryFormEvent.DeleteSuccess -> {
                    // Show success message and navigate back
                    val messageResId =
                        if (event is AdminCategoryFormEvent.Success) R.string.item_saved_success else R.string.item_deleted_success
                    Toast.makeText(context, getString(messageResId), Toast.LENGTH_SHORT).show()
                    viewModel.resetDetailStateToIdle()
                    findNavController().popBackStack()
                }

                is AdminCategoryFormEvent.Error -> {
                    // Show error message (e.g., in a Snackbar or Dialog for more visibility?)
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Error")
                        .setMessage(
                            getString(
                                R.string.error_saving_item,
                                event.message
                            )
                        ) // Generic message for save/delete errors
                        .setPositiveButton(android.R.string.ok, null)
                        .show()
                    viewModel.resetFormEvent() // Reset state after showing error
                }

                else -> { /* Idle, Loading handled by visibility/enabled state */
                }
            }
        }

        // Observe Category Detail Loading State (for Edit mode)
        viewModel.categoryDetailState.observe(viewLifecycleOwner) { state ->

            binding.progressBarAdminForm.isVisible = state is AdminCategoryDetailState.Loading

            when (state) {
                is AdminCategoryDetailState.Idle -> {
                    Log.d("AddEditCategory", "Observed Detail State: Idle")
                    // Ensure form isn't stuck disabled if we navigated here in Add mode
                    // Only enable if categoryToEdit is null (Add mode)
                    if (categoryToEdit == null) {
                        setFormEnabled(true)
                    }
                    // Otherwise, do nothing, wait for Loading/Success/Error
                }
                is AdminCategoryDetailState.Success -> {
                    Log.d("AddEditCategory", "State Success observed. Populating form.")

                    populateForm(state.category) // Sets categoryToEdit
                    // Now explicitly enable the form AFTER populating
                    setFormEnabled(true)
                    binding.buttonAdminDelete.visibility = View.VISIBLE
                }

                is AdminCategoryDetailState.Error -> {
                    binding.progressBarAdminForm.isVisible = false // Ensure progress hidden
                    // Show error loading details - prevent saving
                    Toast.makeText(
                        context,
                        "Error loading category: ${state.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    setFormEnabled(false) // Explicitly disable on error
                    binding.buttonAdminDelete.visibility = View.GONE // Hide delete if load fails
                }

                is AdminCategoryDetailState.NotFound -> {
                    binding.progressBarAdminForm.isVisible = false
                    Toast.makeText(context, "Category not found.", Toast.LENGTH_LONG).show()
                    setFormEnabled(false) // Explicitly disable on error
                    binding.buttonAdminDelete.visibility = View.GONE // Hide delete if load fails
                }

                is AdminCategoryDetailState.Loading -> {
                    // Explicitly disable form while loading details
                    setFormEnabled(false)
                }
            }
        }
    }

    // Helper function to populate form fields
    private fun populateForm(category: Category) {
        this.categoryToEdit = category // Store for potential update/delete
        Log.d("AddEditCategory", "populateForm: categoryToEdit set with ID: ${this.categoryToEdit?.id}")
        binding.editTextCategoryTitle.setText(category.title_ar)
        binding.editTextCategoryDesc.setText(category.description_ar)
        binding.editTextCategoryOrder.setText(category.order.toString())
        binding.editTextCategoryImageUrl.setText(category.imageUrl ?: "")
    }

    // Helper function to enable/disable form elements during loading
    private fun setFormEnabled(isEnabled: Boolean) {
        Log.d("AddEditCategory", "setFormEnabled called with: $isEnabled. categoryToEdit is null? ${categoryToEdit == null}")
        binding.inputLayoutCategoryTitle.isEnabled = isEnabled
        binding.inputLayoutCategoryDesc.isEnabled = isEnabled
        binding.inputLayoutCategoryOrder.isEnabled = isEnabled
        binding.inputLayoutCategoryImageUrl.isEnabled = isEnabled
        binding.buttonAdminSave.isEnabled = isEnabled
        // Delete only enabled if form is enabled AND category has been loaded
        binding.buttonAdminDelete.isEnabled = isEnabled && categoryToEdit != null
        Log.d("AddEditCategory", "setFormEnabled: delete button isEnabled set to ${binding.buttonAdminDelete.isEnabled}") // Add logging
    }


    private fun saveCategoryData() {
        // --- Validation (remains same) ---
        val title = binding.editTextCategoryTitle.text.toString().trim()
        val desc = binding.editTextCategoryDesc.text.toString().trim()
        val orderStr = binding.editTextCategoryOrder.text.toString().trim()
        val imageUrl = binding.editTextCategoryImageUrl.text.toString().trim().ifBlank { null }

        var isValid = true
        if (title.isEmpty()) { /* ... set error ... */ isValid = false
        } else {
            binding.inputLayoutCategoryTitle.error = null
        }
        // ... other validation ...
        val order = orderStr.toIntOrNull()
        if (order == null) { /* ... set error ... */ isValid = false
        } else {
            binding.inputLayoutCategoryOrder.error = null
        }
        if (!isValid) return
        // --- End Validation ---

        // Use categoryToEdit?.id for existing ID, otherwise it's a new category (ID handled by Repo/Firestore)
        val categoryIdForSave = categoryToEdit?.id ?: ""

        val categoryToSave = Category(
            id = categoryIdForSave, // Pass existing ID if editing
            title_ar = title,
            description_ar = desc,
            order = order!!,
            imageUrl = imageUrl
        )

        viewModel.saveCategory(categoryToSave)
    }

    // Add confirmation dialog for delete button click
    private fun showDeleteConfirmationDialog(category: Category) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.confirm_delete_title)
            .setMessage(R.string.confirm_delete_message)
            .setNegativeButton(R.string.button_cancel, null)
            .setPositiveButton(R.string.button_confirm) { _, _ ->
                Log.d("AddEditCategory", "Delete confirmed for category ID: ${category.id}. Calling ViewModel.")
                // Call ViewModel to perform deletion
                viewModel.deleteCategory(category.id)
                // The observer for formEvent will handle navigation on DeleteSuccess
            }
            .show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}