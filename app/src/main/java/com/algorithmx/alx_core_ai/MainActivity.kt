package com.algorithmx.alx_core_ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.algorithmx.alx_core_ai.ui.AiDemoScreen
import com.algorithmx.alx_core_ai.ui.theme.AppTheme
import com.algorithmx.q_base.core_ai.brain.AiBrainManager
import com.algorithmx.q_base.core_ai.brain.BrainDataStoreManager
import com.algorithmx.q_base.core_ai.brain.usage.UsageStatsRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var aiBrainManager: AiBrainManager
    @Inject lateinit var dataStoreManager: BrainDataStoreManager
    @Inject lateinit var usageStatsRepository: UsageStatsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                AiDemoScreen(
                    aiBrainManager = aiBrainManager,
                    dataStoreManager = dataStoreManager,
                    usageStatsRepository = usageStatsRepository
                )
            }
        }
    }
}
