package sharepa.nlprogramming

import sharepa.nlprogramming.translator.NLToKotlinScriptTranslator
import sharepa.nlprogramming.translator.GroqNLToKotlinScriptTranslator
import sharepa.nlprogramming.compiler.KotlinScriptCompiler
import sharepa.nlprogramming.compiler.BasicJvmKotlinScriptCompiler

internal class NlProgrammingCompilationException(message: String, cause: Throwable? = null) : Exception(message, cause)

class NLProgramming {
    private val translator: NLToKotlinScriptTranslator = GroqNLToKotlinScriptTranslator()
    private val compiler: KotlinScriptCompiler = BasicJvmKotlinScriptCompiler()

    fun implementAndRunFun(input: String, vararg args: Pair<String, Any>): Any? {
        @Suppress("UNCHECKED_CAST")
        val compiledFunExpr = try {
            val translatedFunExpr = translator.translateToKotlinScriptFunctionExpression(input)
            compiler.compileToValue(translatedFunExpr) as (Map<String, Any?>) -> Any?
        } catch (e: Exception) {
            throw NlProgrammingCompilationException(
                "Error translating natural language expression to a Kotlin script.",
                e
            )
        }

        return compiledFunExpr(args.toMap())
    }
}
