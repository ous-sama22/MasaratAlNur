package com.oussama.masaratalnur.util

import com.oussama.masaratalnur.data.model.Category

interface CategoryClickListener { // Define listener
    fun onCategoryClicked(category: Category)
}