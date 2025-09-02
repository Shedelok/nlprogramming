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

*[More examples below](#more-examples--demo)*

## Features

**Ambiguity Detection**: The library analyses prompts before translation and verifies that generated code matches your
intent, throwing an exception with some feedback when it can't figure out what exactly you meant to avoid running
undesired code.

**Caching**: The LLM responses can be cached to disk with configurable size limits and TTL, improving library
performance and cost of using LLM APIs by not compiling the same prompt twice even between separate program runs.

## Important Notes

1. This repository is more of a PoC rather than a production-ready dependency for big projects. Make sure to cover code generated with this library with tests.
2. To use the lib (or to play with it) you need an API_KEY. Currently, 2 LLM APIs are supported:
    * Groq (https://groq.com/, generate a key at https://console.groq.com/keys): free API with weaker models
    * Anthropic (https://docs.anthropic.com/en/api/messages, generate a key and buy some credits
      at https://console.anthropic.com/settings/keys): better models with usage-based billing.

## More Examples + Demo

You can see some examples of what the library is capable of in
* [NLProgrammingIntegrationTest.kt](nlp/src/test/kotlin/sharepa/nlprogramming/NLProgrammingIntegrationTest.kt),
* Executable [App.kt](app/src/main/kotlin/App.kt),
* A tiny vibe-coded web app that has a few usages of the lib. More info about the demo app in [its README.md](demo/README.md).

Here are more examples of NLProgramming usage from the demo application:

### 1. Complex data processing
**Location:** [StockDataService.kt:76-83](demo/src/main/kotlin/service/StockDataService.kt#L76-L83)
```kotlin
val processDataFunc = nlp.translateAndCompile(
    """
    import sharepa.demo.model.StockPrice
    combine timestamps args["timestamps"] with prices args["prices"], 
    skip entries where price is null,
    convert each timestamp to yyyy-MM-dd date format,
    return list of StockPrice objects with date and price
    """.trimIndent()
)
```

### 2. Getting current timestamp
**Location:** [StockDataService.kt:34](demo/src/main/kotlin/service/StockDataService.kt#L34)
```kotlin
val endTime = nlp.translateAndCompile("get current unix timestamp in seconds")(emptyMap()) as Long
```

### 3. Date calculations
**Location:** [StockDataService.kt:37-39](demo/src/main/kotlin/service/StockDataService.kt#L37-L39)
```kotlin
val startTime = nlp.translateAndCompile(
    "calculate unix timestamp for 30 days before given timestamp args[\"endTime\"]"
)(mapOf("endTime" to endTime))
```

### 4. String normalization
**Location:** [StockController.kt:73-75](demo/src/main/kotlin/api/StockController.kt#L73-L75)
```kotlin
val normalizeSymbolFunc = nlp.translateAndCompile(
    "convert stock symbol args[\"symbol\"] to uppercase and remove any whitespace"
)
```
