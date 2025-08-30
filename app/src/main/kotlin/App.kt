package sharepa.app

import sharepa.nlp.nlp

fun main() {
    println("=== NLProgramming Demo ===\n")

    println("English to Kotlin Translation:")
    val englishInput = """return sum and average of args["a"] and args["b"]"""

    try {
        val mathFunction = nlp(englishInput)
        val result = mathFunction(mapOf("a" to 5, "b" to 10))
        println("   Input: $englishInput")
        println("   Result: $result")
    } catch (e: Exception) {
        println("   Error: ${e.message}")
    }
}
