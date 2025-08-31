package sharepa.nlprogramming.ambiguity

import sharepa.nlprogramming.llm.LLMClient
import org.json.JSONObject

/**
 * LLM-based ambiguity detector that uses confidence scoring and issue identification.
 */
internal class LLMAmbiguityDetector(
    private val llmClient: LLMClient,
    clarityThreshold: Int
) : AmbiguityDetector {
    private val resultFactory = AmbiguityResultFactory(clarityThreshold)
    override fun detectAmbiguity(naturalLanguageText: String): AmbiguityResult {
        val response = llmClient.generateText(AMBIGUITY_DETECTION_PROMPT, naturalLanguageText)
        return parseAmbiguityResponse(response)
    }

    private fun parseAmbiguityResponse(response: String): AmbiguityResult {
        val json = JSONObject(response.trim())

        val clarityScore = json.getInt("clarity_score")
        val summary = json.getString("summary")
        val issues = json.getJSONArray("issues").run { (0 until length()).map(this::getString) }
        val suggestions = json.getJSONArray("suggestions").run { (0 until length()).map(this::getString) }

        return resultFactory.create(
            clarityScore = clarityScore,
            summary = summary,
            issues = issues,
            suggestions = suggestions
        )
    }
}

private const val AMBIGUITY_DETECTION_PROMPT = """
You are an ambiguity detection system for natural language programming prompts that are later translated to Kotlin. Analyze the given prompt and determine how clearly it specifies what code should be generated.

Be strict about ambiguity detection - flag prompts that lack details that would lead to different implementations by different developers.

Your main goal is to assess the probability of this prompt resulting in code that behaves differently if given to different developers to implement.

Provide your analysis as a JSON object with this exact structure:
{
    "summary": "<brief explanation of your assessment>",
    "clarity_score": <integer 0-100>,
    "issues": [<array of specific ambiguity issues found>],
    "suggestions": [<array of specific suggestions to improve clarity>]
}

Clarity scoring guidelines:
- 90-100: Very clear, specific, unambiguous (e.g., <example>add args["a"] and args["b"]</example>)
- 70-89: Mostly clear with minor ambiguities (e.g., <example>multiply args["x"] by 2</example>)
- 50-69: Somewhat unclear, multiple interpretations possible (e.g., <example>calculate the result</example>)
- 30-49: Quite ambiguous, significant clarification needed (e.g., <example>process the data</example>)
- 0-29: Very ambiguous or incomprehensible (e.g., <example>do something</example>)

Consider these CLEAR prompts that should score 80+:
- <example>add args["a"] and args["b"]</example>
- <example>return true if args["num"] is odd and false otherwise</example>
- <example>count palindromes in Array<String> args["arr"]</example>
- <example>sort args["numbers"] in ascending order</example>

Look for these major issues that should lower confidence:
- Vague operations without clear meaning ("process", "handle", "manage", "do something")
- No clear input/output specification
- Multiple drastically different interpretations possible
- Complete lack of context
- Missing sort order for sorting operations (ascending/descending)
- Missing comparison criteria or algorithms
- Ambiguous default behaviors

Example analyses:

Input: "find the maximum value in args[\"numbers\"]"
Output:
{
    "summary": "The prompt is clear about data access and operation with no ambiguity.",
    "clarity_score": 92,
    "issues": [],
    "suggestions": []
}

Input: "calculate the average of the data"
Output:
{
    "summary": "The prompt lacks essential details about data access and type specification.",
    "clarity_score": 25,
    "issues": [
        "No specification of how to access the data from function arguments",
        "Unclear what 'the data' refers to - which argument contains it?",
        "No specification of data type or structure"
    ],
    "suggestions": [
        "Specify 'calculate the average of args[\"values\"]'",
        "Clarify the data type (e.g., 'calculate the average of the numbers in args[\"dataset\"]')"
    ]
}

Input: "reverse the string args[\"text\"]"
Output:
{
    "summary": "The prompt clearly specifies the operation and data access with no ambiguity.",
    "clarity_score": 95,
    "issues": [],
    "suggestions": []
}
"""