package com.algorithmx.androidmodules.coreai.brain.usage

import com.algorithmx.androidmodules.coreai.brain.models.BrainProvider
import kotlinx.coroutines.flow.Flow

interface UsageStatsRepository {
    suspend fun logUsage(
        provider: BrainProvider,
        modelUsed: String,
        tokensEstimated: Int,
        isSuccess: Boolean,
        errorMessage: String? = null
    )

    fun observeModelUsageSummary(appId: String? = null): Flow<List<ModelUsageSummary>>

    fun observeTotalUsageSummary(appId: String? = null): Flow<TotalUsageSummary>
}
