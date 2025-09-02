package sharepa.demo.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import sharepa.demo.model.*
import sharepa.nlprogramming.NLProgramming
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class StockDataService(private val nlp: NLProgramming) {
    
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { 
                ignoreUnknownKeys = true 
                isLenient = true
            })
        }
    }
    
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    
    suspend fun getStockData(symbol: String): StockInfo {
        try {
            println("SERVICE: Fetching data for symbol: $symbol")
            
            val endTime = nlp.translateAndCompile("get current unix timestamp in seconds")(emptyMap()) as Long
            println("SERVICE: End time: $endTime")
            
            val startTime = nlp.translateAndCompile(
                "calculate unix timestamp for 30 days before given timestamp args[\"endTime\"]"
            )(mapOf("endTime" to endTime))
            println("SERVICE: Start time: $startTime (30 days ago)")
            
            val url = "https://query2.finance.yahoo.com/v8/finance/chart/$symbol" +
                    "?period1=$startTime&period2=$endTime&interval=1d"
            println("SERVICE: Yahoo Finance URL: $url")
            
            val response: YahooFinanceResponse = httpClient.get(url) {
                header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            }.body()
            println("SERVICE: Received response for $symbol")
            
            val result = response.chart.result.firstOrNull()
                ?: throw IllegalStateException("No data found for symbol: $symbol")
            println("SERVICE: Found ${result.timestamp.size} data points for $symbol")
            
            val prices = convertToPrices(result.timestamp, result.indicators.quote.first().close)
            println("SERVICE: Converted to ${prices.size} price entries")
            
            val stockInfo = StockInfo(
                symbol = result.meta.symbol,
                name = result.meta.longName ?: symbol,
                prices = prices
            )
            println("SERVICE: Successfully created StockInfo for ${stockInfo.symbol}")
            
            return stockInfo
        } catch (e: Exception) {
            println("ERROR in StockDataService.getStockData for $symbol: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
    
    private fun convertToPrices(timestamps: List<Long>, prices: List<Double?>): List<StockPrice> {
        try {
            println("SERVICE: Converting ${timestamps.size} timestamps to prices")
            val processDataFunc = nlp.translateAndCompile(
                """
                import sharepa.demo.model.StockPrice
                combine timestamps args["timestamps"] with prices args["prices"], 
                skip entries where price is null,
                convert each timestamp to yyyy-MM-dd date format,
                return list of StockPrice objects with date and price
                """.trimIndent()
            )
            println("SERVICE: NLP data processing function compiled")
            
            val result = processDataFunc(mapOf("timestamps" to timestamps, "prices" to prices)) as List<StockPrice>
            println("SERVICE: Converted to ${result.size} valid price entries")
            return result
        } catch (e: Exception) {
            println("ERROR in convertToPrices: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
    
    fun close() {
        httpClient.close()
    }
}