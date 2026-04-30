
package com.algorithmx.q_base.core_ai.brain.registry

import com.algorithmx.q_base.core_ai.brain.models.BrainCategory
import com.algorithmx.q_base.core_ai.brain.models.BrainProvider
import java.io.BufferedReader
import java.io.InputStreamReader

object BrainRegistry {

    val categoryMap: Map<BrainCategory, List<String>> by lazy {
        loadFromCsvOrFallback()
    }

    private fun loadFromCsvOrFallback(): Map<BrainCategory, List<String>> {
        val reasoning = mutableListOf<String>()
        val functionCalling = mutableListOf<String>()
        val textToText = mutableListOf<String>()

        val stream = this::class.java.getResourceAsStream("unified_model_catalog.csv")
            ?: this::class.java.getResourceAsStream("/com/algorithmx/q_base/core_ai/brain/registry/unified_model_catalog.csv")

        if (stream != null) {
            try {
                val reader = BufferedReader(InputStreamReader(stream))
                var lineCount = 0
                
                reader.forEachLine { line ->
                    lineCount++
                    if (lineCount == 1 || line.isBlank()) return@forEachLine
                    
                    // Simple CSV line parser
                    val parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*${'$'})".toRegex())
                    if (parts.size >= 11) {
                        val modelCode = parts[2].trim().removeSurrounding("\"")
                        if (modelCode.isEmpty()) return@forEachLine
                        
                        val outputType = parts[3].trim().removeSurrounding("\"")
                        val notes = parts.subList(10, parts.size).joinToString(",").lowercase()
                        
                        if (outputType == "text") {
                            textToText.add(modelCode)
                            if (notes.contains("reasoning")) {
                                reasoning.add(modelCode)
                            }
                            if (notes.contains("function calling") || notes.contains("tool use") || notes.contains("agentic")) {
                                functionCalling.add(modelCode)
                            } else if (modelCode.contains("llama-3.1") || modelCode.contains("llama-3.3") || modelCode.contains("compound")) {
                                functionCalling.add(modelCode)
                            }
                        }
                    }
                }
                
                return mapOf(
                    BrainCategory.REASONING to reasoning.distinct(),
                    BrainCategory.FUNCTION_CALLING to functionCalling.distinct(),
                    BrainCategory.TEXT_TO_TEXT to textToText.distinct()
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        // Fallback to static list
        return mapOf(
            BrainCategory.REASONING to listOf("gemini-2.5-pro", "llama-3.3-70b-versatile"),
            BrainCategory.FUNCTION_CALLING to listOf("llama-3.3-70b-versatile", "meta-llama/llama-4-scout-17b-16e-instruct", "groq/compound"),
            BrainCategory.TEXT_TO_TEXT to listOf("gemini-2.5-flash", "llama-3.1-8b-instant")
        )
    }

    fun getProviderForModel(modelName: String): BrainProvider {
        return when {
            modelName.startsWith("gem") -> BrainProvider.GEMINI
            modelName.startsWith("lyria") -> BrainProvider.GEMINI
            modelName.startsWith("veo") -> BrainProvider.GEMINI
            modelName.startsWith("gemma-local") -> BrainProvider.LOCAL_GEMMA
            else -> BrainProvider.GROQ
        }
    }
}
