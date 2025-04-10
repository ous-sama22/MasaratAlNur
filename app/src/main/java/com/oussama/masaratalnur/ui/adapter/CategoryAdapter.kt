package com.oussama.masaratalnur.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.oussama.masaratalnur.R
import com.oussama.masaratalnur.data.model.Category // Use Category model
import com.oussama.masaratalnur.databinding.ListItemCategoryBinding // Use Category item binding
import com.oussama.masaratalnur.util.CategoryClickListener

class CategoryAdapter(private val clickListener: CategoryClickListener? = null) :
    ListAdapter<Category, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ListItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class CategoryViewHolder(private val binding: ListItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Category, listener: CategoryClickListener?) {
            binding.textCategoryTitle.text = category.title_ar

            binding.imageCategory.load(category.imageUrl) {
                placeholder(R.drawable.ic_launcher_background) // Update placeholder
                error(R.drawable.ic_launcher_background) // Update error drawable
                crossfade(true)
            }

            binding.root.setOnClickListener {
                listener?.onCategoryClicked(category)
            }
        }
    }

    class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem == newItem
        }
    }
}