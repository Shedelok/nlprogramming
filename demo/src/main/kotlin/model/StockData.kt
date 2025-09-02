package sharepa.demo.model

import kotlinx.serialization.Serializable

@Serializable
data class StockPrice(
    val date: String,
    val price: Double
)

@Serializable
data class StockInfo(
    val symbol: String,
    val name: String,
    val prices: List<StockPrice>
)

@Serializable
data class YahooFinanceResponse(
    val chart: Chart
) {
    @Serializable
    data class Chart(
        val result: List<Result>
    ) {
        @Serializable
        data class Result(
            val meta: Meta,
            val timestamp: List<Long>,
            val indicators: Indicators
        ) {
            @Serializable
            data class Meta(
                val symbol: String,
                val longName: String? = null
            )
            
            @Serializable
            data class Indicators(
                val quote: List<Quote>
            ) {
                @Serializable
                data class Quote(
                    val close: List<Double?>
                )
            }
        }
    }
}