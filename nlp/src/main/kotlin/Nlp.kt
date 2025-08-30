package sharepa.nlp

import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

class NlpCompilationException(message: String, cause: Throwable? = null) : Exception(message, cause)

fun nlp(input: String): (Map<String, Any>) -> Any? {
    val translator = EnglishToKotlinTranslator()
    val kotlinCode = translator.translateToKotlin(input)

    return compileKotlinScript(kotlinCode)
}

private fun compileKotlinScript(code: String): (Map<String, Any>) -> Any? {
    val scriptHost = BasicJvmScriptingHost()

    // Wrap the user code in a function that takes arguments
    val wrappedScript = buildString {
        appendLine("// User code wrapped in a function")
        appendLine("fun executeUserCode(args: Map<String, Any>): Any? {")
        appendLine("    return run {")
        // Indent the user code
        code.lines().forEach { line ->
            appendLine("        $line")
        }
        appendLine("    }")
        appendLine("}")
        appendLine()
        appendLine("// Return the function reference")
        appendLine("::executeUserCode")
    }

    val scriptSource = wrappedScript.toScriptSource()
    val compilationConfiguration = ScriptCompilationConfiguration {
        jvm {
            dependenciesFromCurrentContext(wholeClasspath = true)
        }
    }

    val evaluationConfiguration = ScriptEvaluationConfiguration {}

    val result = scriptHost.eval(scriptSource, compilationConfiguration, evaluationConfiguration)

    return when (result) {
        is ResultWithDiagnostics.Success -> {
            val returnValue = result.value.returnValue
            when (returnValue) {
                is ResultValue.Value -> {
                    try {
                        @Suppress("UNCHECKED_CAST")
                        returnValue.value as (Map<String, Any>) -> Any?
                    } catch (e: ClassCastException) {
                        throw NlpCompilationException("Script did not return a function reference: ${e.message}", e)
                    }
                }

                else -> throw NlpCompilationException("Script evaluation did not return expected function")
            }
        }

        is ResultWithDiagnostics.Failure -> {
            val errorMessage = result.reports.joinToString("\n") { "${it.severity}: ${it.message}" }
            throw NlpCompilationException(errorMessage)
        }
    }
}

