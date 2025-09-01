package sharepa.app

import sharepa.nlprogramming.NLProgramming

fun main() {
    val apiKey = System.getenv("GROQ_API_KEY")
        ?: throw IllegalStateException("GROQ_API_KEY environment variable is required")

    val nlp = NLProgramming(apiKey, cacheSizeLimitKB = 1 * 1000)

    try {
        val result = nlp.compileAndCall(
            """calculate number of palindrome strings in array args["arr"] (not list)""",
            "arr" to arrayOf("A", "b", "ba", "aba")
        )
        println("Result: $result")
    } catch (e: Exception) {
        println("Error: ${e.message}")
    }
}
