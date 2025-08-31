package sharepa.nlprogramming.ambiguity

@ConsistentCopyVisibility
data class AmbiguityResult internal constructor(
    /**
     * Clarity score from 0 to 100, where higher values indicate clearer prompts.
     */
    val clarityScore: Int,
    val summary: String,
    val issues: List<String> = emptyList(),
    val suggestions: List<String> = emptyList(),
    private val clarityThreshold: Int
) {
    val isAmbiguous = clarityScore < clarityThreshold
}

class AmbiguityResultFactory(private val clarityThreshold: Int) {
    fun create(
        clarityScore: Int,
        summary: String,
        issues: List<String> = emptyList(),
        suggestions: List<String> = emptyList()
    ): AmbiguityResult {
        return AmbiguityResult(
            clarityScore = clarityScore,
            summary = summary,
            issues = issues,
            suggestions = suggestions,
            clarityThreshold = clarityThreshold
        )
    }
}