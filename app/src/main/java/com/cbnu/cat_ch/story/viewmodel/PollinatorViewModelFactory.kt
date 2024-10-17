package com.cbnu.cat_ch.story.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cbnu.cat_ch.story.data.GenerationRepository

class PollinatorViewModelFactory(
    private val repository: GenerationRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PollinatorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PollinatorViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}