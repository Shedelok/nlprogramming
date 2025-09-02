package sharepa.nlprogramming

import sharepa.nlprogramming.translator.NLToKotlinScriptTranslator
import sharepa.nlprogramming.translator.LLMNLToKotlinScriptTranslator
import sharepa.nlprogramming.compiler.BasicJvmKotlinScriptCompiler
import sharepa.nlprogramming.ambiguity.PreliminaryAmbiguityDetector
import sharepa.nlprogramming.ambiguity.LLMPreliminaryAmbiguityDetector
import sharepa.nlprogramming.ambiguity.ImplementationConfidenceChecker
import sharepa.nlprogramming.ambiguity.LLMImplementationConfidenceChecker
import sharepa.nlprogramming.compiler.KotlinScriptCompiler
import sharepa.nlprogramming.llm.GroqLLMClient
import sharepa.nlprogramming.llm.AnthropicLLMClient
import sharepa.nlprogramming.llm.CachingLLMClient
import sharepa.nlprogramming.llm.LLMClient
import sharepa.nlprogramming.llm.ThrottlingLLMClient
import java.io.Closeable

/**
 * Natural language programming library that translates human-readable instructions into executable Kotlin code.
 *
 * Converts natural language prompts into compiled functions using LLM translation and ambiguity detection.
 * Supports caching, throttling, and validation to ensure reliable code generation.
 *
 * Usage:
 * ```kotlin
 * NLProgramming(llmApiKey, cacheSizeLimitKB = 50 * 1000).use { nlp ->
 *     val fn = nlp.translateAndCompile("calculate sum of numbers in Array args[\"numbers\"]")
 *     val result = fn(mapOf("numbers" to arrayOf(1, 2, 3, 4, 5)))
 * }
 * ```
 */
class NLProgramming(
    llmApiKey: String,
    clarityThresholdForAmbiguityDetection: Int = 80,
    cacheSizeLimitKB: Long? = null, // how much disk space this class is allowed to use (during and between runtimes), null = no cache
    cacheTtlHours: Long = 7 * 24,
    sleepBeforeEachLlmCallMillis: Long? = null // how much to sleep before each LLM call, null = no sleep
) : Closeable {
    private val compiler: KotlinScriptCompiler = BasicJvmKotlinScriptCompiler()

    private val llmClient: LLMClient = when {
        llmApiKey.startsWith("gsk_") -> GroqLLMClient(llmApiKey)
        llmApiKey.startsWith("sk-ant") -> AnthropicLLMClient(llmApiKey)
        else -> throw IllegalArgumentException(
            "Unsupported API key format. Supported prefixes: gsk_ (Groq), sk-ant (Anthropic)"
        )
    }.let { client ->
        if (sleepBeforeEachLlmCallMillis != null) {
            ThrottlingLLMClient(client, sleepBeforeEachLlmCallMillis)
        } else {
            client
        }
    }.let { client ->
        if (cacheSizeLimitKB != null) {
            CachingLLMClient(client, cacheSizeLimitKB, cacheTtlHours)
        } else {
            client
        }
    }

    private val translator: NLToKotlinScriptTranslator = LLMNLToKotlinScriptTranslator(llmClient)
    private val preliminaryAmbiguityDetector: PreliminaryAmbiguityDetector =
        LLMPreliminaryAmbiguityDetector(llmClient, clarityThresholdForAmbiguityDetection)
    private val implementationConfidenceChecker: ImplementationConfidenceChecker =
        LLMImplementationConfidenceChecker(llmClient, clarityThresholdForAmbiguityDetection)


    fun translateAndCompile(input: String): Function1<Map<String, Any?>, Any?> {
        return try {
            requireNotAmbiguous(input)

            val translatedFunExpr = translator.translateToKotlinScriptFunctionExpression(input)
            requireImplementationIsAcceptable(input, translatedFunExpr)

            @Suppress("UNCHECKED_CAST")
            try {
                compiler.compileToValue(translatedFunExpr) as (Map<String, Any?>) -> Any?
            } catch (e: Exception) {
                throw NlProgrammingCompilationException(
                    "Error translating prompt to a Kotlin script. Try making your code less ambiguous.",
                    e
                )
            }
        } catch (e: Exception) {
            throw when (e) {
                is NlProgrammingImplementationMismatchException, is NlProgrammingPreliminaryAmbiguityException -> e
                else -> NlProgrammingCompilationException(
                    "Unexpected error translating prompt to a Kotlin script.",
                    e
                )
            }
        }
    }

    private fun requireNotAmbiguous(prompt: String) {
        val preliminaryAmbiguityResult = preliminaryAmbiguityDetector.detectAmbiguity(prompt)

        if (preliminaryAmbiguityResult.isAmbiguous) {
            throw NlProgrammingPreliminaryAmbiguityException(preliminaryAmbiguityResult)
        }
    }

    private fun requireImplementationIsAcceptable(prompt: String, implementation: String) {
        val assessmentResult = implementationConfidenceChecker.assessImplementationAcceptability(prompt, implementation)

        if (!assessmentResult.isAcceptable) {
            throw NlProgrammingImplementationMismatchException(implementation, assessmentResult.issues)
        }
    }

    /**
     * This method has to be closed to save cache to disk
     */
    override fun close() {
        llmClient.close()
    }
}
