package com.algorithmx.q_base.core_ai.brain

import android.util.Log
import com.algorithmx.q_base.core_ai.brain.implementations.GeminiBrainImpl
import com.algorithmx.q_base.core_ai.brain.implementations.OpenAiCompatibleBrainImpl
import com.algorithmx.q_base.core_ai.brain.models.BrainProvider
import com.algorithmx.q_base.core_ai.brain.models.StoredBrainConfig
import com.algorithmx.q_base.core_ai.brain.registry.BrainRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiBrainManager @Inject constructor(
    private val dataStoreManager: BrainDataStoreManager,
    private val usageLogger: AiUsageLogger,
    private val configProvider: BrainConfigProvider
) {
    private val TAG = "AiBrainManager"
    private val scope = CoroutineScope(Dispatchers.IO)
    
    private val _currentConfig = MutableStateFlow<StoredBrainConfig?>(null)

    init {
        dataStoreManager.brainConfigFlow.onEach { config ->
            _currentConfig.value = config
        }.launchIn(scope)
    }

    private suspend fun buildBrain(modelName: String): AiBrain? {
        val provider = BrainRegistry.getProviderForModel(modelName)
        val apiKey = configProvider.getApiKey(provider)
        
        if (apiKey.isBlank() && provider != BrainProvider.LOCAL_GEMMA) {
            Log.e(TAG, "API key missing for provider: $provider")
            return null
        }

        return when (provider) {
            BrainProvider.GEMINI -> {
                GeminiBrainImpl(apiKey, modelName)
            }
            BrainProvider.GROQ -> {
                OpenAiCompatibleBrainImpl(
                    apiKey = apiKey,
                    modelName = modelName,
                    baseUrl = "https://api.groq.com/openai/v1/chat/completions"
                )
            }
            BrainProvider.LOCAL_GEMMA -> {
                null
            }
        }
    }

    suspend fun askBrain(prompt: String, isJsonMode: Boolean = false): Result<String> {
        val config = _currentConfig.value ?: return Result.failure(Exception("AI engine not initialized. Please check your settings."))
        
        if (config.isMasterAiFreeze) {
            return Result.failure(Exception("AI generation is currently disabled by Master Freeze."))
        }

        val currentModel = config.modelName
        val systemPrompt = config.systemInstruction
        val brain = buildBrain(currentModel)

        if (brain == null) {
            return Result.failure(Exception("Failed to initialize AI model ($currentModel)."))
        }

        val fullPrompt = if (systemPrompt.isNotBlank()) "$systemPrompt\n\nUser Request: $prompt" else prompt
        
        val result = brain.generateText(fullPrompt, isJsonMode = isJsonMode)
        
        val isSuccess = result.isSuccess
        val errorMessage = result.exceptionOrNull()?.message

        // Log usage (asynchronously)
        val responseText = if (isSuccess) result.getOrThrow() else "Error: $errorMessage"
        val estimatedTokens = ((fullPrompt.length + responseText.length) / 4.0).toInt()
        scope.launch {
            try {
                usageLogger.logUsage(
                    provider = BrainRegistry.getProviderForModel(currentModel),
                    modelUsed = currentModel,
                    tokensEstimated = estimatedTokens,
                    isSuccess = isSuccess,
                    errorMessage = if (!isSuccess) errorMessage else null
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to log usage: ${e.message}")
            }
        }

        return result
    }

    suspend fun streamFromBrain(prompt: String): Flow<String> {
        val config = _currentConfig.value ?: return emptyFlow()
        if (config.isMasterAiFreeze) return emptyFlow()

        val primaryModel = config.modelName
        val systemPrompt = config.systemInstruction
        
        val brain = buildBrain(primaryModel) ?: return emptyFlow()
        val fullPrompt = "$systemPrompt\n\nUser Request: $prompt"
        
        return brain.generateTextStream(fullPrompt)
    }
}
