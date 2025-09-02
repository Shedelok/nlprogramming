package sharepa.demo.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import sharepa.demo.service.StockDataService
import sharepa.nlprogramming.NLProgramming
import kotlin.system.exitProcess

@Serializable
data class AddStockRequest(val symbol: String)

@Serializable
data class StockListItem(val symbol: String, val name: String)

class StockController(
    private val stockDataService: StockDataService,
    private val nlp: NLProgramming
) {

    private val activeStocks = mutableSetOf<String>()

    init {
        activeStocks.add("^GSPC") // S&P 500 as default
    }

    fun setupRoutes(routing: Routing) {
        routing {
            route("/api") {

                get("/stocks") {
                    try {
                        println("API: Getting stocks list - ${activeStocks.size} stocks: $activeStocks")
                        
                        val stockList = activeStocks.map { symbol ->
                            StockListItem(symbol, symbol) // Simplified to avoid NLP calls for listing
                        }
                        
                        println("API: Returning stocks: $stockList")
                        call.respond(stockList)
                    } catch (e: Exception) {
                        println("ERROR in GET /stocks: ${e.message}")
                        e.printStackTrace()
                        call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
                    }
                }

                get("/stocks/{symbol}/history") {
                    val symbol = call.parameters["symbol"]
                        ?: return@get call.respond(HttpStatusCode.BadRequest, "Symbol parameter required")

                    try {
                        println("API: Fetching history for symbol: $symbol")
                        val stockInfo = stockDataService.getStockData(symbol)
                        println("API: Successfully fetched ${stockInfo.prices.size} data points for $symbol")
                        call.respond(stockInfo)
                    } catch (e: Exception) {
                        println("ERROR in GET /stocks/$symbol/history: ${e.message}")
                        e.printStackTrace()
                        call.respond(HttpStatusCode.InternalServerError, "Error fetching data: ${e.message}")
                    }
                }

                post("/stocks") {
                    try {
                        println("API: Received POST /stocks request")
                        val request = call.receive<AddStockRequest>()
                        println("API: Adding stock symbol: ${request.symbol}")

                        val normalizeSymbolFunc = nlp.translateAndCompile(
                            "convert stock symbol args[\"symbol\"] to uppercase and remove any whitespace"
                        )
                        println("API: NLP function compiled successfully")

                        val normalizedSymbol = normalizeSymbolFunc(mapOf("symbol" to request.symbol)) as String
                        println("API: Normalized symbol: $normalizedSymbol")

                        if (activeStocks.add(normalizedSymbol)) {
                            println("API: Stock $normalizedSymbol added successfully. Active stocks: $activeStocks")
                            call.respond(HttpStatusCode.Created, mapOf("message" to "Stock added successfully"))
                        } else {
                            println("API: Stock $normalizedSymbol already exists")
                            call.respond(HttpStatusCode.Conflict, mapOf("message" to "Stock already exists"))
                        }
                    } catch (e: Exception) {
                        println("ERROR in POST /stocks: ${e.message}")
                        e.printStackTrace()
                        call.respond(HttpStatusCode.BadRequest, "Invalid request: ${e.message}")
                    }
                }

                delete("/stocks/{symbol}") {
                    val symbol = call.parameters["symbol"]
                        ?: return@delete call.respond(HttpStatusCode.BadRequest, "Symbol parameter required")

                    try {
                        println("API: Deleting stock symbol: $symbol")
                        if (activeStocks.remove(symbol)) {
                            println("API: Stock $symbol removed successfully. Remaining stocks: $activeStocks")
                            call.respond(HttpStatusCode.OK, mapOf("message" to "Stock removed successfully"))
                        } else {
                            println("API: Stock $symbol not found in active stocks")
                            call.respond(HttpStatusCode.NotFound, mapOf("message" to "Stock not found"))
                        }
                    } catch (e: Exception) {
                        println("ERROR in DELETE /stocks/$symbol: ${e.message}")
                        e.printStackTrace()
                        call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
                    }
                }
            }
        }
    }
}