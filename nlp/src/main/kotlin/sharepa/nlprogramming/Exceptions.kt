package sharepa.nlprogramming

import sharepa.nlprogramming.ambiguity.PreliminaryAmbiguityResult

/**
 * Exception raised during converting user's prompt into an executable code.
 */
open class NlProgrammingCompilationException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Exception raised when the user's prompt needs clarification.
 */
abstract class NlProgrammingAmbiguityException(message: String, cause: Throwable? = null) : NlProgrammingCompilationException(message, cause)

class NlProgrammingPreliminaryAmbiguityException(val preliminaryAmbiguityResult: PreliminaryAmbiguityResult) :
    NlProgrammingAmbiguityException(formatAmbiguityMessage(preliminaryAmbiguityResult))

private fun formatAmbiguityMessage(result: PreliminaryAmbiguityResult): String = buildString {
    appendLine("The provided prompt is too ambiguous:")
    appendLine("Clarity: ${result.clarityScore}%")
    if (result.issues.isNotEmpty()) {
        appendLine("Issues: ${result.issues.joinToString("; ")}")
    }
    if (result.suggestions.isNotEmpty()) {
        appendLine("Suggestions: ${result.suggestions.joinToString("; ")}")
    }
}.trimEnd()

class NlProgrammingImplementationMismatchException(
    val generatedCode: String,
    val issues: List<String> = emptyList()
) : NlProgrammingAmbiguityException(buildImplementationMismatchMessage(generatedCode, issues))

private fun buildImplementationMismatchMessage(code: String, issues: List<String>): String = buildString {
    appendLine("The code generated doesn't match prompt very well. The prompt must be ambiguous. Generated code:")
    appendLine(code)
    if (issues.isNotEmpty()) {
        appendLine("Issues found:")
        issues.forEach { issue -> appendLine("- $issue") }
    }
}.trimEnd()


