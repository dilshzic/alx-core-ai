package com.algorithmx.alx_core_ai.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.algorithmx.alx_core_ai.viewmodels.BrainConfigViewModel
import com.algorithmx.androidmodules.coreai.brain.AiBrainManager
import com.algorithmx.androidmodules.coreai.brain.BrainDataStoreManager
import com.algorithmx.androidmodules.coreai.brain.models.BrainCapabilities
import com.algorithmx.androidmodules.coreai.brain.models.BrainCategory
import com.algorithmx.androidmodules.coreai.brain.models.BrainProvider
import com.algorithmx.androidmodules.coreai.brain.models.StoredBrainConfig
import com.algorithmx.androidmodules.coreai.brain.registry.BrainRegistry
import com.algorithmx.androidmodules.coreai.brain.usage.ModelUsageSummary
import com.algorithmx.androidmodules.coreai.brain.usage.TotalUsageSummary
import com.algorithmx.androidmodules.coreai.brain.usage.UsageStatsRepository
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiDemoScreen(
    aiBrainManager: AiBrainManager,
    dataStoreManager: BrainDataStoreManager,
    usageStatsRepository: UsageStatsRepository,
    configViewModel: BrainConfigViewModel,
    onOpenConfig: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val config by configViewModel.config.collectAsState()
    val uiState by configViewModel.uiState.collectAsState()
    
    val totalSummary by usageStatsRepository
        .observeTotalUsageSummary()
        .collectAsState(initial = TotalUsageSummary(0, 0))
    val modelSummaries by usageStatsRepository
        .observeModelUsageSummary()
        .collectAsState(initial = emptyList())

    val allModels = remember { BrainRegistry.allModelCodes }

    // Draft state for primary fields
    var draftModel by remember { mutableStateOf("") }
    var draftSystemInstruction by remember { mutableStateOf("") }
    var prompt by remember { mutableStateOf("") }
    var response by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isJsonMode by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

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
                TopAppBar(
                    title = {
                        Text(
                            text = "Core AI Playground",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    actions = {
                        IconButton(onClick = { configViewModel.resetToBaseline() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset to Baseline",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = onOpenConfig) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Advanced Settings",
                                tint = Color.White
                            )
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
                // Active Engine Badge
                item {
                    ProviderBadge(provider = currentProvider)
                }

                // Card 1: Identity & Rapid Model Switch
                item {
                    ConfigCard(title = "Identity & Model", icon = Icons.Default.Info) {
                        DropdownField(
                            label = "Select Model",
                            options = allModels,
                            selected = draftModel,
                            optionLabel = { it },
                            onSelected = { 
                                draftModel = it
                                configViewModel.saveConfiguration(it, draftSystemInstruction)
                            }
                        )
                        
                        if (capabilities.supportsSystemInstruction) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = draftSystemInstruction,
                                onValueChange = { 
                                    draftSystemInstruction = it
                                },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("System Persona") },
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                    unfocusedTextColor = Color.White,
                                    focusedTextColor = Color.White
                                )
                            )
                            
                            // Apply button for system instruction (since it's text)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = { 
                                    configViewModel.saveConfiguration(draftModel, draftSystemInstruction)
                                }) {
                                    Text("Apply Persona", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Card 2: Interactive Prompt
                item {
                    ConfigCard(title = "Inference") {
                        OutlinedTextField(
                            value = prompt,
                            onValueChange = { prompt = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Prompt") },
                            minLines = 4,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                unfocusedTextColor = Color.White,
                                focusedTextColor = Color.White
                            )
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(
                                    checked = isJsonMode,
                                    onCheckedChange = { isJsonMode = it }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("JSON Mode", color = Color.White, fontSize = 14.sp)
                            }
                            
                            Button(
                                onClick = {
                                    if (prompt.isBlank()) {
                                        errorMessage = "Enter a prompt."
                                        return@Button
                                    }
                                    scope.launch {
                                        isLoading = true
                                        errorMessage = null
                                        response = ""
                                        val result = aiBrainManager.askBrain(prompt, isJsonMode)
                                        result.onSuccess { response = it }
                                              .onFailure { errorMessage = it.message ?: "Request failed." }
                                        isLoading = false
                                    }
                                },
                                enabled = !isLoading,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Text("Generate")
                                }
                            }
                        }

                        AnimatedVisibility(visible = errorMessage != null) {
                            Text(text = errorMessage.orEmpty(), color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }

                        if (response.isNotBlank()) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.1f))
                            Text(
                                text = response,
                                color = Color.White.copy(alpha = 0.9f),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Card 3: Quick Tuning Clusters
                item {
                    ConfigCard(title = "Parameter Tuning") {
                        if (capabilities.supportsTemperature) {
                            ParameterSlider(
                                label = "Temperature",
                                value = uiState.temperature,
                                range = 0f..2f,
                                onValueChange = { 
                                    configViewModel.updateTemperature(it)
                                    // We'll save when the user stops sliding or via a debounce in real app, 
                                    // but for demo we can add a small "Save" button or just let it be volatile until manually saved in config screen.
                                    // Actually, let's make them save immediately for better UX.
                                    configViewModel.saveConfiguration(draftModel, draftSystemInstruction)
                                },
                                description = "Creativity level."
                            )
                        }
                        
                        if (capabilities.supportsMaxTokens) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.05f))
                            ParameterSlider(
                                label = "Max Tokens",
                                value = uiState.maxTokens.toFloat(),
                                range = 1f..4096f,
                                steps = 64,
                                onValueChange = { 
                                    configViewModel.updateMaxTokens(it.roundToInt())
                                    configViewModel.saveConfiguration(draftModel, draftSystemInstruction)
                                },
                                description = "Response length limit.",
                                isInteger = true
                            )
                        }
                    }
                }

                // Card 4: Usage Stats
                item {
                    ConfigCard(title = "Usage Analytics") {
                        UsageMetricRow("Total Requests", totalSummary.totalRequests.toString())
                        UsageMetricRow("Token Consumption", totalSummary.totalTokens.toString())
                        
                        if (modelSummaries.isNotEmpty()) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.05f))
                            Text("Top Models", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.5f))
                            modelSummaries.take(3).forEach { summary ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(summary.modelUsed, color = Color.White, fontSize = 12.sp)
                                    Text("${summary.totalRequests} req", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UsageMetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
        Text(text = value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

private fun defaultStoredConfig(): StoredBrainConfig {
    return StoredBrainConfig(
        provider = BrainProvider.GEMINI,
        modelName = "gemini-1.5-flash",
        systemInstruction = "You are a helpful knowledge assistant.",
        totalRequests = 0,
        totalTokens = 0,
        category = BrainCategory.TEXT_TO_TEXT,
        themeMode = "SYSTEM",
        notificationsEnabled = true,
        isMasterAiFreeze = false
    )
}
