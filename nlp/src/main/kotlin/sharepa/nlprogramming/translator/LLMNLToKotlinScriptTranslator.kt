package sharepa.nlprogramming.translator

import sharepa.nlprogramming.llm.LLMClient

internal class LLMNLToKotlinScriptTranslator(
    private val llmClient: LLMClient
) : NLToKotlinScriptTranslator {

    override fun translateToKotlinScriptFunctionExpression(naturalLanguageText: String): String {
        val rawResponse = llmClient.generateText(KOTLIN_SCRIPT_SYSTEM_PROMPT, naturalLanguageText)

        // Remove any markdown code blocks if present (common across LLM providers)
        return rawResponse
            .replace("```kotlin", "")
            .replace("```", "")
            .trim()
    }
}

private const val KOTLIN_SCRIPT_SYSTEM_PROMPT = """
You are a Kotlin script expression generator. Convert the following description to a complete Kotlin script expression.

Rules:
1. Return the code in a function expression wrapped in parentheses: (fun(args: Map<String, Any>): Any? { ... }). If code requires any imports, add them before the function expression.
2. Access input values using args["key_name"] syntax inside the function
3. Be attentive to the data types mentioned in the users's prompt. Use types mentioned (or more generic types if possible).
4. Use appropriate type casting when accessing args values
5. No explanations, comments, or markdown formatting
6. The expression should evaluate to a function value that can be called

Examples:
<example1>
Input: "return sum and average of args[\"a\"] and args[\"b\"] as a pair"
Output:
(fun(args: Map<String, Any>): Any? {
    val a = args["a"] as Int
    val b = args["b"] as Int
    val sum = a + b
    val average = sum.toDouble() / 2
    return sum to average
})
</example1>
<example2>
Input: "import java.util.concurrent.atomic.AtomicInteger; return AtomicInteger(0)"
Output:
import java.util.concurrent.atomic.AtomicInteger
(fun(args: Map<String, Any>): Any? {
    return AtomicInteger(0)
})
</example2>
"""