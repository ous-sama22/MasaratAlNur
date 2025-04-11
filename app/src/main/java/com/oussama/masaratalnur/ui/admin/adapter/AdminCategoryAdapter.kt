package com.oussama.masaratalnur.ui.admin.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.oussama.masaratalnur.data.model.Category
import com.oussama.masaratalnur.databinding.ListItemAdminCategoryBinding

class AdminCategoryAdapter(private val listener: AdminListClickListener<Category>) :
    ListAdapter<Category, AdminCategoryAdapter.ViewHolder>(CategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ListItemAdminCategoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), listener)
    }

    class ViewHolder(private val binding: ListItemAdminCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(category: Category, listener: AdminListClickListener<Category>) {
            binding.textAdminCategoryTitle.text = category.title_ar
            binding.textAdminCategoryDesc.text = category.description_ar
            binding.buttonEditCategory.setOnClickListener { listener.onEditClicked(category) }
            binding.buttonDeleteCategory.setOnClickListener { listener.onDeleteClicked(category) }
            binding.root.setOnClickListener { listener.onEditClicked(category) }
        }
    }

    class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean = oldItem == newItem
    }
}