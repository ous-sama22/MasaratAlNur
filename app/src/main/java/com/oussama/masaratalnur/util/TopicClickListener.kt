package com.oussama.masaratalnur.util

import com.oussama.masaratalnur.data.model.Topic

// Define click listener interface (optional but good practice)
interface TopicClickListener {
    fun onTopicClicked(topic: Topic)
}