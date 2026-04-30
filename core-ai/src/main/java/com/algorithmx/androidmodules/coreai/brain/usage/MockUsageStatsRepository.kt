package com.algorithmx.androidmodules.coreai.brain.usage

import com.algorithmx.androidmodules.coreai.brain.models.BrainProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Mock implementation of UsageStatsRepository for temporary use without Room database.
 * This is used to unblock app development while we resolve Hilt+Room metadata compatibility issues.
 */
class MockUsageStatsRepository : UsageStatsRepository {
    override suspend fun logUsage(
        provider: BrainProvider,
        modelUsed: String,
        tokensEstimated: Int,
        isSuccess: Boolean,
        errorMessage: String?
    ) {
        // No-op: just silently ignore usage logging
    }

    override fun observeModelUsageSummary(appId: String?): Flow<List<ModelUsageSummary>> {
        return flowOf(emptyList())
    }

    override fun observeTotalUsageSummary(appId: String?): Flow<TotalUsageSummary> {
        return flowOf(TotalUsageSummary(totalRequests = 0, totalTokens = 0))
    }
}
