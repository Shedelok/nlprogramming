package sharepa.nlprogramming.translator

internal interface NLToKotlinScriptTranslator {
    /**
     * Translates natural language text into a Kotlin script function expression.
     * Is expected to return a function expression that can be compiled and called as it is.
     * The returned function is expected to have signature: (Map<String, Any>) -> Any?
     */
    fun translateToKotlinScriptFunctionExpression(naturalLanguageText: String): String
}