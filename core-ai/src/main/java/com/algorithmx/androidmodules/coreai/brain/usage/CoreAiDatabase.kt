package com.algorithmx.androidmodules.coreai.brain.usage

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [UsageEventEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CoreAiDatabase : RoomDatabase() {
    abstract fun usageStatsDao(): UsageStatsDao
}
