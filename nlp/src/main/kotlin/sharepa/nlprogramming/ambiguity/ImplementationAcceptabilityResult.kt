package sharepa.nlprogramming.ambiguity

@ConsistentCopyVisibility
data class ImplementationAcceptabilityResult internal constructor(
    /**
     * Confidence score from 0 to 100, where higher values indicate better code-prompt match.
     */
    val confidence: Int,
    val issues: List<String> = emptyList(),
    val confidenceThreshold: Int
) {
    val isAcceptable = confidence >= confidenceThreshold
}

class ImplementationAcceptabilityResultFactory(private val confidenceThreshold: Int) {
    fun create(
        confidence: Int,
        issues: List<String> = emptyList()
    ): ImplementationAcceptabilityResult {
        return ImplementationAcceptabilityResult(
            confidence = confidence,
            issues = issues,
            confidenceThreshold = confidenceThreshold
        )
    }
}