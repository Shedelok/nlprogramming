package sharepa.demo

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.html.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.*
import sharepa.demo.api.StockController
import sharepa.demo.service.StockDataService
import sharepa.nlprogramming.NLProgramming

fun main() {
    println("Starting Stock Price Dashboard Demo...")
    
    val apiKey = System.getenv("LLM_API_KEY")
        ?: throw IllegalStateException("LLM_API_KEY environment variable is required")
    
    println("Initializing NLProgramming library...")
    
    val nlp = NLProgramming(apiKey, cacheSizeLimitKB = 50 * 1000, sleepBeforeEachLlmCallMillis = 1000)
    val stockDataService = StockDataService(nlp)
    val stockController = StockController(stockDataService, nlp)
    
    // Use atomic boolean to track shutdown state safely
    val isShuttingDown = java.util.concurrent.atomic.AtomicBoolean(false)
    val shutdownLatch = java.util.concurrent.CountDownLatch(1)
    
    // Add shutdown hook with proper blocking
    Runtime.getRuntime().addShutdownHook(Thread {
        if (isShuttingDown.compareAndSet(false, true)) {
            println("\n🔄 Shutdown signal received. Saving cache and cleaning up resources...")
            
            try {
                // Force synchronous cleanup
                stockDataService.close()
                println("📦 HTTP client closed")
                
                // Ensure NLProgramming cache is flushed to disk
                nlp.close()
                println("💾 NLProgramming cache saved to disk")
                
                println("✅ Resources cleaned up successfully.")
            } catch (e: Exception) {
                println("❌ Error during cleanup: ${e.message}")
                e.printStackTrace()
            } finally {
                shutdownLatch.countDown()
            }
        }
    })
    
    println("Setting up services...")
    println("Services initialized. Starting web server...")

    try {
        embeddedServer(Netty, port = 8080) {
            install(ContentNegotiation) {
                json()
            }
            
            install(CORS) {
                anyHost()
                allowHeader("Content-Type")
            }

            routing {
                stockController.setupRoutes(this)
                
                get("/") {
                    call.respondHtml {
                        head {
                            title("Stock Price Dashboard")
                            meta(charset = "UTF-8")
                            meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
                            script(src = "https://cdn.jsdelivr.net/npm/chart.js") {}
                            style {
                                unsafe {
                                    raw("""
                                        body { 
                                            font-family: Arial, sans-serif; 
                                            margin: 20px; 
                                            background: #f5f5f5;
                                        }
                                        .container { 
                                            max-width: 1200px; 
                                            margin: 0 auto; 
                                        }
                                        .header { 
                                            background: white; 
                                            padding: 20px; 
                                            border-radius: 8px; 
                                            margin-bottom: 20px;
                                            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                                        }
                                        .controls { 
                                            display: flex; 
                                            gap: 10px; 
                                            align-items: center; 
                                        }
                                        .controls input { 
                                            padding: 8px; 
                                            border: 1px solid #ddd; 
                                            border-radius: 4px; 
                                        }
                                        .controls button { 
                                            padding: 8px 16px; 
                                            border: none; 
                                            border-radius: 4px; 
                                            cursor: pointer; 
                                        }
                                        .add-btn { 
                                            background: #007bff; 
                                            color: white; 
                                        }
                                        .add-btn:hover { 
                                            background: #0056b3; 
                                        }
                                        .stock-chart { 
                                            background: white; 
                                            padding: 20px; 
                                            margin-bottom: 20px; 
                                            border-radius: 8px;
                                            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                                            position: relative;
                                        }
                                        .chart-header { 
                                            display: flex; 
                                            justify-content: space-between; 
                                            align-items: center; 
                                            margin-bottom: 15px; 
                                        }
                                        .delete-btn { 
                                            background: #dc3545; 
                                            color: white; 
                                            padding: 5px 10px; 
                                            font-size: 12px; 
                                        }
                                        .delete-btn:hover { 
                                            background: #c82333; 
                                        }
                                        .chart-container { 
                                            height: 300px; 
                                            width: 100%; 
                                        }
                                        .loading { 
                                            text-align: center; 
                                            color: #666; 
                                            font-style: italic; 
                                        }
                                    """)
                                }
                            }
                        }
                        body {
                            div(classes = "container") {
                                div(classes = "header") {
                                    h1 { +"Stock Price Dashboard" }
                                    p { +"Powered by NLProgramming Library" }
                                    
                                    div(classes = "controls") {
                                        input(type = InputType.text) {
                                            id = "stockSymbol"
                                            placeholder = "Enter stock symbol (e.g., AAPL)"
                                        }
                                        button(classes = "add-btn") {
                                            id = "addStockBtn"
                                            +"Add Stock"
                                        }
                                    }
                                }
                                
                                div {
                                    id = "stockCharts"
                                }
                            }
                            
                            script {
                                unsafe {
                                    raw("""
                                        class StockDashboard {
                                            constructor() {
                                                this.charts = new Map();
                                                this.initializeEventListeners();
                                                this.loadInitialStocks();
                                            }
                                            
                                            initializeEventListeners() {
                                                document.getElementById('addStockBtn').addEventListener('click', () => {
                                                    this.addStock();
                                                });
                                                
                                                document.getElementById('stockSymbol').addEventListener('keypress', (e) => {
                                                    if (e.key === 'Enter') {
                                                        this.addStock();
                                                    }
                                                });
                                            }
                                            
                                            async loadInitialStocks() {
                                                try {
                                                    const response = await fetch('/api/stocks');
                                                    const stocks = await response.json();
                                                    
                                                    for (const stock of stocks) {
                                                        await this.createStockChart(stock.symbol);
                                                    }
                                                } catch (error) {
                                                    console.error('Error loading initial stocks:', error);
                                                }
                                            }
                                            
                                            async addStock() {
                                                const symbolInput = document.getElementById('stockSymbol');
                                                const symbol = symbolInput.value.trim().toUpperCase();
                                                
                                                if (!symbol) return;
                                                
                                                try {
                                                    const response = await fetch('/api/stocks', {
                                                        method: 'POST',
                                                        headers: {
                                                            'Content-Type': 'application/json',
                                                        },
                                                        body: JSON.stringify({ symbol })
                                                    });
                                                    
                                                    if (response.ok) {
                                                        await this.createStockChart(symbol);
                                                        symbolInput.value = '';
                                                    } else {
                                                        const error = await response.json();
                                                        alert('Error: ' + error.message);
                                                    }
                                                } catch (error) {
                                                    alert('Error adding stock: ' + error.message);
                                                }
                                            }
                                            
                                            async createStockChart(symbol) {
                                                const chartsContainer = document.getElementById('stockCharts');
                                                
                                                const chartDiv = document.createElement('div');
                                                chartDiv.className = 'stock-chart';
                                                chartDiv.id = `chart-${'$'}{symbol}`;
                                                
                                                chartDiv.innerHTML = `
                                                    <div class="chart-header">
                                                        <h3>${'$'}{symbol}</h3>
                                                        <button class="delete-btn" onclick="dashboard.deleteStock('${'$'}{symbol}')">Delete</button>
                                                    </div>
                                                    <div class="chart-container">
                                                        <div class="loading">Loading chart data...</div>
                                                        <canvas id="chart-canvas-${'$'}{symbol}" style="display: none;"></canvas>
                                                    </div>
                                                `;
                                                
                                                chartsContainer.appendChild(chartDiv);
                                                
                                                try {
                                                    const response = await fetch(`/api/stocks/${'$'}{symbol}/history`);
                                                    const stockData = await response.json();
                                                    
                                                    this.renderChart(symbol, stockData);
                                                } catch (error) {
                                                    chartDiv.querySelector('.loading').textContent = 'Error loading data: ' + error.message;
                                                }
                                            }
                                            
                                            renderChart(symbol, stockData) {
                                                const canvas = document.getElementById(`chart-canvas-${'$'}{symbol}`);
                                                const loadingDiv = canvas.parentElement.querySelector('.loading');
                                                
                                                loadingDiv.style.display = 'none';
                                                canvas.style.display = 'block';
                                                
                                                const ctx = canvas.getContext('2d');
                                                
                                                const chart = new Chart(ctx, {
                                                    type: 'line',
                                                    data: {
                                                        labels: stockData.prices.map(p => p.date),
                                                        datasets: [{
                                                            label: `${'$'}{stockData.name} Price`,
                                                            data: stockData.prices.map(p => p.price),
                                                            borderColor: '#007bff',
                                                            backgroundColor: 'rgba(0, 123, 255, 0.1)',
                                                            tension: 0.1,
                                                            fill: true
                                                        }]
                                                    },
                                                    options: {
                                                        responsive: true,
                                                        maintainAspectRatio: false,
                                                        scales: {
                                                            y: {
                                                                beginAtZero: false,
                                                                title: {
                                                                    display: true,
                                                                    text: 'Price (${'$'})'
                                                                }
                                                            },
                                                            x: {
                                                                title: {
                                                                    display: true,
                                                                    text: 'Date'
                                                                }
                                                            }
                                                        },
                                                        plugins: {
                                                            legend: {
                                                                display: true,
                                                                position: 'top'
                                                            }
                                                        }
                                                    }
                                                });
                                                
                                                this.charts.set(symbol, chart);
                                            }
                                            
                                            async deleteStock(symbol) {
                                                try {
                                                    const response = await fetch(`/api/stocks/${'$'}{symbol}`, {
                                                        method: 'DELETE'
                                                    });
                                                    
                                                    if (response.ok) {
                                                        const chartElement = document.getElementById(`chart-${'$'}{symbol}`);
                                                        if (chartElement) {
                                                            const chart = this.charts.get(symbol);
                                                            if (chart) {
                                                                chart.destroy();
                                                                this.charts.delete(symbol);
                                                            }
                                                            chartElement.remove();
                                                        }
                                                    } else {
                                                        const error = await response.json();
                                                        alert('Error: ' + error.message);
                                                    }
                                                } catch (error) {
                                                    alert('Error deleting stock: ' + error.message);
                                                }
                                            }
                                        }
                                        
                                        const dashboard = new StockDashboard();
                                    """)
                                }
                            }
                        }
                    }
                }
            }
        }.start(wait = false)
        
        println("✅ Stock Dashboard is running at: http://localhost:8080")
        println("Press Ctrl+C to stop the server")
        
        // Keep the main thread alive until shutdown signal
        while (!isShuttingDown.get()) {
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                break
            }
        }
        
        // Wait for shutdown hook to complete (max 10 seconds)
        println("⏳ Waiting for cleanup to complete...")
        if (!shutdownLatch.await(10, java.util.concurrent.TimeUnit.SECONDS)) {
            println("⚠️  Cleanup timeout - forcing exit")
        }
        
    } catch (e: Exception) {
        println("ERROR starting server: ${e.message}")
        e.printStackTrace()
        throw e
    }
}