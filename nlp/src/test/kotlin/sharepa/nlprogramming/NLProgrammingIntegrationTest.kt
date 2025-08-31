package sharepa.nlprogramming

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import java.util.concurrent.TimeUnit
import kotlin.test.assertTrue

class NLProgrammingIntegrationTest {

    private lateinit var nlp: NLProgramming

    @BeforeEach
    fun setUp() {
        val apiKey = System.getenv("GROQ_API_KEY")
        assumeTrue(
            apiKey != null && apiKey.isNotBlank(),
            "GROQ_API_KEY environment variable is required for integration tests"
        )

        nlp = NLProgramming()
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    fun `should work for add a and b`() {
        val result = nlp.implementAndRunFun(
            """add args["a"] and args["b"]""",
            "a" to 5,
            "b" to 3
        )

        assertEquals(8, result)
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    fun `should work for return true if num is odd`() {
        val result = nlp.implementAndRunFun(
            """return true if args["num"] is odd and false otherwise""",
            "num" to 7
        )

        assertEquals(true, result)
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    fun `should work for count palindromes in array`() {
        val result = nlp.implementAndRunFun(
            """count how many palindromes are there in Array<String> args["arr"]""",
            "arr" to arrayOf("a", "hello", "madam", "world", "level")
        )

        assertEquals(3, result)
    }

    @Test
    fun `should throw ambiguity exception for ambiguous prompt`() {
        assertThrows<NlProgrammingAmbiguityException> {
            nlp.implementAndRunFun(
                """sort the list of integers args["list"]""",
                "list" to listOf(3, 1, 4, 1, 5)
            )
        }
    }
}