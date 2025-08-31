package sharepa.nlprogramming

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.Assumptions.assumeTrue
import kotlin.test.assertEquals
import java.util.concurrent.TimeUnit

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
    fun `add a and b`() {
        val result = nlp.implementAndRunFun(
            "add args['a'] and args['b']",
            "a" to 5,
            "b" to 3
        )

        assertEquals(8, result)
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    fun `return true if num is odd`() {
        val result = nlp.implementAndRunFun(
            "return true if args['num'] is odd and false otherwise",
            "num" to 7
        )

        assertEquals(true, result)
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    fun `count palindromes in array`() {
        val result = nlp.implementAndRunFun(
            "count how many palindromes are there in Array<String> args['arr']",
            "arr" to arrayOf("a", "hello", "madam", "world", "level")
        )

        assertEquals(3, result)
    }
}