package com.cbnu.cat_ch.gallery.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.cbnu.cat_ch.story.room.dao.StoryDao
import com.cbnu.cat_ch.story.room.db.StoryDatabase
import com.cbnu.cat_ch.story.room.entity.StoryEntity
import kotlinx.coroutines.launch

class GalleryViewModel(application: Application) : AndroidViewModel(application) {
    private val storyDao: StoryDao = StoryDatabase.getDatabase(application).storyDao()

    private val _allStories = MutableLiveData<LiveData<List<StoryEntity>>>()
    val allStories: LiveData<List<StoryEntity>> = storyDao.getAllStories()

    init {
        fetchStories()
    }

    private fun fetchStories() {
        viewModelScope.launch {
            _allStories.value = storyDao.getAllStories()
        }
    }

    fun refreshStories() {
        fetchStories()
    }

    // New function to delete multiple stories
    fun deleteStories(storyIds: List<Long>) {
        viewModelScope.launch {
            storyDao.deleteStoriesByIds(storyIds)
            refreshStories() // Refresh the list after deletion
        }
    }
}