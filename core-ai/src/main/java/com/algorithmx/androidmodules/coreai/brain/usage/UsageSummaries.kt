package com.algorithmx.androidmodules.coreai.brain.usage

data class ModelUsageSummary(
    val modelUsed: String,
    val totalTokens: Long,
    val totalRequests: Long
)

data class TotalUsageSummary(
    val totalTokens: Long,
    val totalRequests: Long
)
