package sharepa.nlprogramming

import sharepa.nlprogramming.ambiguity.AmbiguityResult

/**
 * Wrapper for exceptions happened when running compiled code on user's input.
 */
class NlProgrammingExecutionException(val generatedCode: String, cause: Throwable? = null) :
    Exception("Error executing users's function.", cause)

/**
 * Exception raised during converting user's prompt into an executable code.
 */
open class NlProgrammingCompilationException(message: String, cause: Throwable? = null) : Exception(message, cause)

class NlProgrammingImplementationMismatchException(
    val generatedCode: String,
    val issues: List<String> = emptyList()
) : NlProgrammingCompilationException(buildImplementationMismatchMessage(generatedCode, issues))

private fun buildImplementationMismatchMessage(code: String, issues: List<String>): String = buildString {
    appendLine("The code generated doesn't match prompt very well. The prompt must be ambiguous. Generated code:")
    appendLine(code)
    if (issues.isNotEmpty()) {
        appendLine("Issues found:")
        issues.forEach { issue -> appendLine("- $issue") }
    }
}.trimEnd()

class NlProgrammingAmbiguityException(val ambiguityResult: AmbiguityResult) :
    NlProgrammingCompilationException(formatAmbiguityMessage(ambiguityResult))

private fun formatAmbiguityMessage(result: AmbiguityResult): String = buildString {
    appendLine("The provided prompt is too ambiguous:")
    appendLine("Clarity: ${result.clarityScore}%")
    if (result.issues.isNotEmpty()) {
        appendLine("Issues: ${result.issues.joinToString("; ")}")
    }
    if (result.suggestions.isNotEmpty()) {
        appendLine("Suggestions: ${result.suggestions.joinToString("; ")}")
    }
}.trimEnd()

