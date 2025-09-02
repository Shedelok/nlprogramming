package sharepa.nlprogramming.llm

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

private const val URL = "https://api.anthropic.com/v1/messages"
private const val MODEL = "claude-sonnet-4-20250514"
private const val ANTHROPIC_VERSION = "2023-06-01"
private const val TEMPERATURE = 0
private const val MAX_TOKENS = 4000

internal class AnthropicLLMClient(
    private val apiKey: String
) : LLMClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    override fun generateText(systemPrompt: String, userMessage: String): String {
        val requestBody = createRequestBody(systemPrompt, userMessage)

        val request = Request.Builder()
            .url(URL)
            .header("x-api-key", apiKey)
            .header("anthropic-version", ANTHROPIC_VERSION)
            .header("Content-Type", "application/json")
            .post(requestBody)
            .build()

        return client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string()
                ?: throw Exception("Empty response body")

            if (!response.isSuccessful) {
                throw Exception("API call failed: ${response.code} - $responseBody")
            }

            extractResponse(responseBody)
        }
    }

    private fun createRequestBody(systemPrompt: String, userMessage: String): RequestBody {
        val messages = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "user")
                put("content", userMessage)
            })
        }

        val jsonBody = JSONObject().apply {
            put("model", MODEL)
            put("max_tokens", MAX_TOKENS)
            put("temperature", TEMPERATURE)
            put("system", systemPrompt)
            put("messages", messages)
        }

        return jsonBody.toString().toRequestBody("application/json".toMediaType())
    }

    override fun describeModel(): String {
        return "$URL, model $MODEL, version: $ANTHROPIC_VERSION, temperature $TEMPERATURE, max_tokens $MAX_TOKENS"
    }

    private fun extractResponse(responseJson: String): String {
        val json = JSONObject(responseJson)
        val content = json.getJSONArray("content")
        val firstContent = content.getJSONObject(0)
        return firstContent.getString("text")
    }
}