package com.algorithmx.alx_core_ai.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.algorithmx.q_base.core_ai.brain.AiBrainManager
import com.algorithmx.q_base.core_ai.brain.BrainDataStoreManager
import com.algorithmx.q_base.core_ai.brain.models.BrainCategory
import com.algorithmx.q_base.core_ai.brain.models.BrainProvider
import com.algorithmx.q_base.core_ai.brain.models.StoredBrainConfig
import com.algorithmx.q_base.core_ai.brain.registry.BrainRegistry
import com.algorithmx.q_base.core_ai.brain.usage.ModelUsageSummary
import com.algorithmx.q_base.core_ai.brain.usage.TotalUsageSummary
import com.algorithmx.q_base.core_ai.brain.usage.UsageStatsRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiDemoScreen(
    aiBrainManager: AiBrainManager,
    dataStoreManager: BrainDataStoreManager,
    usageStatsRepository: UsageStatsRepository
) {
    val scope = rememberCoroutineScope()
    val defaultConfig = remember { defaultStoredConfig() }
    val config by dataStoreManager.brainConfigFlow.collectAsState(initial = defaultConfig)
    val totalSummary by usageStatsRepository
        .observeTotalUsageSummary()
        .collectAsState(initial = TotalUsageSummary(0, 0))
    val modelSummaries by usageStatsRepository
        .observeModelUsageSummary()
        .collectAsState(initial = emptyList())

    val allModels = remember {
        BrainRegistry.categoryMap.values.flatten().distinct().sorted()
    }

    var selectedCategory by remember { mutableStateOf(config.category) }
    var selectedModel by remember { mutableStateOf(config.modelName) }
    var systemInstruction by remember { mutableStateOf(config.systemInstruction) }
    var prompt by remember { mutableStateOf("") }
    var response by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isJsonMode by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val modelsForCategory = remember(selectedCategory, allModels) {
        val categoryModels = BrainRegistry.categoryMap[selectedCategory].orEmpty()
        if (categoryModels.isNotEmpty()) categoryModels else allModels
    }

    LaunchedEffect(config.category) {
        selectedCategory = config.category
    }

    LaunchedEffect(config.modelName, modelsForCategory) {
        selectedModel = when {
            modelsForCategory.contains(config.modelName) -> config.modelName
            modelsForCategory.isNotEmpty() -> modelsForCategory.first()
            else -> config.modelName
        }
    }

    LaunchedEffect(config.systemInstruction) {
        systemInstruction = config.systemInstruction
    }

    val backgroundBrush = remember {
        Brush.verticalGradient(
            listOf(
                Color(0xFFF8F2EA),
                Color(0xFFE6EFE5)
            )
        )
    }

    fun persistConfig(model: String, category: BrainCategory, instruction: String) {
        val provider = BrainRegistry.getProviderForModel(model)
        scope.launch {
            dataStoreManager.saveEngineConfiguration(
                provider = provider,
                modelName = model,
                category = category,
                systemInstruction = instruction
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(backgroundBrush)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Core AI Playground",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    SectionCard(title = "Active Model") {
                        Text(
                            text = "Provider: ${config.provider}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Model: ${config.modelName}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Category: ${config.category}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                item {
                    SectionCard(title = "Model Configuration") {
                        DropdownField(
                            label = "Category",
                            options = BrainCategory.values().toList(),
                            selected = selectedCategory,
                            optionLabel = { it.name },
                            onSelected = { category ->
                                selectedCategory = category
                                val nextModel = BrainRegistry.categoryMap[category]
                                    ?.firstOrNull()
                                    ?: allModels.firstOrNull()
                                    ?: selectedModel
                                selectedModel = nextModel
                                persistConfig(nextModel, category, systemInstruction)
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        DropdownField(
                            label = "Model",
                            options = modelsForCategory,
                            selected = selectedModel,
                            optionLabel = { it },
                            onSelected = { model ->
                                selectedModel = model
                                persistConfig(model, selectedCategory, systemInstruction)
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = systemInstruction,
                            onValueChange = { systemInstruction = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("System instruction") },
                            minLines = 2
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = {
                                systemInstruction = config.systemInstruction
                            }) {
                                Text("Reset")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = {
                                persistConfig(selectedModel, selectedCategory, systemInstruction)
                            }) {
                                Text("Apply")
                            }
                        }
                    }
                }

                item {
                    SectionCard(title = "Prompt") {
                        OutlinedTextField(
                            value = prompt,
                            onValueChange = { prompt = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Prompt") },
                            minLines = 4
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Switch(
                                checked = isJsonMode,
                                onCheckedChange = { isJsonMode = it }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("JSON mode")
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isLoading) {
                                Text(
                                    text = "Generating...",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Button(
                                onClick = {
                                    if (prompt.isBlank()) {
                                        errorMessage = "Enter a prompt before generating."
                                        return@Button
                                    }
                                    scope.launch {
                                        isLoading = true
                                        errorMessage = null
                                        response = ""
                                        val result = aiBrainManager.askBrain(prompt, isJsonMode)
                                        result.onSuccess {
                                            response = it
                                        }.onFailure {
                                            errorMessage = it.message ?: "Request failed."
                                        }
                                        isLoading = false
                                    }
                                },
                                enabled = !isLoading,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("Generate")
                            }
                        }
                        AnimatedVisibility(
                            visible = errorMessage != null,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Text(
                                text = errorMessage.orEmpty(),
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        AnimatedVisibility(
                            visible = response.isNotBlank(),
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                Divider()
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = response,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                item {
                    SectionCard(title = "Usage Summary") {
                        UsageMetricRow(
                            label = "Total requests",
                            value = totalSummary.totalRequests.toString()
                        )
                        UsageMetricRow(
                            label = "Estimated tokens",
                            value = totalSummary.totalTokens.toString()
                        )
                        if (modelSummaries.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Top models",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            ModelUsageList(modelSummaries.take(3))
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> DropdownField(
    label: String,
    options: List<T>,
    selected: T,
    optionLabel: (T) -> String,
    onSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = optionLabel(selected),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            content.invoke()
        }
    }
}

@Composable
private fun UsageMetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ModelUsageList(items: List<ModelUsageSummary>) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        items.forEach { summary ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = summary.modelUsed, style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "${summary.totalRequests} req / ${summary.totalTokens} tok",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private fun defaultStoredConfig(): StoredBrainConfig {
    return StoredBrainConfig(
        provider = BrainProvider.GEMINI,
        modelName = "gemini-3.1-flash-lite-preview",
        systemInstruction = "You are a helpful knowledge assistant.",
        totalRequests = 0,
        totalTokens = 0,
        category = BrainCategory.TEXT_TO_TEXT,
        themeMode = "SYSTEM",
        notificationsEnabled = true,
        isMasterAiFreeze = false
    )
}
