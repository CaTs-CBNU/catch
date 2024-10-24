package com.cbnu.cat_ch.story.viewmodel

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cbnu.cat_ch.R
import com.cbnu.cat_ch.story.data.GenerationRepository
import com.cbnu.cat_ch.story.data.PollinatorUiState
import com.cbnu.cat_ch.story.model.GenerationParameters
import com.cbnu.cat_ch.story.model.GenerationResult
import com.cbnu.cat_ch.story.model.Model
import com.cbnu.cat_ch.story.model.Models
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

class PollinatorViewModel(private val repository: GenerationRepository) : ViewModel(){
    // Pollinator state management
    private val _uiState = MutableStateFlow(PollinatorUiState())
    val uiState: StateFlow<PollinatorUiState> = _uiState

    private val _width = MutableStateFlow(512)
    val width: StateFlow<Int> = _width

    private val _height = MutableStateFlow(512)
    val height: StateFlow<Int> = _height

    private val _model = MutableStateFlow(Models.getDefaultModel())
    val model: StateFlow<Model> = _model

    // 이미지 생성 중 상태를 추적하는 LiveData
    private val _isGeneratingImage = MutableLiveData<Boolean>().apply { value = false }
    val isGeneratingImage: LiveData<Boolean> = _isGeneratingImage

    var listener: ImageListener? = null

    

    var _navigateToNextScreen: ((Boolean) -> Unit)? = null
    val navigateToNextScreen: (Boolean) -> Unit
        get() = { _navigateToNextScreen?.invoke(it) }

    // Pollinator methods
    fun onPromptChanged(newPrompt: String) {
        _uiState.update { currentState ->
            currentState.copy(
                prompt = newPrompt
            )
        }
    }

    private fun saveImage(context: Context) {
        if (_uiState.value.bitmap != null) {
            val uri = saveBitmap(
                context,
                _uiState.value.bitmap!!,
                Bitmap.CompressFormat.PNG,
                "image/png",
                "pollinator_${System.currentTimeMillis()}.png"
            )

            // 이미지 경로 업데이트
            _uiState.update { currentState ->
                currentState.copy(imagePath = uri.toString())
            }

            val text = context.getString(R.string.image_saved)
            val duration = Toast.LENGTH_SHORT
            Toast.makeText(context, text, duration).show()
        }
    }

    fun generateImage(context: Context) {
        _isGeneratingImage.value = true  // 이미지 생성 시작

        _uiState.update { currentState ->
            currentState.copy(
                bitmap = null,
                isLoading = true,
                isError = false,
                showCheckmark = false // Reset checkmark visibility
            )
        }
        viewModelScope.launch {
            when (val result = repository.generate(getParameters())) {
                is GenerationResult.Success -> {
                    handleSuccess(result.bitmap, context)
                }
                is GenerationResult.Error -> {
                    handleError("Error: ${result.exception.message}")
                }
                is GenerationResult.RequestError -> {
                    handleError("Request error occurred Code:" + " ${result.code} : ${result.message}")
                }
                is GenerationResult.InvalidResponseError -> {
                    handleError("Invalid response from server")
                }
                is GenerationResult.ServerError -> {
                    handleError("Server Error")
                }
                is GenerationResult.NetworkError -> {
                    handleError("Network Error")
                }
            }
            _isGeneratingImage.value = false  // 이미지 생성 완료
        }
    }

    private fun getParameters(): GenerationParameters {
        return GenerationParameters(
            prompt = _uiState.value.prompt,
            width = _uiState.value.width,
            height = _uiState.value.height,
            model = _uiState.value.model.value,
            noLogo = _uiState.value.noLogo,
            private = _uiState.value.noFeed,
            seed = _uiState.value.seed,
        )
    }

    private fun handleSuccess(bitmap: Bitmap, context: Context) {
        _uiState.update { currentState ->
            currentState.copy(
                bitmap = bitmap,
                isLoading = false,
                isError = false,
                errorMessage = null,
                showCheckmark = true // Reset checkmark visibility
            )
        }
        viewModelScope.launch {
            try {
                saveImage(context)
                // 이미지 경로를 listener를 통해 전달
                listener?.onImageGenerated(_uiState.value.imagePath ?: "")
                navigateToNextScreen.invoke(true) // Navigate to next screen on success
            } catch (e: IOException) {
                handleError("Failed to save image: ${e.message}")
            }
        }
    }

    private fun handleError(errorMessage: String) {
        _uiState.update { currentState ->
            currentState.copy(
                bitmap = null,
                isLoading = false,
                isError = true,
                errorMessage = errorMessage
            )
        }
    }

    @Throws(IOException::class)
    fun saveBitmap(
        context: Context,
        bitmap: Bitmap,
        format: Bitmap.CompressFormat,
        mimeType: String,
        displayName: String
    ): Uri {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM)
        }

        val resolver = context.contentResolver
        var uri: Uri? = null

        try {
            uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: throw IOException("Failed to create new MediaStore record.")

            resolver.openOutputStream(uri)?.use {
                if (!bitmap.compress(format, 95, it))
                    throw IOException("Failed to save bitmap.")
            } ?: throw IOException("Failed to open output stream.")

            return uri

        } catch (e: IOException) {
            uri?.let { orphanUri ->
                resolver.delete(orphanUri, null, null)
            }
            throw e
        }
    }

    fun onModelChanged(model: Model) {
        _uiState.update { currentState ->
            currentState.copy(
                model = model
            )
        }
    }

    fun onWidthChanged(width: Int) {
        _uiState.update { currentState ->
            currentState.copy(
                width = width,
            )
        }
    }

    fun onHeightChanged(height: Int) {
        _uiState.update { currentState ->
            currentState.copy(
                height = height,
            )
        }
    }

    fun setGeneratingImage(isGenerating: Boolean) {
        _isGeneratingImage.value = isGenerating
    }
}