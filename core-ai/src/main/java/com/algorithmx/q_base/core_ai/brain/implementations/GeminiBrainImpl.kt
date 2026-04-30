package com.algorithmx.q_base.core_ai.brain.implementations

import com.algorithmx.q_base.core_ai.brain.AiBrain
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GeminiBrainImpl(
    private val apiKey: String,
    private val modelName: String,
    private val systemInstruction: String? = null,
    private val temperature: Float? = null,
    private val topK: Int? = null,
    private val topP: Float? = null,
    private val maxOutputTokens: Int? = null,
    private val stopSequences: List<String>? = null,
    private val customSafetySettings: List<SafetySetting>? = null
) : AiBrain {

    private fun buildModel(isJsonMode: Boolean): GenerativeModel {
        val config = generationConfig {
            this@GeminiBrainImpl.temperature?.let { temperature = it }
            this@GeminiBrainImpl.topK?.let { topK = it }
            this@GeminiBrainImpl.topP?.let { topP = it }
            this@GeminiBrainImpl.maxOutputTokens?.let { maxOutputTokens = it }
            this@GeminiBrainImpl.stopSequences?.let { stopSequences = it }
            
            if (isJsonMode) {
                responseMimeType = "application/json"
            }
        }

        return GenerativeModel(
            modelName = modelName,
            apiKey = apiKey,
            generationConfig = config,
            safetySettings = customSafetySettings,
            systemInstruction = systemInstruction?.let { content { text(it) } }
        )
    }

    override suspend fun generateText(prompt: String, isJsonMode: Boolean): Result<String> {
        return try {
            val response = buildModel(isJsonMode).generateContent(prompt)
            Result.success(response.text ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun generateTextStream(prompt: String, isJsonMode: Boolean): Flow<String> {
        return buildModel(isJsonMode).generateContentStream(prompt).map { it.text ?: "" }
    }
}
