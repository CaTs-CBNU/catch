package com.cbnu.cat_ch.story.model

data class ImageGenerationRequest(
    val model: String,
    val prompt: String,
    val size: String,
    val quality: String,
    val n: Int
)