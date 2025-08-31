package sharepa.nlprogramming

import sharepa.nlprogramming.ambiguity.AmbiguityResult

open class NlProgrammingCompilationException(message: String, cause: Throwable? = null) : Exception(message, cause)

class NlProgrammingAmbiguityException(
    val ambiguityResult: AmbiguityResult,
    message: String = "The provided prompt is too ambiguous. ${formatAmbiguityMessage(ambiguityResult)}"
) : NlProgrammingCompilationException(message) {

    companion object {
        private fun formatAmbiguityMessage(result: AmbiguityResult): String {
            val parts = mutableListOf<String>()

            parts.add("Clarity: ${result.clarityScore}%")

            if (result.issues.isNotEmpty()) {
                parts.add("Issues: ${result.issues.joinToString("; ")}")
            }

            if (result.suggestions.isNotEmpty()) {
                parts.add("Suggestions: ${result.suggestions.joinToString("; ")}")
            }

            return parts.joinToString(". ")
        }
    }
}