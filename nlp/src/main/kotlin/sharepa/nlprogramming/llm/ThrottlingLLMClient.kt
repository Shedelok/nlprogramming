package sharepa.nlprogramming.llm

internal class ThrottlingLLMClient(
    private val delegate: LLMClient,
    private val sleepBeforeEachCallMillis: Long
) : LLMClient {

    override fun generateText(systemPrompt: String, userMessage: String): String {
        Thread.sleep(sleepBeforeEachCallMillis)
        return delegate.generateText(systemPrompt, userMessage)
    }

    override fun describeModel(): String {
        return "Throttled(${sleepBeforeEachCallMillis}ms) over delegate=(${delegate.describeModel()})"
    }
}