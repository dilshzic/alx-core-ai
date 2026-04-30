package com.algorithmx.androidmodules.coreai.brain

import android.util.Log
import com.algorithmx.androidmodules.coreai.brain.implementations.GeminiBrainImpl
import com.algorithmx.androidmodules.coreai.brain.implementations.OpenAiCompatibleBrainImpl
import com.algorithmx.androidmodules.coreai.brain.models.BrainProvider
import com.algorithmx.androidmodules.coreai.brain.models.StoredBrainConfig
import com.algorithmx.androidmodules.coreai.brain.models.ReasoningLevel
import com.algorithmx.androidmodules.coreai.brain.registry.BrainRegistry
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

    private suspend fun buildBrain(config: StoredBrainConfig): AiBrain? {
        val provider = config.provider
        val apiKey = configProvider.getApiKey(provider)
        
        if (apiKey.isBlank() && provider != BrainProvider.LOCAL_GEMMA) {
            Log.e(TAG, "API key missing for provider: $provider")
            return null
        }

        return when (provider) {
            BrainProvider.GEMINI -> {
                GeminiBrainImpl(
                    apiKey = apiKey,
                    modelName = config.modelName,
                    systemInstruction = config.systemInstruction,
                    temperature = config.temperature.toFloat(),
                    topP = config.topP.toFloat(),
                    maxOutputTokens = config.maxTokens,
                    stopSequences = config.stopSequences,
                    searchGrounding = config.searchGrounding,
                    codeExecution = config.codeExecution,
                    safetyHarassment = config.safetyHarassment,
                    safetyHateSpeech = config.safetyHateSpeech,
                    safetySexuallyExplicit = config.safetySexuallyExplicit,
                    safetyDangerousContent = config.safetyDangerousContent,
                    reasoningLevel = config.reasoningLevel
                )
            }
            BrainProvider.GROQ -> {
                OpenAiCompatibleBrainImpl(
                    apiKey = apiKey,
                    modelName = config.modelName,
                    baseUrl = "https://api.groq.com/openai/v1/chat/completions",
                    systemInstruction = config.systemInstruction,
                    temperature = config.temperature,
                    topP = config.topP,
                    maxCompletionTokens = config.maxTokens,
                    stopSequences = config.stopSequences,
                    reasoningEffort = when(config.reasoningLevel) {
                        ReasoningLevel.LOW -> "low"
                        ReasoningLevel.MEDIUM -> "medium"
                        ReasoningLevel.HIGH -> "high"
                        else -> null
                    }
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

        val brain = buildBrain(config)

        if (brain == null) {
            return Result.failure(Exception("Failed to initialize AI model (${config.modelName})."))
        }

        val result = brain.generateText(prompt, isJsonMode = isJsonMode)
        
        val isSuccess = result.isSuccess
        val errorMessage = result.exceptionOrNull()?.message

        // Log usage (asynchronously)
        val responseText = if (isSuccess) result.getOrThrow() else "Error: $errorMessage"
        val estimatedTokens = ((prompt.length + responseText.length) / 4.0).toInt()
        scope.launch {
            try {
                usageLogger.logUsage(
                    provider = config.provider,
                    modelUsed = config.modelName,
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

        val brain = buildBrain(config) ?: return emptyFlow()
        
        return brain.generateTextStream(prompt)
    }
}
