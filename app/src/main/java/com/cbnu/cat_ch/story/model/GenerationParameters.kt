package com.cbnu.cat_ch.story.model

data class GenerationParameters(
    val prompt: String,
    val width: Int = DEFAULT_WIDTH,
    val height: Int = DEFAULT_HEIGHT,
    val seed: Int? = null,
    val model: String,
    val noLogo: Boolean = DEFAULT_NO_LOGO,
    val private: Boolean = DEFAULT_PRIVATE,

) {
    companion object {
        const val DEFAULT_NO_LOGO = true
        const val DEFAULT_PRIVATE = false
        const val DEFAULT_WIDTH = 512
        const val DEFAULT_HEIGHT = 512
    }
}