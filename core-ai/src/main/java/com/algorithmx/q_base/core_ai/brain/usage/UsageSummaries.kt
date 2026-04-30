package com.algorithmx.q_base.core_ai.brain.usage

data class ModelUsageSummary(
    val modelUsed: String,
    val totalTokens: Long,
    val totalRequests: Long
)

data class TotalUsageSummary(
    val totalTokens: Long,
    val totalRequests: Long
)
