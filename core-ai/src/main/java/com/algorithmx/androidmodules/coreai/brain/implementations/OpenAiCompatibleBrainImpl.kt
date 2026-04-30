package com.algorithmx.androidmodules.coreai.brain.implementations

import com.algorithmx.androidmodules.coreai.brain.AiBrain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class OpenAiCompatibleBrainImpl(
    private val apiKey: String,
    private val modelName: String,
    private val baseUrl: String,
    private val systemInstruction: String? = null,
    private val temperature: Double? = null,
    private val maxCompletionTokens: Int? = null,
    private val topP: Double? = null,
    private val reasoningEffort: String? = null,
    private val stopSequences: List<String>? = null,
    private val tools: JSONArray? = null
) : AiBrain {

    private val client = OkHttpClient()
    private val mediaType = "application/json; charset=utf-8".toMediaType()

    override suspend fun generateText(prompt: String, isJsonMode: Boolean): Result<String> = withContext(Dispatchers.IO) {
        try {
            val jsonPayload = JSONObject().apply {
                put("model", modelName)
                
                temperature?.let { put("temperature", it) }
                maxCompletionTokens?.let { put("max_completion_tokens", it) }
                topP?.let { put("top_p", it) }
                reasoningEffort?.let { put("reasoning_effort", it) }
                
                stopSequences?.let { 
                    put("stop", JSONArray(it))
                }
                
                tools?.let { put("tools", it) }

                if (isJsonMode) {
                    put("response_format", JSONObject().apply {
                        put("type", "json_object")
                    })
                }
                
                val messagesArray = JSONArray()
                
                if (systemInstruction != null) {
                    messagesArray.put(JSONObject().apply {
                        put("role", "system")
                        put("content", systemInstruction)
                    })
                }
                
                messagesArray.put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
                
                put("messages", messagesArray)
            }

            val request = Request.Builder()
                .url(baseUrl)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .post(jsonPayload.toString().toRequestBody(mediaType))
                .build()

            android.util.Log.d("GroqDebug", "Request URL: $baseUrl")
            android.util.Log.d("GroqDebug", "Request Model: $modelName")

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response body"))
                
                android.util.Log.d("GroqDebug", "Response Code: ${response.code}")
                if (!response.isSuccessful) {
                    android.util.Log.e("GroqDebug", "Error Body: $body")
                    return@withContext Result.failure(Exception("HTTP Error: ${response.code}\nBody: $body"))
                }

                val jsonResponse = JSONObject(body)
                val content = jsonResponse.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .optString("content", "")

                Result.success(content)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun generateTextStream(prompt: String, isJsonMode: Boolean): Flow<String> = flow {
        generateText(prompt, isJsonMode).onSuccess {
            emit(it)
        }.onFailure {
            throw it
        }
    }
}
