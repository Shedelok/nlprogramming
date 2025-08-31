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
        val cleanResponse = response.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        val json = JSONObject(cleanResponse)

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
You are an ambiguity detection system for free-form prompts that are later translated to a function in Kotlin. Analyze the given prompt and determine how clear it is what the function is expected to do.

You will be given a prompt that describes behavior of a kotlin function with the only parameter <parameter>args: Map<String, Any></parameter>.

This prompt will later be passed to an LLM to generate the code for a function in Kotlin that does what the prompt asks to.

Your main goal is to assess how clear expected behavior is described. This should be expressed as the clarity score equal to the probability percentage of the function written by this prompt behaving as was expected by the prompt author.

Treat the potential function as a black box, so only the externally observable behavior, such as returned value or thrown exceptions in different cases, etc., matters.

Only decrease the clarity score and mention issues when they impact what the function returns, or throws or how it changes it arguments.

Consider these CLEAR prompts that should have clarity score 90+:
- "add args[\"a\"] and args[\"b\"]"
- "return true if args[\"num\"] is odd and false otherwise"
- "count palindromes in Array<String> args[\"arr\"]"
- "sort args[\"numbers\"] in ascending order"
- "replace all elements in Array<String> args[\"arr\"] with empty strings"

Look for these major issues that should lower confidence:
- Vague operations without clear meaning ("process", "handle", "manage", "do something")
- No clear input/output specification

Provide your analysis as a JSON object with this exact structure:
{
    "summary": "<brief explanation of your assessment>",
    "clarity_score": <integer from 0 to 100 equal to the probability the description will match the function generated from the same prompt>,
    "issues": [<array of specific examples of ambiguity in desired behavior found>],
    "suggestions": [<array of specific suggestions to fix ambiguity>]
}

ONLY return the JSON object, no comments needed.

Example analyses:
<example1>
Input: "find the maximum value in args[\"numbers\"]"
Output:
{
    "summary": "The prompt is clear about data access and operation with no ambiguity.",
    "clarity_score": 95,
    "issues": [],
    "suggestions": []
}
</example1>
<example2>
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
        "Clarify what \"data\" refers to",
        "Clarify the type or structure of the data"
    ]
}
</example2>
<example3>
Input: "reverse the string args[\"text\"]"
Output:
{
    "summary": "The prompt clearly specifies the operation and data access with no ambiguity.",
    "clarity_score": 100,
    "issues": [],
    "suggestions": []
}
</example3>
"""