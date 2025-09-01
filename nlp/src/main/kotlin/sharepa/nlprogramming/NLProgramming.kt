package sharepa.nlprogramming

import sharepa.nlprogramming.translator.NLToKotlinScriptTranslator
import sharepa.nlprogramming.translator.LLMNLToKotlinScriptTranslator
import sharepa.nlprogramming.compiler.BasicJvmKotlinScriptCompiler
import sharepa.nlprogramming.ambiguity.AmbiguityDetector
import sharepa.nlprogramming.ambiguity.LLMAmbiguityDetector
import sharepa.nlprogramming.ambiguity.ImplementationConfidenceChecker
import sharepa.nlprogramming.ambiguity.LLMImplementationConfidenceChecker
import sharepa.nlprogramming.compiler.KotlinScriptCompiler
import sharepa.nlprogramming.llm.GroqLLMClient
import sharepa.nlprogramming.llm.CachingLLMClient

class NLProgramming(
    clarityThresholdForAmbiguityDetection: Int = 90,
    cacheSizeLimitKB: Long? = null, // how much disk space this class is allowed to use (during and between runtimes), null = no cache
    cacheTtlHours: Long = 7 * 24
) {
    private val compiler: KotlinScriptCompiler = BasicJvmKotlinScriptCompiler()

    private val translator: NLToKotlinScriptTranslator
    private val ambiguityDetector: AmbiguityDetector
    private val implementationConfidenceChecker: ImplementationConfidenceChecker

    init {
        val llmClient = GroqLLMClient().let { baseClient ->
            if (cacheSizeLimitKB != null) {
                CachingLLMClient(baseClient, cacheSizeLimitKB, cacheTtlHours)
            } else {
                baseClient
            }
        }
        translator = LLMNLToKotlinScriptTranslator(llmClient)
        ambiguityDetector = LLMAmbiguityDetector(llmClient, clarityThresholdForAmbiguityDetection)
        implementationConfidenceChecker = LLMImplementationConfidenceChecker(llmClient, clarityThresholdForAmbiguityDetection)
    }

    fun compileAndCall(input: String, vararg args: Pair<String, Any>): Any? {
        val ambiguityResult = ambiguityDetector.detectAmbiguity(input)
        if (ambiguityResult.isAmbiguous) {
            throw NlProgrammingAmbiguityException(ambiguityResult)
        }

        val translatedFunExpr = try {
            translator.translateToKotlinScriptFunctionExpression(input)
        } catch (e: Exception) {
            throw NlProgrammingCompilationException(
                "Error translating natural language expression to a Kotlin script.",
                e
            )
        }

        if (!implementationConfidenceChecker.isImplementationAcceptable(input, translatedFunExpr)) {
            throw NlProgrammingImplementationMismatchException(translatedFunExpr)
        }

        @Suppress("UNCHECKED_CAST")
        val compiledFunExpr = try {
            compiler.compileToValue(translatedFunExpr) as (Map<String, Any?>) -> Any?
        } catch (e: Exception) {
            throw NlProgrammingCompilationException(
                "Error translating natural language expression to a Kotlin script.",
                e
            )
        }

        return try {
            compiledFunExpr(args.toMap())
        } catch (e: Exception) {
            throw NlProgrammingExecutionException(translatedFunExpr, e)
        }
    }
}
