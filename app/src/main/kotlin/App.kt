package sharepa.app

import sharepa.nlprogramming.NLProgramming

fun main() {
    val apiKey = System.getenv("LLM_API_KEY")
        ?: throw IllegalStateException("LLM_API_KEY environment variable is required")

    val nlp = NLProgramming(apiKey, cacheSizeLimitKB = 1 * 1000, sleepBeforeEachLlmCallMillis = 5000)

    try {
        val result = nlp.translateAndCompile(
            """calculate number of palindrome strings in array args["arr"] (not list)"""
        )(mapOf("arr" to arrayOf("A", "b", "ba", "aba")))
        println("Result: $result")
    } catch (e: Exception) {
        println("Error: ${e.message}")
    }
}
