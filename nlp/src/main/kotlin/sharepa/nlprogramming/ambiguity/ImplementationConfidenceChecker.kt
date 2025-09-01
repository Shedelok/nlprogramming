package sharepa.nlprogramming.ambiguity

internal interface ImplementationConfidenceChecker {
    fun assessImplementationAcceptability(naturalLanguagePrompt: String, generatedCode: String): ImplementationAcceptabilityResult
}