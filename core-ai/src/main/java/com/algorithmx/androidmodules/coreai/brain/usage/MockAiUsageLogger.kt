package com.algorithmx.androidmodules.coreai.brain.usage

import com.algorithmx.androidmodules.coreai.brain.AiUsageLogger
import com.algorithmx.androidmodules.coreai.brain.models.BrainProvider

/**
 * Mock implementation of AiUsageLogger for temporary use without Room database.
 */
class MockAiUsageLogger : AiUsageLogger {
    override suspend fun logUsage(
        provider: BrainProvider,
        modelUsed: String,
        tokensEstimated: Int,
        isSuccess: Boolean,
        errorMessage: String?
    ) {
        // No-op: just silently ignore usage logging
    }
}
