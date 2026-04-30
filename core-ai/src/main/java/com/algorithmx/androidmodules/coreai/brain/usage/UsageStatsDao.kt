package com.algorithmx.androidmodules.coreai.brain.usage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageStatsDao {
    @Insert
    suspend fun insertUsage(event: UsageEventEntity)

    @Query(
        "SELECT modelUsed, SUM(tokensEstimated) AS totalTokens, COUNT(*) AS totalRequests " +
            "FROM usage_events WHERE appId = :appId GROUP BY modelUsed ORDER BY totalTokens DESC"
    )
    fun observeModelUsageSummary(appId: String): Flow<List<ModelUsageSummary>>

    @Query(
        "SELECT IFNULL(SUM(tokensEstimated), 0) AS totalTokens, COUNT(*) AS totalRequests " +
            "FROM usage_events WHERE appId = :appId"
    )
    fun observeTotalUsageSummary(appId: String): Flow<TotalUsageSummary>
}
