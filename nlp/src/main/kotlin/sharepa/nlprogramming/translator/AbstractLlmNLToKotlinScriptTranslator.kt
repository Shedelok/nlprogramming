package sharepa.nlprogramming.translator

import sharepa.nlprogramming.llm.LLMClient


internal abstract class AbstractLlmNLToKotlinScriptTranslator(
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
You are a Kotlin script expression generator. Convert the following natural language description to a complete Kotlin script expression.

Rules:
1. Return ONLY a function expression wrapped in parentheses: (fun(args: Map<String, Any>): Any? { ... })
2. Access input values using args["key_name"] syntax inside the function
3. Use appropriate type casting when accessing args values
4. No explanations, comments, or markdown formatting
5. The expression should evaluate to a function value that can be called

Example:
Input: "return sum and average of args[\"a\"] and args[\"b\"] as a pair"
Output:
(fun(args: Map<String, Any>): Any? {
    val a = args["a"] as Int
    val b = args["b"] as Int
    val sum = a + b
    val average = sum.toDouble() / 2
    return sum to average
})
"""