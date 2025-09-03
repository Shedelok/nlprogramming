package sharepa.nlprogramming.ambiguity

import org.json.JSONObject

internal fun parseJsonFromLLMResponse(response: String): JSONObject {
    return JSONObject(response.trim()
        .removePrefix("```json")
        .removePrefix("```")
        .removeSuffix("```")
        .trim())
}