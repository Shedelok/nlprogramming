package sharepa.nlprogramming

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import java.util.concurrent.TimeUnit
import kotlin.test.assertTrue
import kotlin.test.fail

class NLProgrammingIntegrationTest {

    private lateinit var nlp: NLProgramming

    @BeforeEach
    fun setUp() {
        val apiKey = System.getenv("GROQ_API_KEY")
        assumeTrue(
            apiKey != null && apiKey.isNotBlank(),
            "GROQ_API_KEY environment variable is required for integration tests"
        )

        nlp = NLProgramming(apiKey, cacheSizeLimitKB = null, sleepBeforeEachLlmCallMillis = 5000)
    }

    @Test
    fun `should work for add ints a and b`() {
        val result = nlp.translateAndCompile("""add ints args["a"] and args["b"]""")(
            mapOf(
                "a" to 5,
                "b" to 3
            )
        )

        assertEquals(8, result)
    }

    @Test
    fun `should work for return true if num is odd`() {
        val result = nlp.translateAndCompile("""return true if args["num"] is odd and false otherwise""")(
            mapOf("num" to 7)
        )

        assertEquals(true, result)
    }

    @Test
    fun `should work for count palindromes in array`() {
        val result = nlp.translateAndCompile("""count how many palindromes are there in Array<String> args["arr"]""")(
            mapOf("arr" to arrayOf("a", "hello", "madam", "world", "level"))
        )

        assertEquals(3, result)
    }

    @Test
    fun `should work with nulls as arguments`() {
        val result =
            nlp.translateAndCompile(""" if object args["o"] is null, return string "null", otherwise return string "not-null" """)(
                mapOf("o" to null)
            )

        assertEquals("null", result)
    }

    @Test
    fun `should modify arguments by reference`() {
        val array = intArrayOf(2, 1, 3, -2, 0, 2)

        nlp.translateAndCompile("""you are given IntArray args["array"]. Replace all values equal 2 with -2 in it""")(
            mapOf("array" to array)
        )

        assertEquals(intArrayOf(-2, 1, 3, -2, 0, -2).toList(), array.toList())
    }

    @Test
    fun `should understand and compile pseudocode`() {
        val array = intArrayOf(1, 2, 3, -10, 5, 6)

        val result = nlp.translateAndCompile(
            """
                array = args["array"] as IntArray
                result = new IntArray of the same size
                c = 0
                for each element (e,i) in array:
                  c += e
                  result[i] = c

                return result
            """.trimIndent()
        )(mapOf("array" to array))

        assertEquals(intArrayOf(1, 3, 6, -4, 1, 7).toList(), (result as IntArray).toList())
    }

    @Test
    fun `should throw exception for slightly ambiguous prompt`() {
        assertThrows<NlProgrammingCompilationException> {
            nlp.translateAndCompile("""locate the first duplication in list args['list']""")
        }
    }

    @Test
    fun `should throw ambiguity exception for very ambiguous prompt`() {
        assertThrows<NlProgrammingAmbiguityException> {
            nlp.translateAndCompile("""find min, median and max in the list""")
        }
    }
}