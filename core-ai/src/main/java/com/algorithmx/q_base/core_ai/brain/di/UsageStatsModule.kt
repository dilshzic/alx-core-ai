package com.algorithmx.q_base.core_ai.brain.di

import android.content.Context
import com.algorithmx.q_base.core_ai.brain.AiUsageLogger
import com.algorithmx.q_base.core_ai.brain.usage.UsageStatsRepository
import com.algorithmx.q_base.core_ai.brain.usage.MockUsageStatsRepository
import com.algorithmx.q_base.core_ai.brain.usage.MockAiUsageLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UsageStatsModule {

    // TEMPORARY: Using mock implementations to bypass Hilt+Room+Kotlin 2.1.0 metadata issue
    // TODO: Re-enable Room database once metadata compatibility is fixed

    @Provides
    @Singleton
    fun provideUsageStatsRepository(
        @ApplicationContext context: Context
    ): UsageStatsRepository {
        return MockUsageStatsRepository()
    }

    @Provides
    @Singleton
    fun provideAiUsageLogger(usageStatsRepository: UsageStatsRepository): AiUsageLogger {
        return MockAiUsageLogger()
    }
}
