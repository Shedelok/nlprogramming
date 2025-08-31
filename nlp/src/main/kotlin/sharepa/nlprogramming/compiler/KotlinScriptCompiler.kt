package sharepa.nlprogramming.compiler

internal interface KotlinScriptCompiler {
    /**
     * Compiles a Kotlin script that evaluates to a value.
     */
    fun compileToValue(code: String): Any?
}