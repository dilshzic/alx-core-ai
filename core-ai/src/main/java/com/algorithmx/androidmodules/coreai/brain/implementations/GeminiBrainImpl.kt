package com.algorithmx.androidmodules.coreai.brain.implementations

import com.algorithmx.androidmodules.coreai.brain.AiBrain
import com.algorithmx.androidmodules.coreai.brain.models.SafetyThreshold
import com.algorithmx.androidmodules.coreai.brain.models.ReasoningLevel
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.Tool
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GeminiBrainImpl(
    private val apiKey: String,
    private val modelName: String,
    private val systemInstruction: String? = null,
    private val temperature: Float? = null,
    private val topP: Float? = null,
    private val maxOutputTokens: Int? = null,
    private val stopSequences: List<String>? = null,
    private val searchGrounding: Boolean = false,
    private val codeExecution: Boolean = false,
    private val safetyHarassment: SafetyThreshold = SafetyThreshold.BLOCK_ONLY_HIGH,
    private val safetyHateSpeech: SafetyThreshold = SafetyThreshold.BLOCK_ONLY_HIGH,
    private val safetySexuallyExplicit: SafetyThreshold = SafetyThreshold.BLOCK_ONLY_HIGH,
    private val safetyDangerousContent: SafetyThreshold = SafetyThreshold.BLOCK_ONLY_HIGH,
    private val reasoningLevel: ReasoningLevel = ReasoningLevel.DISABLED
) : AiBrain {

    private fun buildModel(isJsonMode: Boolean): GenerativeModel {
        val config = generationConfig {
            this@GeminiBrainImpl.temperature?.let { temperature = it }
            this@GeminiBrainImpl.topP?.let { topP = it }
            this@GeminiBrainImpl.maxOutputTokens?.let { maxOutputTokens = it }
            this@GeminiBrainImpl.stopSequences?.let { stopSequences = it }
            
            if (isJsonMode) {
                responseMimeType = "application/json"
            }

            // Note: Reasoning/Thinking support in SDK 0.9.0 is handled via model selection 
            // and internal heuristics for thinking models.
        }

        val tools = mutableListOf<Tool>()
        if (codeExecution) {
            tools.add(Tool.CODE_EXECUTION)
        }

        val safetySettings = listOf(
            SafetySetting(HarmCategory.HARASSMENT, mapThreshold(safetyHarassment)),
            SafetySetting(HarmCategory.HATE_SPEECH, mapThreshold(safetyHateSpeech)),
            SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, mapThreshold(safetySexuallyExplicit)),
            SafetySetting(HarmCategory.DANGEROUS_CONTENT, mapThreshold(safetyDangerousContent))
        )

        return GenerativeModel(
            modelName = modelName,
            apiKey = apiKey,
            generationConfig = config,
            safetySettings = safetySettings,
            tools = tools.ifEmpty { null },
            systemInstruction = systemInstruction?.let { content { text(it) } }
        )
    }

    private fun mapThreshold(threshold: SafetyThreshold): BlockThreshold {
        return when (threshold) {
            SafetyThreshold.BLOCK_NONE -> BlockThreshold.NONE
            SafetyThreshold.BLOCK_ONLY_HIGH -> BlockThreshold.ONLY_HIGH
            SafetyThreshold.BLOCK_MEDIUM_AND_ABOVE -> BlockThreshold.MEDIUM_AND_ABOVE
            SafetyThreshold.BLOCK_LOW_AND_ABOVE -> BlockThreshold.LOW_AND_ABOVE
        }
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
