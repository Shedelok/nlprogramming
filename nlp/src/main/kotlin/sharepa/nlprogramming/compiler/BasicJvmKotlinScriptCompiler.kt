package sharepa.nlprogramming.compiler

import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

internal class BasicJvmKotlinScriptCompiler : KotlinScriptCompiler {
    override fun compileToValue(code: String): Any? {
        val scriptHost = BasicJvmScriptingHost()
        val scriptSource = code.toScriptSource()
        val compilationConfiguration = ScriptCompilationConfiguration {
            jvm {
                dependenciesFromCurrentContext(wholeClasspath = true)
            }
        }
        val evaluationConfiguration = ScriptEvaluationConfiguration {}

        return when (val evalResult =
            scriptHost.eval(scriptSource, compilationConfiguration, evaluationConfiguration)) {
            is ResultWithDiagnostics.Success -> {
                when (val returnValue = evalResult.value.returnValue) {
                    is ResultValue.Value -> {
                        returnValue.value
                    }

                    else -> throw Exception("Script evaluation did not return expected function")
                }
            }

            is ResultWithDiagnostics.Failure -> {
                val errorMessage = evalResult.reports.joinToString("\n") { "${it.severity}: ${it.message}" }
                throw Exception("Error evaluating script: $errorMessage")
            }
        }
    }
}