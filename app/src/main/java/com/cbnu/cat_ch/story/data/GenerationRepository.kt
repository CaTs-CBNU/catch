package com.cbnu.cat_ch.story.data

import com.cbnu.cat_ch.story.model.GenerationParameters
import com.cbnu.cat_ch.story.model.GenerationResult

interface GenerationRepository {
    suspend fun generate(generationParameters: GenerationParameters): GenerationResult
}