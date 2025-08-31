package sharepa.nlprogramming

import sharepa.nlprogramming.ambiguity.AmbiguityResult

class NlProgrammingExecutionException(val generatedCode: String, cause: Throwable? = null) :
    Exception("Error executing users's function.", cause)

open class NlProgrammingCompilationException(message: String, cause: Throwable? = null) : Exception(message, cause)

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

