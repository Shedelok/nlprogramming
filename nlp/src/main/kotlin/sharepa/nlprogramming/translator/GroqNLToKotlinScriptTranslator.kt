package sharepa.nlprogramming.translator

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

internal class GroqNLToKotlinScriptTranslator : NLToKotlinScriptTranslator {
    private val client = OkHttpClient()
    private val apiKey = System.getenv("GROQ_API_KEY")
        ?: throw IllegalStateException("GROQ_API_KEY environment variable is required")

    override fun translateToKotlinScriptFunctionExpression(naturalLanguageText: String): String {
        val requestBody = createRequestBody(naturalLanguageText)

        val request = Request.Builder()
            .url("https://api.groq.com/openai/v1/chat/completions")
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

            extractKotlinCode(responseBody)
        }
    }

    private fun createRequestBody(naturalLanguageText: String): RequestBody {
        val systemPrompt = $$"""
            You are a Kotlin script expression generator. Convert the following natural language description to a complete Kotlin script expression.

            Rules:
            1. Return ONLY a function expression wrapped in parentheses: (fun(args: Map<String, Any>): Any? { ... })
            2. Access input values using args["key_name"] syntax inside the function
            3. Use appropriate type casting when accessing args values
            4. No explanations, comments, or markdown formatting
            5. The expression should evaluate to a function value that can be called

            Example:
            Input: "return sum and average of args[\"a\"] and args[\"b\"]"
            Output:
            (fun(args: Map<String, Any>): Any? {
                val a = args["a"] as Int
                val b = args["b"] as Int
                val sum = a + b
                val average = sum.toDouble() / 2
                return "Sum: $sum, Average: $average"
            })
        """.trimIndent()

        val messages = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "system")
                put("content", systemPrompt)
            })
            put(JSONObject().apply {
                put("role", "user")
                put("content", naturalLanguageText)
            })
        }

        val jsonBody = JSONObject().apply {
            put("model", "llama-3.3-70b-versatile")
            put("messages", messages)
            put("temperature", 0)
            put("max_tokens", 4000)
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