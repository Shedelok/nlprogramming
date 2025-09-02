package sharepa.nlprogramming.llm

import java.io.Closeable

internal interface LLMClient : Closeable {
    fun generateText(systemPrompt: String, userMessage: String): String

    /**
     * Info about the model(s) and parameters used by this client
     */
    fun describeModel(): String

    override fun close() {
        // Default empty implementation - most clients don't need cleanup
    }
}