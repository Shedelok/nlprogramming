# NLProgramming

A natural language programming library that translates human-readable instructions into executable Kotlin code using
LLMs.

## Overview

NLProgramming allows you to write snippets of code by describing what you want in natural language. The library
translates the prompt into a Kotlin function with the following signature: `fun(args: Map<String, Any>): Any?`.

### Example

```kotlin
fun main() {
    val apiKey = System.getenv("LLM_API_KEY")
        ?: throw IllegalStateException("LLM_API_KEY environment variable is required")

    NLProgramming(apiKey, cacheSizeLimitKB = 50 * 1000).use { nlp ->
        val calcPalindromes = nlp.translateAndCompile(
            """
                calculate number of palindrome strings in list args["arr"].
                if the given list is empty, throw IllegalArgumentException
               """.trimIndent()
        )

        println(calcPalindromes(mapOf("arr" to listOf("ana", "bob")))) // prints 2
        println(calcPalindromes(mapOf("arr" to listOf("xy", "madam")))) // prints 1
        println(calcPalindromes(mapOf("arr" to emptyList<String>()))) // throws IllegalArgumentException
    }
}
```

## Features

**Ambiguity Detection**: The library analyses prompts before translation and verifies that generated code matches your
intent, throwing an exception with some feedback when it can't figure out what exactly you meant to avoid running
undesired code.

**Caching**: The LLM responses can be cached to disk with configurable size limits and TTL, improving library
performance and cost of using LLM APIs by not compiling the same prompt twice even between separate program runs.

## Important Notes

1. This repository is more of a PoC rather than a production-ready dependency for big projects.
2. You can see examples of what the library is capable of in [NLProgrammingIntegrationTest.kt](nlp/src/test/kotlin/sharepa/nlprogramming/NLProgrammingIntegrationTest.kt). To play with it you can use [App.kt](app/src/main/kotlin/App.kt).
3. To use the lib (or to play with it) you need an API_KEY. Currently, 2 LLM APIs are supported:
    * Groq (https://groq.com/, generate a key at https://console.groq.com/keys): free API with weaker models
    * Anthropic(https://docs.anthropic.com/en/api/messages, generate a key and buy some credits
      at https://console.anthropic.com/settings/keys): better models with usage-based billing.
