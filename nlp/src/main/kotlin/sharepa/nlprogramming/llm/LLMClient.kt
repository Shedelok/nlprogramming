package sharepa.nlprogramming.llm

internal interface LLMClient {
    fun generateText(systemPrompt: String, userMessage: String): String
}