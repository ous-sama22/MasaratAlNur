package com.oussama.masaratalnur.util

import com.oussama.masaratalnur.data.model.Lesson

interface LessonClickListener {
    fun onLessonClick(lesson: Lesson)
}