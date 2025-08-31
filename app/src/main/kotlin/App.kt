package sharepa.app

import sharepa.nlprogramming.NLProgramming

fun main() {
    val nlp = NLProgramming()

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
