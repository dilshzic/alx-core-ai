package com.algorithmx.androidmodules.coreai.brain.registry

import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import com.algorithmx.androidmodules.coreai.brain.models.BrainCategory
import com.algorithmx.androidmodules.coreai.brain.models.BrainProvider

@Serializable
data class ModelEntry(
    val provider: String,
    val name: String,
    val code: String,
    val output: String = "text",
    val notes: String = ""
)

object BrainRegistry {

    private val modelProviderMap: Map<String, BrainProvider> by lazy {
        loadModelProviderMapFromJson()
    }

    val allModelCodes: List<String> by lazy {
        modelProviderMap.keys.toList().sorted()
    }

    val categoryMap: Map<BrainCategory, List<String>> by lazy {
        loadFromJsonCategories()
    }

    private val json = Json { ignoreUnknownKeys = true }

    private fun loadModelProviderMapFromJson(): Map<String, BrainProvider> {
        val map = mutableMapOf<String, BrainProvider>()
        val jsonString = loadJsonString() ?: return emptyMap()

        try {
            val entries = json.decodeFromString<List<ModelEntry>>(jsonString)
            android.util.Log.d("BrainRegistry", "Decoded ${entries.size} entries from JSON")
            entries.forEach { entry ->
                val provider = when (entry.provider.lowercase()) {
                    "google" -> BrainProvider.GEMINI
                    "groq" -> BrainProvider.GROQ
                    "local" -> BrainProvider.LOCAL_GEMMA
                    else -> BrainProvider.GROQ
                }
                map[entry.code] = provider
            }
        } catch (e: Exception) {
            android.util.Log.e("BrainRegistry", "Error decoding JSON: ${e.message}")
            e.printStackTrace()
        }
        return map
    }

    private fun loadFromJsonCategories(): Map<BrainCategory, List<String>> {
        val reasoning = mutableListOf<String>()
        val functionCalling = mutableListOf<String>()
        val textToText = mutableListOf<String>()
        val vision = mutableListOf<String>()
        val multilingual = mutableListOf<String>()

        val jsonString = loadJsonString() ?: return emptyMap()

        try {
            val entries = json.decodeFromString<List<ModelEntry>>(jsonString)
            entries.forEach { entry ->
                val code = entry.code
                val notes = entry.notes.lowercase()
                
                textToText.add(code)
                if (notes.contains("reasoning") || code.contains("pro") || code.contains("70b")) {
                    reasoning.add(code)
                }
                if (notes.contains("function") || notes.contains("tool") || code.contains("llama-3.1") || code.contains("gemini")) {
                    functionCalling.add(code)
                }
                if (notes.contains("vision") || code.contains("vision")) {
                    vision.add(code)
                }
                if (notes.contains("multilingual") || notes.contains("arabic")) {
                    multilingual.add(code)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return mapOf(
            BrainCategory.REASONING to reasoning.distinct(),
            BrainCategory.FUNCTION_CALLING to functionCalling.distinct(),
            BrainCategory.TEXT_TO_TEXT to textToText.distinct(),
            BrainCategory.VISION to vision.distinct(),
            BrainCategory.MULTILINGUAL to multilingual.distinct()
        )
    }

    private fun loadJsonString(): String? {
        val path1 = "unified_model_catalog.json"
        val path2 = "/com/algorithmx/androidmodules/coreai/brain/registry/unified_model_catalog.json"
        
        var stream = this::class.java.getResourceAsStream(path1)
        if (stream == null) {
            android.util.Log.d("BrainRegistry", "Stream null for $path1, trying $path2")
            stream = this::class.java.getResourceAsStream(path2)
        }
        
        if (stream == null) {
            android.util.Log.e("BrainRegistry", "Failed to load model catalog JSON from resources.")
            return null
        }
        
        return try {
            val text = stream.bufferedReader().use { it.readText() }
            android.util.Log.d("BrainRegistry", "Successfully loaded JSON string (length: ${text.length})")
            text
        } catch (e: Exception) {
            android.util.Log.e("BrainRegistry", "Error reading JSON: ${e.message}")
            null
        }
    }

    fun getProviderForModel(modelName: String): BrainProvider {
        return modelProviderMap[modelName] ?: BrainProvider.GROQ
    }
}
