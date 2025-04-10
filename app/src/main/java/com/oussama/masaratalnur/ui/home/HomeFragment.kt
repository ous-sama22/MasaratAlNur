package com.oussama.masaratalnur.ui.home // Adjust package name if different

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
// TODO: Add ViewModel imports later
import com.oussama.masaratalnur.databinding.FragmentHomeBinding // Make sure this matches your layout file name

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // TODO: Declare ViewModel later

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: Initialize ViewModel and Observe LiveData later
        // TODO: Setup RecyclerView for topics later
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}