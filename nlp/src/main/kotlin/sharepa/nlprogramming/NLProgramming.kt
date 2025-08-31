package sharepa.nlprogramming

import sharepa.nlprogramming.translator.NLToKotlinScriptTranslator
import sharepa.nlprogramming.translator.LLMNLToKotlinScriptTranslator
import sharepa.nlprogramming.compiler.BasicJvmKotlinScriptCompiler
import sharepa.nlprogramming.ambiguity.AmbiguityDetector
import sharepa.nlprogramming.ambiguity.LLMAmbiguityDetector
import sharepa.nlprogramming.ambiguity.ImplementationConfidenceChecker
import sharepa.nlprogramming.ambiguity.LLMImplementationConfidenceChecker
import sharepa.nlprogramming.llm.GroqLLMClient

class NLProgramming(clarityThresholdForAmbiguityDetection: Int = 90) {
    private val compiler = BasicJvmKotlinScriptCompiler()

    private val translator: NLToKotlinScriptTranslator
    private val ambiguityDetector: AmbiguityDetector
    private val implementationConfidenceChecker: ImplementationConfidenceChecker

    init {
        val llmClient = GroqLLMClient()
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
