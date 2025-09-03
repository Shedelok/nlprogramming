package sharepa.nlprogramming.llm

import org.json.JSONObject

internal class ThrottlingLLMClient(
    private val delegate: LLMClient,
    private val sleepBeforeEachCallMillis: Long
) : LLMClient {

    override fun generateText(systemPrompt: String, userMessage: String): String {
        Thread.sleep(sleepBeforeEachCallMillis)
        return delegate.generateText(systemPrompt, userMessage)
    }

    override fun generateJson(systemPrompt: String, userMessage: String): JSONObject {
        Thread.sleep(sleepBeforeEachCallMillis)
        return delegate.generateJson(systemPrompt, userMessage)
    }

    override fun describeModel(): String {
        return "Throttled(${sleepBeforeEachCallMillis}ms) delegate=(${delegate.describeModel()})"
    }

    override fun close() {
        delegate.close()
    }
}