package com.cbnu.cat_ch.story.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cbnu.cat_ch.story.data.CharacterRole

class StoryViewModel: ViewModel(), ImageListener {
    private val _previousStep = MutableLiveData<Int>()
    val previousStep: LiveData<Int> get() = _previousStep

    private val _currentStep = MutableLiveData<Int>()
    val currentStep: LiveData<Int> get() = _currentStep

    private val _storyTopic = MutableLiveData<String>()
    val storyTopic: LiveData<String> get() = _storyTopic

    private val _storyCharacter = MutableLiveData<String>()
    val storyCharacter: LiveData<String> get() = _storyCharacter

    private val _storyPlot = MutableLiveData<String>()
    val storyPlot: LiveData<String> get() = _storyPlot

    // New properties for the StoryBackgroundFragment
    private val _atmosphere = MutableLiveData<String>()
    val atmosphere: LiveData<String> get() = _atmosphere

    private val _location = MutableLiveData<String>()
    val location: LiveData<String> get() = _location

    // LiveData to hold the list of character roles
    private val _characterRoles = MutableLiveData<List<CharacterRole>>()
    val characterRoles: LiveData<List<CharacterRole>> get() = _characterRoles

    private val _generationStoryPlot = MutableLiveData<String>()
    val generationStoryPlot: LiveData<String> get() = _generationStoryPlot

    private val _imagePath = MutableLiveData<String>()
    val imagePath: LiveData<String> get() = _imagePath

    // New property for emotion
    private val _emotion = MutableLiveData<String>()
    val emotion: LiveData<String> get() = _emotion

    init {
        _previousStep.value = 1
        _currentStep.value = 1
        _characterRoles.value = mutableListOf()
        _storyPlot.value = ""
        _storyTopic.value = ""
    }


    fun updateCurrentStep(step: Int) {
        _currentStep.value = step
    }

    fun updatePreviousStep(step: Int) {
        _previousStep.value = step
    }

    fun setStoryTopic(topic: String) {
        _storyTopic.value = topic
    }

    fun setStoryPlot(plot: String) {
        _storyPlot.value = plot
    }

    // Update methods
    fun updateAtmosphere(atmosphere: String) {
        _atmosphere.value = atmosphere
    }


    fun updateLocation(location: String) {
        _location.value = location
    }

    fun setCharacterRoles(roles: List<CharacterRole>) {
        _characterRoles.value = roles
    }

    fun addCharacterRoles(roles: List<CharacterRole>) {
        _characterRoles.value = roles
    }
    fun updateCharacterRole(position: Int, newName: String, newRole: String) {
        val updatedList = _characterRoles.value?.toMutableList() ?: mutableListOf()
        if (position in updatedList.indices) {
            updatedList[position] = CharacterRole(newName, newRole)
            _characterRoles.value = updatedList
        }
    }
    fun setGenerationStoryPlot(plot: String) {
        _generationStoryPlot.value = plot
    }

    fun updateGenerationStoryPlot(text: String) {
        _generationStoryPlot.value = text
    }

    // New update method for emotion
    fun updateEmotion(emotion: String) {
        _emotion.value = emotion
    }

    override fun onImageGenerated(imagePath: String) {
        _imagePath.value = imagePath
    }
}