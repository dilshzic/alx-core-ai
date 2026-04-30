package com.algorithmx.alx_core_ai.di

import com.algorithmx.q_base.core_ai.brain.BrainConfigProvider
import com.algorithmx.q_base.core_ai.brain.models.BrainProvider
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
                    BrainProvider.GEMINI -> com.algorithmx.q_base.core_ai.BuildConfig.GEMINI_API_KEY
                    BrainProvider.GROQ -> com.algorithmx.q_base.core_ai.BuildConfig.GROQ_API_KEY
                    BrainProvider.LOCAL_GEMMA -> ""
                }
            }
        }
    }
}
