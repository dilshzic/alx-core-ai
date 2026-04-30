package com.algorithmx.alx_core_ai.di

import com.algorithmx.androidmodules.coreai.brain.AiUsageLogger
import com.algorithmx.androidmodules.coreai.brain.BrainConfigProvider
import com.algorithmx.androidmodules.coreai.brain.models.BrainProvider
import com.algorithmx.androidmodules.coreai.brain.usage.MockAiUsageLogger
import com.algorithmx.androidmodules.coreai.brain.usage.MockUsageStatsRepository
import com.algorithmx.androidmodules.coreai.brain.usage.UsageStatsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppBrainModule {

    @Provides
    @Singleton
    fun provideBrainConfigProvider(): BrainConfigProvider {
        return object : BrainConfigProvider {
            override suspend fun getApiKey(provider: BrainProvider): String {
                return when (provider) {
                    BrainProvider.GEMINI -> com.algorithmx.androidmodules.coreai.BuildConfig.GEMINI_API_KEY
                    BrainProvider.GROQ -> com.algorithmx.androidmodules.coreai.BuildConfig.GROQ_API_KEY
                    BrainProvider.LOCAL_GEMMA -> ""
                }
            }
        }
    }

    @Provides
    @Singleton
    fun provideUsageStatsRepository(): UsageStatsRepository {
        return MockUsageStatsRepository()
    }

    @Provides
    @Singleton
    fun provideAiUsageLogger(): AiUsageLogger {
        return MockAiUsageLogger()
    }
}
