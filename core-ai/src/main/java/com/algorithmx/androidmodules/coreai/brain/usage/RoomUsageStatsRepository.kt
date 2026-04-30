package com.algorithmx.androidmodules.coreai.brain.usage

import android.content.Context
import com.algorithmx.androidmodules.coreai.brain.models.BrainProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow

class RoomUsageStatsRepository(
    private val usageStatsDao: UsageStatsDao,
    context: Context
) : UsageStatsRepository {

    private val defaultAppId = context.packageName

    override suspend fun logUsage(
        provider: BrainProvider,
        modelUsed: String,
        tokensEstimated: Int,
        isSuccess: Boolean,
        errorMessage: String?
    ) {
        usageStatsDao.insertUsage(
            UsageEventEntity(
                appId = defaultAppId,
                provider = provider.name,
                modelUsed = modelUsed,
                tokensEstimated = tokensEstimated,
                isSuccess = isSuccess,
                errorMessage = errorMessage,
                createdAtEpochMs = System.currentTimeMillis()
            )
        )
    }

    override fun observeModelUsageSummary(appId: String?): Flow<List<ModelUsageSummary>> {
        return usageStatsDao.observeModelUsageSummary(appId ?: defaultAppId)
    }

    override fun observeTotalUsageSummary(appId: String?): Flow<TotalUsageSummary> {
        return usageStatsDao.observeTotalUsageSummary(appId ?: defaultAppId)
    }
}
