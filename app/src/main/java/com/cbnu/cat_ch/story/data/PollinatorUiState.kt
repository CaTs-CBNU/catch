package com.cbnu.cat_ch.story.data

import android.graphics.Bitmap
import com.cbnu.cat_ch.story.model.Model
import com.cbnu.cat_ch.story.model.Models
import com.cbnu.cat_ch.story.model.Sizes

data class PollinatorUiState(
    val prompt: String = "",
    val bitmap: Bitmap? = null,
    val seed: Int? = null,
    val model: Model = Models.getDefaultModel(),
    val width: Int = Sizes.getDefaultSize(),
    val height: Int = Sizes.getDefaultSize(),
    val noLogo: Boolean = false,
    val noFeed: Boolean = false,
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val showCheckmark: Boolean = false, // Reset checkmark visibility
    val imagePath: String? = null // 추가된 프로퍼티
)