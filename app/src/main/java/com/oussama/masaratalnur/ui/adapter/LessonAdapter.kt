package com.oussama.masaratalnur.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.oussama.masaratalnur.data.model.Lesson
import com.oussama.masaratalnur.data.model.LessonDiffCallback
import com.oussama.masaratalnur.databinding.ListItemLessonBinding // Import generated binding class
import com.oussama.masaratalnur.util.LessonClickListener // Import click listener interface (needs creating)

class LessonAdapter(private val clickListener: LessonClickListener) :
    ListAdapter<Lesson, LessonAdapter.LessonViewHolder>(LessonDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonViewHolder {
        return LessonViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: LessonViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, clickListener)
    }

    class LessonViewHolder private constructor(private val binding: ListItemLessonBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Lesson, clickListener: LessonClickListener) {
            binding.textLessonTitle.text = item.title_ar
            binding.textLessonXp.text = "+${item.xpAward} XP" // Example XP display
            // Set click listener on the root view or specific elements
            binding.root.setOnClickListener {
                clickListener.onLessonClick(item)
            }
            // TODO: Add logic to show completion state later
        }

        companion object {
            fun from(parent: ViewGroup): LessonViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemLessonBinding.inflate(layoutInflater, parent, false)
                return LessonViewHolder(binding)
            }
        }
    }
}
