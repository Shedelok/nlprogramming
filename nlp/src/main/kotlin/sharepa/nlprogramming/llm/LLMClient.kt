package sharepa.nlprogramming.llm

internal interface LLMClient {
    fun generateText(systemPrompt: String, userMessage: String): String

    /**
     * Info about the model(s) and parameters used by this client
     */
    fun describeModel(): String
}