package sharepa.nlprogramming.llm

import org.json.JSONObject
import java.io.Closeable

internal interface LLMClient : Closeable {
    fun generateText(systemPrompt: String, userMessage: String): String

    /**
     * Make sure to require JSON in systemPrompt and/or userMessage as well.
     */
    fun generateJson(systemPrompt: String, userMessage: String): JSONObject

    /**
     * Info about the model(s) and parameters used by this client
     */
    fun describeModel(): String

    override fun close() {
        // Default empty implementation - most clients don't need cleanup
    }
}