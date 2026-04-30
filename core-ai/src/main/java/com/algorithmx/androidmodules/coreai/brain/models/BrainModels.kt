package com.algorithmx.androidmodules.coreai.brain.models

import kotlinx.serialization.Serializable

enum class BrainProvider {
    GEMINI,
    GROQ,
    LOCAL_GEMMA
}

enum class BrainCategory {
    REASONING,
    FUNCTION_CALLING,
    TEXT_TO_TEXT,
    VISION,
    MULTILINGUAL
}

enum class SafetyThreshold {
    BLOCK_NONE,
    BLOCK_ONLY_HIGH,
    BLOCK_MEDIUM_AND_ABOVE,
    BLOCK_LOW_AND_ABOVE
}

enum class ReasoningLevel {
    DISABLED,
    LOW,
    MEDIUM,
    HIGH
}

@Serializable
data class BrainConfig(
    val provider: BrainProvider,
    val modelName: String,
    val category: BrainCategory = BrainCategory.TEXT_TO_TEXT
)

data class StoredBrainConfig(
    val provider: BrainProvider,
    val modelName: String,
    val systemInstruction: String,
    val totalRequests: Int,
    val totalTokens: Int,
    val category: BrainCategory = BrainCategory.TEXT_TO_TEXT,
    val themeMode: String = "SYSTEM",
    val notificationsEnabled: Boolean = true,
    val isMasterAiFreeze: Boolean = false,
    
    // Advanced Configuration
    val temperature: Double = 1.0,
    val topP: Double = 0.95,
    val maxTokens: Int = 4096,
    val presencePenalty: Double = 0.0,
    val frequencyPenalty: Double = 0.0,
    val stopSequences: List<String> = emptyList(),

    // New: Reasoning Level
    val reasoningLevel: ReasoningLevel = ReasoningLevel.DISABLED,

    // Gemini Specific Tools & Safety
    val searchGrounding: Boolean = false,
    val codeExecution: Boolean = false,
    val safetyHarassment: SafetyThreshold = SafetyThreshold.BLOCK_ONLY_HIGH,
    val safetyHateSpeech: SafetyThreshold = SafetyThreshold.BLOCK_ONLY_HIGH,
    val safetySexuallyExplicit: SafetyThreshold = SafetyThreshold.BLOCK_ONLY_HIGH,
    val safetyDangerousContent: SafetyThreshold = SafetyThreshold.BLOCK_ONLY_HIGH
)

data class ProviderCapabilities(
    val supportsSystemInstruction: Boolean,
    val supportsTemperature: Boolean,
    val supportsTopP: Boolean,
    val supportsMaxTokens: Boolean,
    val supportsPresencePenalty: Boolean,
    val supportsFrequencyPenalty: Boolean,
    val supportsStopSequences: Boolean,
    val supportsJsonMode: Boolean,
    val supportsSearchGrounding: Boolean,
    val supportsCodeExecution: Boolean,
    val supportsSafetySettings: Boolean,
    val supportsReasoning: Boolean
)

object BrainCapabilities {
    fun getForProvider(provider: BrainProvider): ProviderCapabilities {
        return when (provider) {
            BrainProvider.GEMINI -> ProviderCapabilities(
                supportsSystemInstruction = true,
                supportsTemperature = true,
                supportsTopP = true,
                supportsMaxTokens = true,
                supportsPresencePenalty = false,
                supportsFrequencyPenalty = false,
                supportsStopSequences = true,
                supportsJsonMode = true,
                supportsSearchGrounding = true,
                supportsCodeExecution = true,
                supportsSafetySettings = true,
                supportsReasoning = true // Supported via specific models or thinkingConfig
            )
            BrainProvider.GROQ -> ProviderCapabilities(
                supportsSystemInstruction = true,
                supportsTemperature = true,
                supportsTopP = true,
                supportsMaxTokens = true,
                supportsPresencePenalty = true,
                supportsFrequencyPenalty = true,
                supportsStopSequences = true,
                supportsJsonMode = true,
                supportsSearchGrounding = false,
                supportsCodeExecution = false,
                supportsSafetySettings = false,
                supportsReasoning = true // DeepSeek-R1 support
            )
            BrainProvider.LOCAL_GEMMA -> ProviderCapabilities(
                supportsSystemInstruction = false,
                supportsTemperature = false,
                supportsTopP = false,
                supportsMaxTokens = false,
                supportsPresencePenalty = false,
                supportsFrequencyPenalty = false,
                supportsStopSequences = false,
                supportsJsonMode = false,
                supportsSearchGrounding = false,
                supportsCodeExecution = false,
                supportsSafetySettings = false,
                supportsReasoning = false
            )
        }
    }
}
