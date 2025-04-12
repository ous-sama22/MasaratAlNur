package com.oussama.masaratalnur.data.model

import androidx.recyclerview.widget.DiffUtil

// DiffUtil for efficient list updates
class LessonDiffCallback : DiffUtil.ItemCallback<Lesson>() {
    override fun areItemsTheSame(oldItem: Lesson, newItem: Lesson): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Lesson, newItem: Lesson): Boolean {
        return oldItem == newItem // Relies on data class equals()
    }
}