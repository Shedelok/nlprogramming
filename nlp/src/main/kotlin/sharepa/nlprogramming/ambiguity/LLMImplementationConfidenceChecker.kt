package sharepa.nlprogramming.ambiguity

import sharepa.nlprogramming.llm.LLMClient

internal class LLMImplementationConfidenceChecker(
    private val llmClient: LLMClient,
    private val confidenceThreshold: Int
) : ImplementationConfidenceChecker {

    override fun isImplementationAcceptable(naturalLanguagePrompt: String, generatedCode: String): Boolean {
        val response = llmClient.generateText(
            IMPLEMENTATION_CONFIDENCE_PROMPT,
            """
              <naturalLanguagePrompt>
              $naturalLanguagePrompt
              </naturalLanguagePrompt>
              <generatedCode>
              $generatedCode
              </generatedCode>
            """.trimIndent()
        )

        val confidence = parseConfidenceResponse(response)
        return confidence >= confidenceThreshold
    }

    private fun parseConfidenceResponse(response: String): Int {
        return response.trim().toInt()
    }
}

private const val IMPLEMENTATION_CONFIDENCE_PROMPT = """
You are checking if generated Kotlin code matches the prompt that was used for its generation.

You will receive:
1. A prompt describing a function behaviour
2. Code of a function in Kotlin that should match the prompt

Note, that the code is always a function with a single parameter: <parameter>args: Map<String, Any></parameter> and should access all arguments through this map.

Return ONLY a number from 0-100 representing how well the code's BEHAVIOR matches what the prompt asked for. The goal is to detect situations in which we need to ask user for more clarifications about their prompt, because the prompt can be interpreted differently to what the generated code does.

ONLY evaluate behavioral correctness:
- Does the code do what the prompt asked?
- Are inputs, outputs, and operations correct?
- Does it produce the expected results?

IGNORE implementation details like:
- Code style or variable naming
- Specific algorithms used (quicksort vs mergesort - both are fine for "sort")
- Performance optimizations
- Memory usage patterns

Examples:
<example1>
Prompt: "return sum of int args['a'] and int args['b']"
Code: (fun(args: Map<String, Any>): Any? { val a = args["a"] as Int; val b = args["b"] as Int; return a + b })
Answer: 100
</example1>
<example2>
Prompt: "sort array ascending"
Code: (fun(args: Map<String, Any>): Any? { val arr = args["array"] as IntArray; arr.sort(); return arr })
Answer: 60
</example2>
<example3>
Prompt: "calculate factorial of args['n']"
Code: (fun(args: Map<String, Any>): Any? { val str = args["n"] as String; return str.reversed() })
Answer: 0
</example3>

Return ONLY the number, nothing else.
"""