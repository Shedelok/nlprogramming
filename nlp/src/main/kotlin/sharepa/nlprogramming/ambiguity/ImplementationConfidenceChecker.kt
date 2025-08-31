package sharepa.nlprogramming.ambiguity

internal interface ImplementationConfidenceChecker {
    /**
     * Returns true if the generated code matches the original prompt well enough,
     * false if the implementation confidence is too low and the prompt needs clarification.
     */
    fun isImplementationAcceptable(naturalLanguagePrompt: String, generatedCode: String): Boolean
}