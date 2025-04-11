package com.oussama.masaratalnur.ui.admin.adapter

// Generic listener for admin list items
interface AdminListClickListener<T> {
    fun onEditClicked(item: T)
    fun onDeleteClicked(item: T)
}