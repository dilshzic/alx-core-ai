package com.algorithmx.androidmodules.coreai.brain.di

import com.algorithmx.androidmodules.coreai.brain.AiBrainManager
import com.algorithmx.androidmodules.coreai.brain.BrainDataStoreManager
import com.algorithmx.androidmodules.coreai.brain.AiUsageLogger
import com.algorithmx.androidmodules.coreai.brain.BrainConfigProvider
import com.algorithmx.androidmodules.coreai.brain.NoOpAiUsageLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.Optional
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AiCoreModule {

    @Provides
    @Singleton
    fun provideAiBrainManager(
        dataStoreManager: BrainDataStoreManager,
        usageLogger: Optional<AiUsageLogger>,
        configProvider: BrainConfigProvider
    ): AiBrainManager {
        return AiBrainManager(
            dataStoreManager = dataStoreManager,
            usageLogger = usageLogger.orElse(NoOpAiUsageLogger),
            configProvider = configProvider
        )
    }

}
