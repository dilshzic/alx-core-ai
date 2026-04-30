package com.algorithmx.androidmodules.coreai.brain

import com.algorithmx.androidmodules.coreai.brain.models.BrainProvider

interface BrainConfigProvider {
    suspend fun getApiKey(provider: BrainProvider): String
}
