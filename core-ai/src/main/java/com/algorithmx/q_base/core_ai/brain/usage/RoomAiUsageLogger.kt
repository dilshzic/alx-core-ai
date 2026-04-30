package com.algorithmx.q_base.core_ai.brain.usage

import com.algorithmx.q_base.core_ai.brain.AiUsageLogger
import com.algorithmx.q_base.core_ai.brain.models.BrainProvider

class RoomAiUsageLogger(
    private val usageStatsRepository: UsageStatsRepository
) : AiUsageLogger {

    override suspend fun logUsage(
        provider: BrainProvider,
        modelUsed: String,
        tokensEstimated: Int,
        isSuccess: Boolean,
        errorMessage: String?
    ) {
        usageStatsRepository.logUsage(
            provider = provider,
            modelUsed = modelUsed,
            tokensEstimated = tokensEstimated,
            isSuccess = isSuccess,
            errorMessage = errorMessage
        )
    }
}
