package sharepa.nlprogramming.llm

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

internal class GroqLLMClient : LLMClient {
    private val client = OkHttpClient()
    private val apiKey = System.getenv("GROQ_API_KEY")
        ?: throw IllegalStateException("GROQ_API_KEY environment variable is required")

    override fun generateText(systemPrompt: String, userMessage: String): String {
        val requestBody = createRequestBody(systemPrompt, userMessage)

        val request = Request.Builder()
            .url("https://api.groq.com/openai/v1/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .post(requestBody)
            .build()

        Thread.sleep(1100) // to avoid hitting rate limit
        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("API call failed: ${response.code}")
            }

            val responseBody = response.body?.string()
                ?: throw Exception("Empty response body")

            extractResponse(responseBody)
        }
    }

    private fun createRequestBody(systemPrompt: String, userMessage: String): RequestBody {
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
            put("model", "llama-3.1-8b-instant")
            put("messages", messages)
            put("temperature", 0)
            put("max_tokens", 4000)
        }

        return jsonBody.toString().toRequestBody("application/json".toMediaType())
    }

    private fun extractResponse(responseJson: String): String {
        val json = JSONObject(responseJson)
        val choices = json.getJSONArray("choices")
        val firstChoice = choices.getJSONObject(0)
        val message = firstChoice.getJSONObject("message")
        return message.getString("content")
    }
}