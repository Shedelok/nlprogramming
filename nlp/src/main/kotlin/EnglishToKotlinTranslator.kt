package sharepa.nlp

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class EnglishToKotlinTranslator {
    private val client = OkHttpClient()
    private val apiKey = System.getenv("GROQ_API_KEY")
        ?: throw IllegalStateException("GROQ_API_KEY environment variable is required")

    fun translateToKotlin(englishText: String): String {
        val requestBody = createRequestBody(englishText)

        val request = Request.Builder()
            .url("https://api.groq.com/openai/v1/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .post(requestBody)
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw Exception("API call failed: ${response.code}")
                }

                val responseBody = response.body?.string()
                    ?: throw Exception("Empty response body")

                extractKotlinCode(responseBody)
            }
        } catch (e: Exception) {
            throw NlpCompilationException("Failed to translate English to Kotlin: ${e.message}", e)
        }
    }

    private fun createRequestBody(englishText: String): RequestBody {
        val systemPrompt = """
            You are a Kotlin code generator. Convert the following English description to Kotlin code that works with `args: Map<String, Any>`.

            Rules:
            1. Return ONLY executable Kotlin code, no explanations or markdown
            2. Access input values using args["key_name"] syntax
            3. The last line should be the return value (no explicit return keyword needed)
            4. Use appropriate type casting when accessing args values

            Example:
            Input: "return sum and average of args["a"] and args["b"]"
            Output:
            val a = args["a"] as Int
            val b = args["b"] as Int
            val sum = a + b
            val average = sum.toDouble() / 2
            "Sum: " + sum + ", Average: " + average
        """.trimIndent()

        val messages = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "system")
                put("content", systemPrompt)
            })
            put(JSONObject().apply {
                put("role", "user")
                put("content", englishText)
            })
        }

        val jsonBody = JSONObject().apply {
            put("model", "llama-3.3-70b-versatile")
            put("messages", messages)
            put("temperature", 0.1)
            put("max_tokens", 500)
        }

        return jsonBody.toString().toRequestBody("application/json".toMediaType())
    }

    private fun extractKotlinCode(responseJson: String): String {
        val json = JSONObject(responseJson)
        val choices = json.getJSONArray("choices")
        val firstChoice = choices.getJSONObject(0)
        val message = firstChoice.getJSONObject("message")
        val content = message.getString("content")

        // Remove any markdown code blocks if present
        return content
            .replace("```kotlin", "")
            .replace("```", "")
            .trim()
    }
}