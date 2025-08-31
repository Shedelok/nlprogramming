package sharepa.nlprogramming.ambiguity

internal interface AmbiguityDetector {
    fun detectAmbiguity(naturalLanguageText: String): AmbiguityResult
}