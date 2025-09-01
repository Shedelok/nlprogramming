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

        nlp = NLProgramming(cacheSizeLimitKB = null)
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    fun `should work for add ints a and b`() {
        val result = nlp.compileAndCall(
            """add ints args["a"] and args["b"]""",
            "a" to 5,
            "b" to 3
        )

        assertEquals(8, result)
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    fun `should work for return true if num is odd`() {
        val result = nlp.compileAndCall(
            """return true if args["num"] is odd and false otherwise""",
            "num" to 7
        )

        assertEquals(true, result)
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    fun `should work for count palindromes in array`() {
        val result = nlp.compileAndCall(
            """count how many palindromes are there in Array<String> args["arr"]""",
            "arr" to arrayOf("a", "hello", "madam", "world", "level")
        )

        assertEquals(3, result)
    }

    @Test
    fun `should modify arguments by reference`() {
        val array = intArrayOf(2, 1, 3, -2, 0, 2)

        nlp.compileAndCall(
            """you are given IntArray args["array"]. Replace all values equal 2 with -2 in it""",
            "array" to array
        )

        assertEquals(intArrayOf(-2, 1, 3, -2, 0, -2).toList(), array.toList())
    }

    @Test
    fun `should understand and compile pseudocode`() {
        val array = intArrayOf(1, 2, 3, -10, 5, 6)

        val result = nlp.compileAndCall(
            """
                array = args["array"] as IntArray
                result = new IntArray of the same size
                c = 0
                for each element (e,i) in array:
                  c += e
                  result[i] = c

                return result
            """.trimIndent(),
            "array" to array
        )

        assertEquals(intArrayOf(1, 3, 6, -4, 1, 7).toList(), (result as IntArray).toList())
    }

    @Test
    fun `should throw ambiguity exception for ambiguous prompt`() {
        assertThrows<NlProgrammingAmbiguityException> {
            nlp.compileAndCall(
                """find the first duplicate in list of integers args['list']""",
                "list" to listOf(1, 2, 3, 1, 3)
            )
        }
    }

//    @Test
//    fun `should throw ambiguity exception for ambiguous prompt`() {
//        assertThrows<NlProgrammingAmbiguityException> {
//            nlp.compileAndCall(
//                """sort the list of integers args["list"]""",
//                "list" to listOf(3, 1, 4, 1, 5)
//            )
//        }
//    }
}