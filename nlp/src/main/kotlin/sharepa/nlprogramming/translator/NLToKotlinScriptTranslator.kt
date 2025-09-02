package sharepa.nlprogramming.translator

internal interface NLToKotlinScriptTranslator {
    /**
     * Translates free-form text into a Kotlin script function expression.
     * Is expected to return a function expression that can be compiled and then invoked as it is.
     * The returned function is expected to have signature: (Map<String, Any>) -> Any?
     */
    fun translateToKotlinScriptFunctionExpression(naturalLanguageText: String): String
}