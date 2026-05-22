package com.algorithmx.androidmodules.coreai.brain

import com.algorithmx.androidmodules.coreai.brain.models.BrainProvider

interface AiUsageLogger {
    suspend fun logUsage(
        provider: BrainProvider,
        modelUsed: String,
        tokensEstimated: Int,
        isSuccess: Boolean,
        errorMessage: String? = null
    )
}
