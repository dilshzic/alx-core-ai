package com.algorithmx.q_base.core_ai.brain.models

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
    val isMasterAiFreeze: Boolean = false
)
