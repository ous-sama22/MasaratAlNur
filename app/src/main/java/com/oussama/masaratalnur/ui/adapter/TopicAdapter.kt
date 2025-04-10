package com.oussama.masaratalnur.ui.adapter // Adjust package if needed

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load // Using Coil for image loading - add dependency if needed
import com.oussama.masaratalnur.R // Import R
import com.oussama.masaratalnur.data.model.Topic
import com.oussama.masaratalnur.databinding.ListItemTopicBinding // Import ViewBinding for the item layout
import com.oussama.masaratalnur.util.TopicClickListener

class TopicAdapter(private val clickListener: TopicClickListener? = null) :
    ListAdapter<Topic, TopicAdapter.TopicViewHolder>(TopicDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicViewHolder {
        // Inflate the item layout using ViewBinding
        val binding = ListItemTopicBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TopicViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TopicViewHolder, position: Int) {
        val topic = getItem(position) // Get item using ListAdapter's method
        holder.bind(topic, clickListener)
    }

    // ViewHolder class to hold references to the views within the item layout
    class TopicViewHolder(private val binding: ListItemTopicBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(topic: Topic, listener: TopicClickListener?) {
            binding.textTopicTitle.text = topic.title_ar
            binding.textTopicDescription.text = topic.description_ar

            // Load image using Coil (or Glide)
            if (!topic.imageUrl.isNullOrBlank()) {
                binding.imageTopic.load(topic.imageUrl) {
                    placeholder(R.drawable.ic_launcher_background) // Placeholder drawable
                    error(R.drawable.ic_launcher_background) // Error drawable
                    // transformations(CircleCropTransformation()) // Optional: Make image circular
                    crossfade(true) // Optional: Fade animation
                }
            } else {
                // Set default image if URL is missing
                binding.imageTopic.setImageResource(R.drawable.ic_launcher_background) // Default placeholder
            }

            // Set click listener on the whole card item
            binding.root.setOnClickListener {
                listener?.onTopicClicked(topic)
            }
        }
    }

    // DiffUtil.ItemCallback for efficient list updates
    class TopicDiffCallback : DiffUtil.ItemCallback<Topic>() {
        override fun areItemsTheSame(oldItem: Topic, newItem: Topic): Boolean {
            return oldItem.id == newItem.id // Check if IDs are the same
        }

        override fun areContentsTheSame(oldItem: Topic, newItem: Topic): Boolean {
            return oldItem == newItem // Check if the data content is the same (data class implements equals)
        }
    }
}