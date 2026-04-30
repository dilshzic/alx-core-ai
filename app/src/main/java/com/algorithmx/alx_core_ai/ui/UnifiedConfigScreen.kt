package com.algorithmx.alx_core_ai.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.algorithmx.alx_core_ai.viewmodels.BrainConfigViewModel
import com.algorithmx.androidmodules.coreai.brain.models.BrainCapabilities
import com.algorithmx.androidmodules.coreai.brain.models.BrainCategory
import com.algorithmx.androidmodules.coreai.brain.models.BrainProvider
import com.algorithmx.androidmodules.coreai.brain.registry.BrainRegistry
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedConfigScreen(
    viewModel: BrainConfigViewModel,
    onBack: () -> Unit
) {
    val config by viewModel.config.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    val allModels = remember { BrainRegistry.allModelCodes }
    
    // Draft Model state
    var draftModel by remember { mutableStateOf("") }
    var draftSystemInstruction by remember { mutableStateOf("") }

    LaunchedEffect(config) {
        config?.let {
            draftModel = it.modelName
            draftSystemInstruction = it.systemInstruction
        }
    }
    
    val currentProvider = BrainRegistry.getProviderForModel(draftModel)
    val capabilities = BrainCapabilities.getForProvider(currentProvider)
    
    val backgroundBrush = remember {
        Brush.verticalGradient(
            listOf(Color(0xFF0F172A), Color(0xFF1E293B))
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(backgroundBrush)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Model Config", fontWeight = FontWeight.ExtraBold, color = Color.White) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
                    navigationIcon = {
                        TextButton(onClick = onBack) {
                            Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.resetToBaseline() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reset to Baseline", tint = Color.White)
                        }
                        Button(
                            onClick = {
                                viewModel.saveConfiguration(draftModel, draftSystemInstruction)
                                onBack()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Apply", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // Provider Info
                item {
                    ProviderBadge(provider = currentProvider)
                }
                
                // Card 1: Core Identity
                item {
                    ConfigCard(title = "Identity & Model", icon = Icons.Default.Info) {
                        DropdownField(
                            label = "Active Model",
                            options = allModels,
                            selected = draftModel,
                            optionLabel = { it },
                            onSelected = { draftModel = it }
                        )
                        
                        if (capabilities.supportsSystemInstruction) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = draftSystemInstruction,
                                onValueChange = { draftSystemInstruction = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("System Instruction") },
                                placeholder = { Text("e.g. You are a helpful assistant...") },
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                    unfocusedTextColor = Color.White,
                                    focusedTextColor = Color.White
                                )
                            )
                        }
                    }
                }

                // Card: Reasoning (Think Effort)
                if (capabilities.supportsReasoning) {
                    item {
                        ConfigCard(title = "Reasoning & Thought", icon = Icons.Default.Psychology) {
                            ReasoningLevelSelector(
                                current = uiState.reasoningLevel,
                                onSelected = { viewModel.updateReasoningLevel(it) }
                            )
                        }
                    }
                }
                
                // Card 2: Tools (Gemini Specific)
                if (capabilities.supportsSearchGrounding || capabilities.supportsCodeExecution) {
                    item {
                        ConfigCard(title = "AI Capabilities & Tools", icon = Icons.Default.Build) {
                            if (capabilities.supportsSearchGrounding) {
                                ToolToggle(
                                    label = "Google Search Grounding",
                                    description = "Enable real-time web knowledge to verify facts.",
                                    checked = uiState.searchGrounding,
                                    onCheckedChange = { viewModel.updateSearchGrounding(it) }
                                )
                            }
                            if (capabilities.supportsCodeExecution) {
                                if (capabilities.supportsSearchGrounding) HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.05f))
                                ToolToggle(
                                    label = "Code Execution",
                                    description = "Enable Python code execution for complex math and logic.",
                                    checked = uiState.codeExecution,
                                    onCheckedChange = { viewModel.updateCodeExecution(it) }
                                )
                            }
                        }
                    }
                }
                
                // Card 3: Creativity & Precision
                if (capabilities.supportsTemperature || capabilities.supportsTopP) {
                    item {
                        ConfigCard(title = "Creativity & Sampling") {
                            if (capabilities.supportsTemperature) {
                                ParameterSlider(
                                    label = "Temperature",
                                    value = uiState.temperature,
                                    range = 0f..2f,
                                    onValueChange = { viewModel.updateTemperature(it) },
                                    description = "Control randomness: Lower is more focused, higher is more creative."
                                )
                            }
                            
                            if (capabilities.supportsTopP) {
                                if (capabilities.supportsTemperature) HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.05f))
                                ParameterSlider(
                                    label = "Top P",
                                    value = uiState.topP,
                                    range = 0f..1f,
                                    onValueChange = { viewModel.updateTopP(it) },
                                    description = "Nucleus sampling: Only considers tokens with top P total probability."
                                )
                            }
                        }
                    }
                }
                
                // Card 4: Safety Settings (Gemini Specific)
                if (capabilities.supportsSafetySettings) {
                    item {
                        ConfigCard(title = "Content Safety Thresholds", icon = Icons.Default.Shield) {
                            SafetyThresholdSelector(
                                label = "Harassment",
                                current = uiState.safetyHarassment,
                                onSelected = { viewModel.updateSafetyHarassment(it) }
                            )
                            SafetyThresholdSelector(
                                label = "Hate Speech",
                                current = uiState.safetyHateSpeech,
                                onSelected = { viewModel.updateSafetyHateSpeech(it) }
                            )
                            SafetyThresholdSelector(
                                label = "Sexually Explicit",
                                current = uiState.safetySexuallyExplicit,
                                onSelected = { viewModel.updateSafetySexuallyExplicit(it) }
                            )
                            SafetyThresholdSelector(
                                label = "Dangerous Content",
                                current = uiState.safetyDangerousContent,
                                onSelected = { viewModel.updateSafetyDangerousContent(it) }
                            )
                        }
                    }
                }
                
                // Card 5: Output Controls
                if (capabilities.supportsMaxTokens || capabilities.supportsStopSequences) {
                    item {
                        ConfigCard(title = "Output Constraints") {
                            if (capabilities.supportsMaxTokens) {
                                ParameterSlider(
                                    label = "Max Output Tokens",
                                    value = uiState.maxTokens.toFloat(),
                                    range = 1f..8192f,
                                    steps = 128,
                                    onValueChange = { viewModel.updateMaxTokens(it.roundToInt()) },
                                    description = "The maximum length of the AI's response.",
                                    isInteger = true
                                )
                            }
                            
                            if (capabilities.supportsStopSequences) {
                                if (capabilities.supportsMaxTokens) HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.05f))
                                OutlinedTextField(
                                    value = uiState.stopSequences,
                                    onValueChange = { viewModel.updateStopSequences(it) },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Stop Sequences") },
                                    placeholder = { Text("Comma separated...") },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                        unfocusedTextColor = Color.White,
                                        focusedTextColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
                
                // Card 6: Penalties (Groq Specific)
                if (capabilities.supportsPresencePenalty || capabilities.supportsFrequencyPenalty) {
                    item {
                        ConfigCard(title = "Penalty Settings") {
                            if (capabilities.supportsPresencePenalty) {
                                ParameterSlider(
                                    label = "Presence Penalty",
                                    value = uiState.presencePenalty,
                                    range = -2f..2f,
                                    onValueChange = { viewModel.updatePresencePenalty(it) },
                                    description = "Prevent repeating the same topics."
                                )
                            }
                            
                            if (capabilities.supportsFrequencyPenalty) {
                                if (capabilities.supportsPresencePenalty) HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.05f))
                                ParameterSlider(
                                    label = "Frequency Penalty",
                                    value = uiState.frequencyPenalty,
                                    range = -2f..2f,
                                    onValueChange = { viewModel.updateFrequencyPenalty(it) },
                                    description = "Prevent repeating the exact same words."
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
