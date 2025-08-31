package sharepa.nlprogramming.compiler

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@Suppress("UNCHECKED_CAST")
class BasicJvmKotlinScriptCompilerTest {
    private val compiler = BasicJvmKotlinScriptCompiler()

    @Test
    fun `successfully compiles function expression`() {
        val code = """(fun(args: Map<String, Any>): Any? {
            val a = args["a"] as Int
            var b = args["b"] as Int
            return a + b
        })"""

        val result = compiler.compileToValue(code)

        assertEquals(5, (result as (Map<String, Any>) -> Any?)(mapOf("a" to 2, "b" to 3)))
    }

    @Test
    fun `successfully compiles simple expressions`() {
        val code = """
            val a = 10
            val b = 5
            a + b
        """.trimIndent()

        val result = compiler.compileToValue(code)

        assertEquals(15, result)
    }

    @Test
    fun `should throw exception for invalid code`() {
        val invalidCode = "this is not valid code"

        val exception = assertThrows<Exception> {
            compiler.compileToValue(invalidCode)
        }

        assertNotNull(exception.message)
        assertTrue(exception.message!!.contains("Error evaluating script"))
    }
}