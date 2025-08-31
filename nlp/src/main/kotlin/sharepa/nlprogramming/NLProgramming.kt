package sharepa.nlprogramming

import sharepa.nlprogramming.translator.NLToKotlinScriptTranslator
import sharepa.nlprogramming.translator.GroqNLToKotlinScriptTranslator
import sharepa.nlprogramming.compiler.KotlinScriptCompiler
import sharepa.nlprogramming.compiler.BasicJvmKotlinScriptCompiler
import sharepa.nlprogramming.ambiguity.AmbiguityDetector
import sharepa.nlprogramming.ambiguity.LLMAmbiguityDetector
import sharepa.nlprogramming.ambiguity.AmbiguityResultFactory
import sharepa.nlprogramming.llm.GroqLLMClient

class NLProgramming(clarityThresholdForAmbiguityDetection: Int = 80) {
    private val translator: NLToKotlinScriptTranslator = GroqNLToKotlinScriptTranslator()
    private val compiler: KotlinScriptCompiler = BasicJvmKotlinScriptCompiler()

    private val ambiguityResultFactory: AmbiguityResultFactory = AmbiguityResultFactory(clarityThresholdForAmbiguityDetection)
    private val ambiguityDetector: AmbiguityDetector = LLMAmbiguityDetector(GroqLLMClient(), ambiguityResultFactory)

    fun implementAndRunFun(input: String, vararg args: Pair<String, Any>): Any? {
        val ambiguityResult = ambiguityDetector.detectAmbiguity(input)
        if (ambiguityResult.isAmbiguous) {
            throw NlProgrammingAmbiguityException(ambiguityResult)
        }

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
