package com.algorithmx.androidmodules.coreai.brain.usage

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "usage_events",
    indices = [
        Index(value = ["appId"]),
        Index(value = ["modelUsed"])
    ]
)
data class UsageEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val appId: String,
    val provider: String,
    val modelUsed: String,
    val tokensEstimated: Int,
    val isSuccess: Boolean,
    val errorMessage: String?,
    val createdAtEpochMs: Long
)
