package sharepa.nlprogramming.ambiguity

internal interface PreliminaryAmbiguityDetector {
    fun detectAmbiguity(naturalLanguageText: String): PreliminaryAmbiguityResult
}