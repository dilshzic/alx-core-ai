package com.algorithmx.androidmodules.coreai.brain

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.algorithmx.androidmodules.coreai.brain.models.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "brain_settings")

@Singleton
class BrainDataStoreManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val PROVIDER = stringPreferencesKey("provider")
        val MODEL = stringPreferencesKey("model")
        val CATEGORY = stringPreferencesKey("category")
        val SYSTEM_INSTRUCTION = stringPreferencesKey("system_instruction")
        val TOTAL_REQUESTS = intPreferencesKey("total_requests")
        val TOTAL_TOKENS = intPreferencesKey("total_tokens")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val MASTER_AI_FREEZE = booleanPreferencesKey("master_ai_freeze")
        val IS_SEED_APPLIED = booleanPreferencesKey("is_seed_applied")
        
        // Advanced Config Keys
        val TEMPERATURE = doublePreferencesKey("temperature")
        val TOP_P = doublePreferencesKey("top_p")
        val MAX_TOKENS = intPreferencesKey("max_tokens")
        val PRESENCE_PENALTY = doublePreferencesKey("presence_penalty")
        val FREQUENCY_PENALTY = doublePreferencesKey("frequency_penalty")
        val STOP_SEQUENCES = stringPreferencesKey("stop_sequences")
        val REASONING_LEVEL = stringPreferencesKey("reasoning_level")

        // Gemini Specific
        val SEARCH_GROUNDING = booleanPreferencesKey("search_grounding")
        val CODE_EXECUTION = booleanPreferencesKey("code_execution")
        val SAFETY_HARASSMENT = stringPreferencesKey("safety_harassment")
        val SAFETY_HATE_SPEECH = stringPreferencesKey("safety_hate_speech")
        val SAFETY_SEXUALLY_EXPLICIT = stringPreferencesKey("safety_sexually_explicit")
        val SAFETY_DANGEROUS_CONTENT = stringPreferencesKey("safety_dangerous_content")
    }

    val brainConfigFlow: Flow<StoredBrainConfig> = context.dataStore.data.map { preferences ->
        val providerName = preferences[PreferencesKeys.PROVIDER] ?: BrainProvider.GEMINI.name
        val categoryName = preferences[PreferencesKeys.CATEGORY] ?: BrainCategory.TEXT_TO_TEXT.name
        val stopSeqsStr = preferences[PreferencesKeys.STOP_SEQUENCES] ?: ""
        
        StoredBrainConfig(
            provider = try { BrainProvider.valueOf(providerName) } catch (e: Exception) { BrainProvider.GEMINI },
            modelName = preferences[PreferencesKeys.MODEL] ?: "gemini-1.5-flash",
            systemInstruction = preferences[PreferencesKeys.SYSTEM_INSTRUCTION] ?: "You are a helpful knowledge assistant.",
            totalRequests = preferences[PreferencesKeys.TOTAL_REQUESTS] ?: 0,
            totalTokens = preferences[PreferencesKeys.TOTAL_TOKENS] ?: 0,
            category = try { BrainCategory.valueOf(categoryName) } catch (e: Exception) { BrainCategory.TEXT_TO_TEXT },
            themeMode = preferences[PreferencesKeys.THEME_MODE] ?: "SYSTEM",
            notificationsEnabled = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true,
            isMasterAiFreeze = preferences[PreferencesKeys.MASTER_AI_FREEZE] ?: false,
            
            // Advanced Config
            temperature = preferences[PreferencesKeys.TEMPERATURE] ?: 1.0,
            topP = preferences[PreferencesKeys.TOP_P] ?: 0.95,
            maxTokens = preferences[PreferencesKeys.MAX_TOKENS] ?: 4096,
            presencePenalty = preferences[PreferencesKeys.PRESENCE_PENALTY] ?: 0.0,
            frequencyPenalty = preferences[PreferencesKeys.FREQUENCY_PENALTY] ?: 0.0,
            stopSequences = if (stopSeqsStr.isBlank()) emptyList() else stopSeqsStr.split(","),
            reasoningLevel = ReasoningLevel.valueOf(preferences[PreferencesKeys.REASONING_LEVEL] ?: ReasoningLevel.DISABLED.name),

            // Gemini Specific
            searchGrounding = preferences[PreferencesKeys.SEARCH_GROUNDING] ?: false,
            codeExecution = preferences[PreferencesKeys.CODE_EXECUTION] ?: false,
            safetyHarassment = SafetyThreshold.valueOf(preferences[PreferencesKeys.SAFETY_HARASSMENT] ?: SafetyThreshold.BLOCK_ONLY_HIGH.name),
            safetyHateSpeech = SafetyThreshold.valueOf(preferences[PreferencesKeys.SAFETY_HATE_SPEECH] ?: SafetyThreshold.BLOCK_ONLY_HIGH.name),
            safetySexuallyExplicit = SafetyThreshold.valueOf(preferences[PreferencesKeys.SAFETY_SEXUALLY_EXPLICIT] ?: SafetyThreshold.BLOCK_ONLY_HIGH.name),
            safetyDangerousContent = SafetyThreshold.valueOf(preferences[PreferencesKeys.SAFETY_DANGEROUS_CONTENT] ?: SafetyThreshold.BLOCK_ONLY_HIGH.name)
        )
    }

    suspend fun saveEngineConfiguration(
        provider: BrainProvider,
        modelName: String,
        category: BrainCategory,
        systemInstruction: String
    ) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PROVIDER] = provider.name
            preferences[PreferencesKeys.MODEL] = modelName
            preferences[PreferencesKeys.CATEGORY] = category.name
            preferences[PreferencesKeys.SYSTEM_INSTRUCTION] = systemInstruction
        }
    }

    suspend fun saveAdvancedConfiguration(
        temperature: Double,
        topP: Double,
        maxTokens: Int,
        presencePenalty: Double,
        frequencyPenalty: Double,
        stopSequences: List<String>,
        reasoningLevel: ReasoningLevel = ReasoningLevel.DISABLED,
        searchGrounding: Boolean = false,
        codeExecution: Boolean = false,
        safetyHarassment: SafetyThreshold = SafetyThreshold.BLOCK_ONLY_HIGH,
        safetyHateSpeech: SafetyThreshold = SafetyThreshold.BLOCK_ONLY_HIGH,
        safetySexuallyExplicit: SafetyThreshold = SafetyThreshold.BLOCK_ONLY_HIGH,
        safetyDangerousContent: SafetyThreshold = SafetyThreshold.BLOCK_ONLY_HIGH
    ) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TEMPERATURE] = temperature
            preferences[PreferencesKeys.TOP_P] = topP
            preferences[PreferencesKeys.MAX_TOKENS] = maxTokens
            preferences[PreferencesKeys.PRESENCE_PENALTY] = presencePenalty
            preferences[PreferencesKeys.FREQUENCY_PENALTY] = frequencyPenalty
            preferences[PreferencesKeys.STOP_SEQUENCES] = stopSequences.joinToString(",")
            preferences[PreferencesKeys.REASONING_LEVEL] = reasoningLevel.name
            preferences[PreferencesKeys.SEARCH_GROUNDING] = searchGrounding
            preferences[PreferencesKeys.CODE_EXECUTION] = codeExecution
            preferences[PreferencesKeys.SAFETY_HARASSMENT] = safetyHarassment.name
            preferences[PreferencesKeys.SAFETY_HATE_SPEECH] = safetyHateSpeech.name
            preferences[PreferencesKeys.SAFETY_SEXUALLY_EXPLICIT] = safetySexuallyExplicit.name
            preferences[PreferencesKeys.SAFETY_DANGEROUS_CONTENT] = safetyDangerousContent.name
        }
    }

    suspend fun resetToBaseline() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TEMPERATURE] = 1.0
            preferences[PreferencesKeys.TOP_P] = 0.95
            preferences[PreferencesKeys.MAX_TOKENS] = 4096
            preferences[PreferencesKeys.PRESENCE_PENALTY] = 0.0
            preferences[PreferencesKeys.FREQUENCY_PENALTY] = 0.0
            preferences[PreferencesKeys.STOP_SEQUENCES] = ""
            preferences[PreferencesKeys.REASONING_LEVEL] = ReasoningLevel.DISABLED.name
            preferences[PreferencesKeys.SEARCH_GROUNDING] = false
            preferences[PreferencesKeys.CODE_EXECUTION] = false
            preferences[PreferencesKeys.SAFETY_HARASSMENT] = SafetyThreshold.BLOCK_ONLY_HIGH.name
            preferences[PreferencesKeys.SAFETY_HATE_SPEECH] = SafetyThreshold.BLOCK_ONLY_HIGH.name
            preferences[PreferencesKeys.SAFETY_SEXUALLY_EXPLICIT] = SafetyThreshold.BLOCK_ONLY_HIGH.name
            preferences[PreferencesKeys.SAFETY_DANGEROUS_CONTENT] = SafetyThreshold.BLOCK_ONLY_HIGH.name
            preferences[PreferencesKeys.SYSTEM_INSTRUCTION] = "You are a helpful knowledge assistant."
        }
    }

    suspend fun incrementUsageStats(tokens: Int) {
        context.dataStore.edit { preferences ->
            val currentRequests = preferences[PreferencesKeys.TOTAL_REQUESTS] ?: 0
            val currentTokens = preferences[PreferencesKeys.TOTAL_TOKENS] ?: 0
            preferences[PreferencesKeys.TOTAL_REQUESTS] = currentRequests + 1
            preferences[PreferencesKeys.TOTAL_TOKENS] = currentTokens + tokens
        }
    }

    suspend fun saveThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode
        }
    }

    suspend fun saveNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setMasterAiFreeze(freeze: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MASTER_AI_FREEZE] = freeze
        }
    }
}
