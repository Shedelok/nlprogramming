package sharepa.app

import sharepa.nlprogramming.NLProgramming

fun main() {
    val apiKey = System.getenv("LLM_API_KEY")
        ?: throw IllegalStateException("LLM_API_KEY environment variable is required")

    NLProgramming(apiKey, cacheSizeLimitKB = 50 * 1000).use { nlp ->
        val calcPalindromes = nlp.translateAndCompile(
            """
             calculate number of palindrome strings in list args["arr"].
             if the given list is empty, throw IllegalArgumentException
            """
        )

        println(calcPalindromes(mapOf("arr" to listOf("ana", "bob")))) // prints 2
        println(calcPalindromes(mapOf("arr" to listOf("xy", "madam")))) // prints 1
        println(calcPalindromes(mapOf("arr" to emptyList<String>()))) // throws IllegalArgumentException
    }
}
