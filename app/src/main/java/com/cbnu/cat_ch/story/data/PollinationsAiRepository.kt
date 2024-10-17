package com.cbnu.cat_ch.story.data

import android.graphics.BitmapFactory
import com.cbnu.cat_ch.story.model.GenerationParameters
import com.cbnu.cat_ch.story.model.GenerationResult
import com.cbnu.cat_ch.story.model.Models
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

private const val BASE_URL = "https://image.pollinations.ai/prompt/"

class PollinationsAiRepository : GenerationRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS) // Set connection timeout
        .readTimeout(30, TimeUnit.SECONDS)    // Set read timeout
        .writeTimeout(30, TimeUnit.SECONDS)   // Set write timeout
        .build()

    private fun buildImageUrl(generationParameters: GenerationParameters): String {
        val urlBuilder = StringBuilder(BASE_URL)
        urlBuilder.append(urlEncode(generationParameters.prompt))
        urlBuilder.append("?width=").append(generationParameters.width)
        urlBuilder.append("&height=").append(generationParameters.height)

        if (generationParameters.seed != null) {
            urlBuilder.append("&seed=").append(generationParameters.seed)
        }
        if (generationParameters.model != Models.getDefaultModel().value) {
            urlBuilder.append("&model=").append(urlEncode(generationParameters.model))
        }
        if (generationParameters.noLogo != GenerationParameters.DEFAULT_NO_LOGO) {
            urlBuilder.append("&nologo=true")
        }
        if (generationParameters.private != GenerationParameters.DEFAULT_PRIVATE) {
            urlBuilder.append("&nofeed=true")
        }
        return urlBuilder.toString()
    }

    private fun urlEncode(target: String): String {
        return URLEncoder.encode(
            target,
            StandardCharsets.UTF_8.toString()
        )
    }

    override suspend fun generate(generationParameters: GenerationParameters): GenerationResult {
        return withContext(Dispatchers.IO) {
            val maxRetries = 3
            var currentAttempt = 0

            while (currentAttempt < maxRetries) {
                try {
                    val url = buildImageUrl(generationParameters)
                    val request = Request.Builder()
                        .url(url)
                        .build()
                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            val inputStream = response.body?.byteStream()
                            inputStream?.let {
                                return@withContext GenerationResult.Success(
                                    BitmapFactory.decodeStream(it)
                                )
                            }
                            return@withContext GenerationResult.InvalidResponseError
                        } else {
                            if (response.code >= 500) {
                                return@withContext GenerationResult.ServerError
                            }
                            return@withContext GenerationResult.Error(IOException("Failed response with code: ${response.code}"))
                        }
                    }
                } catch (e: SocketTimeoutException) {
                    currentAttempt++
                    if (currentAttempt >= maxRetries) {
                        e.printStackTrace()
                        return@withContext GenerationResult.NetworkError
                    }
                    // Optionally, add a delay before retrying
                    delay(1000) // Delay for 1 second before retrying
                } catch (e: IOException) {
                    e.printStackTrace()
                    return@withContext GenerationResult.NetworkError
                }
            }
            return@withContext GenerationResult.NetworkError // Return this if all retries fail
        }
    }
}