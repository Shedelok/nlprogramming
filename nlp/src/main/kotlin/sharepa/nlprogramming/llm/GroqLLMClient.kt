package sharepa.nlprogramming.llm

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

private const val URL = "https://api.groq.com/openai/v1/chat/completions"
private const val MODEL = "llama-3.3-70b-versatile"
private const val TEMPERATURE = 0
private const val MAX_TOKENS = 4000

internal class GroqLLMClient(private val apiKey: String) : LLMClient {
    private val client = OkHttpClient()

    override fun generateText(systemPrompt: String, userMessage: String): String {
        return callGroqApi(systemPrompt, userMessage, requireJson = false)
    }

    override fun generateJson(systemPrompt: String, userMessage: String): JSONObject {
        return parseJsonFromLLMResponse(callGroqApi(systemPrompt, userMessage, requireJson = true))
    }

    private fun callGroqApi(systemPrompt: String, userMessage: String, requireJson: Boolean): String {
        val requestBody = createRequestBody(systemPrompt, userMessage, requireJson)

        val request = Request.Builder()
            .url(URL)
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .post(requestBody)
            .build()

        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("API call failed: ${response.code}")
            }

            val responseBody = response.body?.string()
                ?: throw Exception("Empty response body")

            JSONObject(responseBody)
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
        }
    }

    private fun createRequestBody(systemPrompt: String, userMessage: String, requireJson: Boolean): RequestBody {
        val messages = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "system")
                put("content", systemPrompt)
            })
            put(JSONObject().apply {
                put("role", "user")
                put("content", userMessage)
            })
        }

        val jsonBody = JSONObject().apply {
            put("model", MODEL)
            put("messages", messages)
            put("temperature", TEMPERATURE)
            put("max_tokens", MAX_TOKENS)

            if (requireJson) {
                put("response_format", JSONObject().apply {
                    put("type", "json_object")
                })
            }
        }

        return jsonBody.toString().toRequestBody("application/json".toMediaType())
    }

    override fun describeModel(): String {
        return "$URL, model $MODEL, temperature $TEMPERATURE, max_tokens $MAX_TOKENS"
    }
}