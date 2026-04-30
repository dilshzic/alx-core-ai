package com.algorithmx.alx_core_ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.algorithmx.alx_core_ai.ui.AiDemoScreen
import com.algorithmx.alx_core_ai.ui.theme.AppTheme
import com.algorithmx.androidmodules.coreai.brain.AiBrainManager
import com.algorithmx.androidmodules.coreai.brain.BrainDataStoreManager
import com.algorithmx.androidmodules.coreai.brain.usage.UsageStatsRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

import androidx.compose.runtime.*
import androidx.activity.viewModels
import com.algorithmx.alx_core_ai.ui.UnifiedConfigScreen
import com.algorithmx.alx_core_ai.viewmodels.BrainConfigViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var aiBrainManager: AiBrainManager
    @Inject lateinit var dataStoreManager: BrainDataStoreManager
    @Inject lateinit var usageStatsRepository: UsageStatsRepository
    
    private val configViewModel: BrainConfigViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                var currentScreen by remember { mutableStateOf("demo") }
                
                if (currentScreen == "demo") {
                    AiDemoScreen(
                        aiBrainManager = aiBrainManager,
                        dataStoreManager = dataStoreManager,
                        usageStatsRepository = usageStatsRepository,
                        configViewModel = configViewModel,
                        onOpenConfig = { currentScreen = "config" }
                    )
                } else {
                    UnifiedConfigScreen(
                        viewModel = configViewModel,
                        onBack = { currentScreen = "demo" }
                    )
                }
            }
        }
    }
}
