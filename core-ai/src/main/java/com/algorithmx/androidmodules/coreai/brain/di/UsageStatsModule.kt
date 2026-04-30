package com.algorithmx.androidmodules.coreai.brain.di

import com.algorithmx.androidmodules.coreai.brain.AiUsageLogger
import com.algorithmx.androidmodules.coreai.brain.usage.UsageStatsRepository
import dagger.BindsOptionalOf
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class UsageStatsModule {

    @BindsOptionalOf
    abstract fun optionalAiUsageLogger(): AiUsageLogger

    @BindsOptionalOf
    abstract fun optionalUsageStatsRepository(): UsageStatsRepository
}
