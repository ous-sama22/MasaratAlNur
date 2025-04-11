package com.oussama.masaratalnur.ui.admin.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.oussama.masaratalnur.R
import com.oussama.masaratalnur.databinding.FragmentAdminDashboardBinding
import com.oussama.masaratalnur.ui.admin.AdminActivity

class AdminDashboardFragment : Fragment() {

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        (activity as? AdminActivity)?.supportActionBar?.title = getString(R.string.admin_dashboard_title) // Set toolbar title
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonManageCategories.setOnClickListener {
            findNavController().navigate(R.id.action_adminDashboardFragment_to_adminCategoryListFragment) // Define action
        }
        // TODO: Add listeners for Topics, Lessons, Quizzes buttons to navigate to their respective list fragments
        binding.buttonManageTopics.setOnClickListener { /* navigate to topic list */ }
        binding.buttonManageLessons.setOnClickListener { /* navigate to lesson list */ }
        binding.buttonManageQuizzes.setOnClickListener { /* navigate to quiz list */ }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}