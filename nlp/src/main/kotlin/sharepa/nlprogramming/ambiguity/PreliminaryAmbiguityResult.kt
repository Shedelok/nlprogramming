package sharepa.nlprogramming.ambiguity

@ConsistentCopyVisibility
data class PreliminaryAmbiguityResult internal constructor(
    /**
     * Clarity score from 0 to 100, where higher values indicate clearer prompts.
     */
    val clarityScore: Int,
    val summary: String,
    val issues: List<String> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val clarityThreshold: Int
) {
    val isAmbiguous = clarityScore < clarityThreshold
}

class PreliminaryAmbiguityResultFactory(private val clarityThreshold: Int) {
    fun create(
        clarityScore: Int,
        summary: String,
        issues: List<String> = emptyList(),
        suggestions: List<String> = emptyList()
    ): PreliminaryAmbiguityResult {
        return PreliminaryAmbiguityResult(
            clarityScore = clarityScore,
            summary = summary,
            issues = issues,
            suggestions = suggestions,
            clarityThreshold = clarityThreshold
        )
    }
}