package com.algorithmx.alx_core_ai.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algorithmx.androidmodules.coreai.brain.BrainDataStoreManager
import com.algorithmx.androidmodules.coreai.brain.models.*
import com.algorithmx.androidmodules.coreai.brain.registry.BrainRegistry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdvancedConfigUiState(
    val temperature: Float = 1.0f,
    val topP: Float = 0.95f,
    val maxTokens: Int = 4096,
    val presencePenalty: Float = 0.0f,
    val frequencyPenalty: Float = 0.0f,
    val stopSequences: String = "",
    val reasoningLevel: ReasoningLevel = ReasoningLevel.DISABLED,
    val searchGrounding: Boolean = false,
    val codeExecution: Boolean = false,
    val safetyHarassment: SafetyThreshold = SafetyThreshold.BLOCK_ONLY_HIGH,
    val safetyHateSpeech: SafetyThreshold = SafetyThreshold.BLOCK_ONLY_HIGH,
    val safetySexuallyExplicit: SafetyThreshold = SafetyThreshold.BLOCK_ONLY_HIGH,
    val safetyDangerousContent: SafetyThreshold = SafetyThreshold.BLOCK_ONLY_HIGH
)

@HiltViewModel
class BrainConfigViewModel @Inject constructor(
    private val dataStoreManager: BrainDataStoreManager
) : ViewModel() {

    val config: StateFlow<StoredBrainConfig?> = dataStoreManager.brainConfigFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _uiState = MutableStateFlow(AdvancedConfigUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            dataStoreManager.brainConfigFlow.collect { cfg ->
                _uiState.update {
                    it.copy(
                        temperature = cfg.temperature.toFloat(),
                        topP = cfg.topP.toFloat(),
                        maxTokens = cfg.maxTokens,
                        presencePenalty = cfg.presencePenalty.toFloat(),
                        frequencyPenalty = cfg.frequencyPenalty.toFloat(),
                        stopSequences = cfg.stopSequences.joinToString(", "),
                        reasoningLevel = cfg.reasoningLevel,
                        searchGrounding = cfg.searchGrounding,
                        codeExecution = cfg.codeExecution,
                        safetyHarassment = cfg.safetyHarassment,
                        safetyHateSpeech = cfg.safetyHateSpeech,
                        safetySexuallyExplicit = cfg.safetySexuallyExplicit,
                        safetyDangerousContent = cfg.safetyDangerousContent
                    )
                }
            }
        }
    }

    fun updateTemperature(value: Float) = _uiState.update { it.copy(temperature = value) }
    fun updateTopP(value: Float) = _uiState.update { it.copy(topP = value) }
    fun updateMaxTokens(value: Int) = _uiState.update { it.copy(maxTokens = value) }
    fun updatePresencePenalty(value: Float) = _uiState.update { it.copy(presencePenalty = value) }
    fun updateFrequencyPenalty(value: Float) = _uiState.update { it.copy(frequencyPenalty = value) }
    fun updateStopSequences(value: String) = _uiState.update { it.copy(stopSequences = value) }
    fun updateReasoningLevel(value: ReasoningLevel) = _uiState.update { it.copy(reasoningLevel = value) }
    fun updateSearchGrounding(value: Boolean) = _uiState.update { it.copy(searchGrounding = value) }
    fun updateCodeExecution(value: Boolean) = _uiState.update { it.copy(codeExecution = value) }
    fun updateSafetyHarassment(value: SafetyThreshold) = _uiState.update { it.copy(safetyHarassment = value) }
    fun updateSafetyHateSpeech(value: SafetyThreshold) = _uiState.update { it.copy(safetyHateSpeech = value) }
    fun updateSafetySexuallyExplicit(value: SafetyThreshold) = _uiState.update { it.copy(safetySexuallyExplicit = value) }
    fun updateSafetyDangerousContent(value: SafetyThreshold) = _uiState.update { it.copy(safetyDangerousContent = value) }

    fun resetToBaseline() {
        viewModelScope.launch {
            dataStoreManager.resetToBaseline()
        }
    }

    fun saveConfiguration(modelName: String, systemInstruction: String) {
        val provider = BrainRegistry.getProviderForModel(modelName)
        val currentUi = _uiState.value
        
        viewModelScope.launch {
            dataStoreManager.saveEngineConfiguration(
                provider = provider,
                modelName = modelName,
                category = config.value?.category ?: BrainCategory.TEXT_TO_TEXT,
                systemInstruction = systemInstruction
            )
            dataStoreManager.saveAdvancedConfiguration(
                temperature = currentUi.temperature.toDouble(),
                topP = currentUi.topP.toDouble(),
                maxTokens = currentUi.maxTokens,
                presencePenalty = currentUi.presencePenalty.toDouble(),
                frequencyPenalty = currentUi.frequencyPenalty.toDouble(),
                stopSequences = currentUi.stopSequences.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                reasoningLevel = currentUi.reasoningLevel,
                searchGrounding = currentUi.searchGrounding,
                codeExecution = currentUi.codeExecution,
                safetyHarassment = currentUi.safetyHarassment,
                safetyHateSpeech = currentUi.safetyHateSpeech,
                safetySexuallyExplicit = currentUi.safetySexuallyExplicit,
                safetyDangerousContent = currentUi.safetyDangerousContent
            )
        }
    }
    
    fun setMasterFreeze(freeze: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setMasterAiFreeze(freeze)
        }
    }
}
