package com.oussama.masaratalnur.ui.topic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.oussama.masaratalnur.databinding.FragmentTopicListBinding

class TopicListFragment : Fragment() {
    private var _binding: FragmentTopicListBinding? = null
    private val binding get() = _binding!!
    private val args: TopicListFragmentArgs by navArgs() // Safe Args delegate

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTopicListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val categoryId = args.categoryId
        binding.textTopicListPlaceholder.text = "Topics for Category ID: $categoryId" // Update placeholder text
        // TODO: Setup RecyclerView, ViewModel for topics using categoryId
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}