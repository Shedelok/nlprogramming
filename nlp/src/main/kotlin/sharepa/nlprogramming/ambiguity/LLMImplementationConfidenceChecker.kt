package sharepa.nlprogramming.ambiguity

import org.json.JSONObject
import sharepa.nlprogramming.llm.LLMClient

internal class LLMImplementationConfidenceChecker(
    private val llmClient: LLMClient,
    confidenceThreshold: Int
) : ImplementationConfidenceChecker {
    private val resultFactory = ImplementationAcceptabilityResultFactory(confidenceThreshold)

    override fun assessImplementationAcceptability(
        naturalLanguagePrompt: String,
        generatedCode: String
    ): ImplementationAcceptabilityResult {
        val response = llmClient.generateJson(
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

        return parseAcceptabilityResponse(response)
    }

    private fun parseAcceptabilityResponse(response: JSONObject): ImplementationAcceptabilityResult {
        val confidence = response.getInt("confidence")
        val issuesArray = response.optJSONArray("issues")
        val issues = issuesArray?.let { array ->
            (0 until array.length()).map { array.getString(it) }
        } ?: emptyList()

        return resultFactory.create(
            confidence = confidence,
            issues = issues
        )
    }
}

private const val IMPLEMENTATION_CONFIDENCE_PROMPT = """
You are checking if generated Kotlin code matches the prompt that was used for its generation.

You will receive:
1. A prompt describing a function behaviour
2. Code of a function in Kotlin that should match the prompt

Note, that the code is always a function with a single parameter: <parameter>args: Map<String, Any></parameter> and should access all arguments through this map.

Evaluate how well the code's BEHAVIOR matches what the prompt asked for. The goal is to detect situations in which we need to ask user for more clarifications about their prompt, because the prompt can be interpreted differently to what the generated code does.

ONLY evaluate behavioral correctness:
- Does the code do what the prompt asked?
- Are inputs, outputs, and operations correct and what users asked for?
- Does it produce the expected results?

IGNORE implementation details like:
- Code style or variable naming
- Specific algorithms used (quicksort vs mergesort - both are fine for "sort")
- Performance optimizations
- Memory usage patterns

Return JSON in this exact format:
{
  "confidence": <number 0-100>,
  "issues": ["issue description 1", "issue description 2", ...]
}

Provide a confidence score from 0-100 representing how well the code's behavior matches what the prompt asked for.
Include specific issues in the "issues" array explaining why the confidence isn't 100%.

Examples:
<example1>
Prompt: "return sum of int args['a'] and int args['b']"
Code: (fun(args: Map<String, Any>): Any? { val a = args["a"] as Int; val b = args["b"] as Int; return a + b })
Answer: {"confidence": 100, "issues": []}
</example1>
<example2>
Prompt: "sort array ascending"
Code: (fun(args: Map<String, Any>): Any? { val arr = args["array"] as IntArray; arr.sort(); return arr })
Answer: {"confidence": 60, "issues": ["Prompt didn't specify how to access the array in the args", "Prompt didn't specify the type of the elements in the array, code assumed Int."]}
</example2>
<example3>
Prompt: "calculate factorial of args['n']"
Code: (fun(args: Map<String, Any>): Any? { val str = args["n"] as String; return str.reversed() })
Answer: {"confidence": 0, "issues": ["Code reverses string instead of calculating factorial", "Wrong input type (String vs Number)"]}
</example3>

Return ONLY the JSON, nothing else.
"""
