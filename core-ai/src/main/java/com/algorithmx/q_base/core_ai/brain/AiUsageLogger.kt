package com.algorithmx.q_base.core_ai.brain

import com.algorithmx.q_base.core_ai.brain.models.BrainProvider
interface AiUsageLogger {
    suspend fun logUsage(
        provider: BrainProvider,
        modelUsed: String,
        tokensEstimated: Int,
        isSuccess: Boolean,
        errorMessage: String? = null
    )
}
