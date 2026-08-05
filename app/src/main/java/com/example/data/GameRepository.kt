package com.example.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.random.Random

class GameRepository(private val db: AppDatabase) {

    private val marketDao = db.marketDao()
    private val traderDao = db.traderDao()
    private val newsDao = db.newsDao()
    private val settingsDao = db.settingsDao()

    private var warn1000Sent = false
    private var warn2000Sent = false

    val tradersFlow: Flow<List<Trader>> = traderDao.getAllTradersFlow()
    val newsLogsFlow: Flow<List<NewsLog>> = newsDao.getNewsFlow()
    val settingsFlow: Flow<GameSettings?> = settingsDao.getSettingsFlow()

    fun getCandlesFlow(symbol: String): Flow<List<MarketCandle>> = marketDao.getCandlesFlow(symbol)
    suspend fun getCandlesList(symbol: String): List<MarketCandle> = withContext(Dispatchers.IO) {
        marketDao.getCandlesList(symbol)
    }
    fun getPositionsForTraderFlow(traderId: String): Flow<List<TraderPosition>> = traderDao.getPositionsForTraderFlow(traderId)

    // Constants for Assets
    val assets = listOf(
        AssetInfo("MKTX", "Piyasa Endeksi", "Stabil, dengeli küresel borsa endeksi", 100.0, 0.0001, 0.04),
        AssetInfo("SOLR", "Solar Enerji", "Yüksek volatilite, haber odaklı temiz enerji hissesi", 50.0, 0.0003, 0.09),
        AssetInfo("NEOM", "Teknoloji Devi", "İstikrarlı büyüyen, premium teknoloji şirketi", 250.0, 0.0001, 0.03),
        AssetInfo("VOID", "Meme Coin", "Aşırı spekülatif, çılgın fiyat hareketleri olan kripto para", 5.0, 0.0005, 0.22)
    )

    data class AssetInfo(
        val symbol: String,
        val displayName: String,
        val description: String,
        val startPrice: Double,
        val drift: Double,       // Expected trend
        val volatility: Double   // Standard deviation/variance factor
    )

    // Retrieve active settings or build default
    suspend fun getOrInitSettings(): GameSettings = withContext(Dispatchers.IO) {
        var settings = settingsDao.getSettings()
        if (settings == null) {
            val systemLang = java.util.Locale.getDefault().language.uppercase()
            val defaultLang = when {
                systemLang.contains("TR") -> "TR"
                systemLang.contains("AZ") -> "AZ"
                systemLang.contains("RU") -> "RU"
                systemLang.contains("ZH") -> "ZH"
                systemLang.contains("FR") -> "FR"
                systemLang.contains("HI") -> "HI"
                else -> "EN"
            }
            settings = GameSettings(id = 1, selectedLanguage = defaultLang)
            settingsDao.insertOrUpdateSettings(settings)
        }
        settings
    }

    suspend fun updateSettings(settings: GameSettings) = withContext(Dispatchers.IO) {
        settingsDao.insertOrUpdateSettings(settings)
    }

    // Check and Initialize Game Data if empty
    suspend fun initializeGameIfNeeded() = withContext(Dispatchers.IO) {
        val existingTraders = traderDao.getAllTradersList()
        if (existingTraders.isEmpty()) {
            Log.d("GameRepository", "Database is empty. Initializing simulation...")
            
            // 1. Generate 200 AI Traders
            val generatedTraders = generate200Traders()
            traderDao.insertTraders(generatedTraders)

            // 2. Generate Historical Market Data (50 candles each)
            val initialCandles = mutableListOf<MarketCandle>()
            assets.forEach { asset ->
                var currentPrice = asset.startPrice
                var timestamp = System.currentTimeMillis() - (50 * 60000) // 50 minutes ago
                for (i in 1..50) {
                    val changePercent = asset.drift + (Random.nextDouble(-1.0, 1.0) * asset.volatility)
                    val close = (currentPrice * (1.0 + changePercent)).coerceAtLeast(0.01)
                    val high = (maxOf(currentPrice, close) * (1.0 + Random.nextDouble(0.0, asset.volatility / 2))).coerceAtLeast(0.01)
                    val low = (minOf(currentPrice, close) * (1.0 - Random.nextDouble(0.0, asset.volatility / 2))).coerceAtLeast(0.01)
                    val volume = Random.nextDouble(500.0, 10000.0) * (asset.startPrice / currentPrice).coerceAtLeast(0.1)

                    initialCandles.add(
                        MarketCandle(
                            symbol = asset.symbol,
                            timestamp = timestamp,
                            open = currentPrice,
                            high = high,
                            low = low,
                            close = close,
                            volume = volume
                        )
                    )
                    currentPrice = close
                    timestamp += 60000 // +1 minute
                }
            }
            marketDao.insertCandles(initialCandles)

            // Ensure GameSettings is populated
            getOrInitSettings()

            // 3. Populate Initial System News
            val lang = settingsDao.getSettings()?.selectedLanguage ?: "TR"
            val initialMessage = if (lang == "TR") {
                "Simülasyon başladı! 200 yapay zeka yatırımcı aktif şekilde piyasada işlem yapıyor. Sıfır sermaye ile başlayıp mini oyunlarla fon kazanın, spot veya kaldıraçlı borsa işlemlerinde zirveye oynayın!"
            } else {
                "Simulation started! 200 AI traders are active. Start with zero capital, earn funds via mini games, and play for the top in spot or leverage trading!"
            }
            newsDao.insertNews(
                NewsLog(
                    timestamp = System.currentTimeMillis(),
                    traderName = if (lang == "TR") "SİSTEM" else "SYSTEM",
                    message = initialMessage,
                    symbol = "GENEL",
                    isSystemNews = true
                )
            )
        }
    }

    private fun generate200Traders(): List<Trader> {
        val archetypes = listOf("TREND_FOLLOWER", "CONTRARIAN", "SCALPER", "WHALE", "CHAOS", "HODLER", "PANIC_SELLER")
        val traders = mutableListOf<Trader>()

        // Add Player as the 201st trader (0 capital start!)
        traders.add(
            Trader(
                id = "player",
                name = "Siz (Kullanıcı)",
                archetype = "USER_CHOICE",
                cash = 0.0, // 0 capital start!
                initialCapital = 0.0,
                winRate = 0.0,
                isPlayer = true,
                rank = 201
            )
        )

        val firstNames = listOf(
            "Can", "Efe", "Mert", "Arda", "Zeynep", "Elif", "Deniz", "Kaan", "Cem", "Selin", 
            "Murat", "Banu", "Burak", "Hakan", "Yusuf", "Aylin", "Aslı", "Onur", "Volkan", "Tolga", 
            "Buse", "Gökhan", "Ebru", "Oğuz", "Tarık", "Gizem", "Emre", "Fatih", "Kerem", "İrem"
        )
        val lastNames = listOf(
            "Kaya", "Demir", "Çelik", "Şahin", "Yıldız", "Öztürk", "Arslan", "Yılmaz", "Aydın", "Koç", 
            "Bulut", "Kılıç", "Özkan", "Aksoy", "Yalçın", "Polat", "Erdoğan", "Güler", "Yurt", "Şen"
        )
        val suffixes = listOf(
            "HODLer", "Bull", "Bear", "Whale", "Scalper", "Moon", "Pro", "Macro", "Alpha", "Apex", 
            "Max", "Chad", "Quant", "Algo", "Trader", "Guru", "Master", "Wizard", "Ninja", "Hedge"
        )

        val uniqueNames = mutableSetOf<String>()
        var count = 1
        while (count <= 200) {
            val formatType = Random.nextInt(3)
            val name = when (formatType) {
                0 -> "${firstNames.random()} ${lastNames.random()}"
                1 -> "${firstNames.random()}_${suffixes.random()}"
                else -> "${suffixes.random()}_${lastNames.random()}"
            }

            if (!uniqueNames.contains(name)) {
                uniqueNames.add(name)
                val arch = if (count <= 5) "WHALE" else archetypes.random() // Whales are large market makers
                val capital = when (arch) {
                    "WHALE" -> Random.nextDouble(100000.0, 500000.0)
                    "HODLER" -> Random.nextDouble(5000.0, 20000.0)
                    "SCALPER" -> Random.nextDouble(4000.0, 15000.0)
                    else -> Random.nextDouble(8000.0, 30000.0)
                }
                traders.add(
                    Trader(
                        id = "trader_$count",
                        name = name,
                        archetype = arch,
                        cash = capital,
                        initialCapital = capital,
                        winRate = Random.nextDouble(40.0, 68.0),
                        isPlayer = false,
                        rank = count
                    )
                )
                count++
            }
        }
        return traders
    }

    // Earn cash through mini games / jobs
    suspend fun earnMiniGameCash(amount: Double, gameName: String) = withContext(Dispatchers.IO) {
        val player = traderDao.getTraderById("player") ?: return@withContext
        val newCash = player.cash + amount
        // If initial capital was 0, let's keep track of initial capital as what they earned to make ROI realistic,
        // or just keep it 0 and we handle ROI gracefully.
        val newInitial = if (player.initialCapital == 0.0) amount else player.initialCapital
        traderDao.updateTrader(player.copy(cash = newCash, initialCapital = newInitial))

        newsDao.insertNews(
            NewsLog(
                timestamp = System.currentTimeMillis(),
                traderName = "Siz (Kullanıcı)",
                message = "Mini Oyundan ($gameName) $${String.format("%.2f", amount)} nakit kazandınız! 🎮💰",
                symbol = "GENEL",
                isSystemNews = false
            )
        )
    }

    // Google Profile Sync
    suspend fun syncGoogleProfile(email: String, name: String, avatarUrl: String) = withContext(Dispatchers.IO) {
        val currentSettings = getOrInitSettings()
        updateSettings(
            currentSettings.copy(
                googleEmail = email,
                googleName = name,
                googleAvatarUrl = avatarUrl
            )
        )

        // Sync Player trader profile name too!
        val player = traderDao.getTraderById("player")
        if (player != null) {
            traderDao.updateTrader(player.copy(name = name))
        }
    }

    // Advance turn (Generate new candles, run AI trade decisions, update ranks, check player liquidations)
    suspend fun advanceTurn() = withContext(Dispatchers.IO) {
        val traders = traderDao.getAllTradersList().toMutableList()
        val allPositionsList = traderDao.getAllPositions()
        val allPositions = allPositionsList.groupBy { it.traderId }

        // 1. Advance prices for each asset with 60% Indicator, 70% Strategy probabilities
        val currentPrices = mutableMapOf<String, Double>()
        val previousCandlesMap = mutableMapOf<String, List<MarketCandle>>()

        assets.forEach { asset ->
            val candles = marketDao.getCandlesList(asset.symbol)
            previousCandlesMap[asset.symbol] = candles
            if (candles.isEmpty()) return@forEach
            val lastCandle = candles.last()

            // A. Calculate Indicator Direction (+1 or -1)
            val sma10 = calculateSMA(candles, 10)
            val rsi10 = calculateRSI(candles, 10)
            var indDir = 0
            if (lastCandle.close > sma10) indDir += 1 else indDir -= 1
            if (rsi10 < 35.0) indDir += 1
            if (rsi10 > 65.0) indDir -= 1
            val indicatorDirection = when {
                indDir > 0 -> 1.0
                indDir < 0 -> -1.0
                else -> 0.0
            }

            // B. Calculate Popular Strategy Direction (+1 or -1)
            // Look at average positioning of AI Whales/Trend followers
            val totalSymbolPositions = allPositionsList.count { it.symbol == asset.symbol }
            val strategyDirection = if (totalSymbolPositions > 25) 1.0 else -1.0

            // C. Apply Probabilities:
            // 60% probability the indicator determines direction
            // 70% probability the strategy consensus determines direction
            var predictedMovement = 0.0
            if (Random.nextDouble() < 0.60) {
                predictedMovement += indicatorDirection * asset.volatility * 0.7
            }
            if (Random.nextDouble() < 0.70) {
                predictedMovement += strategyDirection * asset.volatility * 0.8
            }

            // D. Trend Cycles and Volatility Shocks (Requirement 3)
            // Cycle shifts every 15-20 candles per asset to vary trend lines
            val candleCount = candles.size
            val cycleIndex = candleCount / 18
            val cycleSeed = (asset.symbol.hashCode() + cycleIndex).toLong()
            val cycleRandom = java.util.Random(cycleSeed)
            
            // Cycle type: 0 = Strong Upward Bull, 1 = Strong Downward Bear, 2 = Sideways Normal
            val cycleType = cycleRandom.nextInt(3)
            val cycleDrift = when (cycleType) {
                0 -> asset.volatility * 0.16 // Bull trend
                1 -> -asset.volatility * 0.18 // Bear trend
                else -> 0.0
            }

            // Sudden giant shocks (flash crash or god pump) or normal volatility-based noise
            val shockRand = Random.nextDouble()
            val shockFactor = when {
                shockRand < 0.04 -> -Random.nextDouble(0.12, 0.28) // FLASH CRASH!
                shockRand > 0.96 -> Random.nextDouble(0.10, 0.24)  // GOD PUMP!
                else -> Random.nextDouble(-0.5, 0.5) * asset.volatility // Standard motion
            }

            val changePercent = asset.drift + cycleDrift + predictedMovement + shockFactor

            val open = lastCandle.close
            val close = (open * (1.0 + changePercent)).coerceAtLeast(0.01)
            val high = (maxOf(open, close) * (1.0 + Random.nextDouble(0.0, asset.volatility / 3.2))).coerceAtLeast(0.01)
            val low = (minOf(open, close) * (1.0 - Random.nextDouble(0.0, asset.volatility / 3.2))).coerceAtLeast(0.01)
            val volume = Random.nextDouble(800.0, 12000.0) * (asset.startPrice / close).coerceAtLeast(0.1)

            val newCandle = MarketCandle(
                symbol = asset.symbol,
                timestamp = lastCandle.timestamp + 60000,
                open = open,
                high = high,
                low = low,
                close = close,
                volume = volume
            )
            marketDao.insertCandles(listOf(newCandle))
            currentPrices[asset.symbol] = close
        }

        // 2. CHECK PLAYER LIQUIDATIONS & TP/SL ORDERS
        val player = traderDao.getTraderById("player")
        if (player != null) {
            val playerPositions = traderDao.getPositionsForTraderList("player")
            playerPositions.forEach { pos ->
                val currentPrice = currentPrices[pos.symbol] ?: return@forEach
                if (pos.isLeverage) {
                    var isLiquidated = false
                    if (pos.isLong && currentPrice <= pos.liquidationPrice) {
                        isLiquidated = true
                    } else if (!pos.isLong && currentPrice >= pos.liquidationPrice) {
                        isLiquidated = true
                    }

                    if (isLiquidated) {
                        // Liquidate! Delete position, do not return margin.
                        traderDao.deletePosition(pos)
                        
                        // Publish breaking liquidation news
                        val lang = settingsDao.getSettings()?.selectedLanguage ?: "TR"
                        val liqMessage = if (lang == "TR") {
                            "🚨 LİKİDASYON ALARMI: Siz (Kullanıcı), $${String.format("%.2f", currentPrice)} fiyattan $${String.format("%.2f", pos.liquidationPrice)} Likidasyon sınırına çarparak $${String.format("%.2f", pos.margin)} değerindeki kaldıraçlı ${if (pos.isLong) "LONG" else "SHORT"} pozisyonunuzu kaybettiniz!"
                        } else {
                            "🚨 LIQUIDATION ALERT: You (User) hit the liquidation level of $${String.format("%.2f", pos.liquidationPrice)} at $${String.format("%.2f", currentPrice)} and lost your leveraged ${if (pos.isLong) "LONG" else "SHORT"} position with a margin of $${String.format("%.2f", pos.margin)}!"
                        }
                        newsDao.insertNews(
                            NewsLog(
                                timestamp = System.currentTimeMillis(),
                                traderName = if (lang == "TR") "KARA KUTU" else "LIQUIDATION",
                                message = liqMessage,
                                symbol = pos.symbol,
                                isSystemNews = true
                            )
                        )
                    } else {
                        // Check Take Profit and Stop Loss triggers for leverage positions
                        var isTpTriggered = false
                        var isSlTriggered = false

                        if (pos.takeProfitPrice > 0.0) {
                            if (pos.isLong && currentPrice >= pos.takeProfitPrice) {
                                isTpTriggered = true
                            } else if (!pos.isLong && currentPrice <= pos.takeProfitPrice) {
                                isTpTriggered = true
                            }
                        }

                        if (pos.stopLossPrice > 0.0 && !isTpTriggered) {
                            if (pos.isLong && currentPrice <= pos.stopLossPrice) {
                                isSlTriggered = true
                            } else if (!pos.isLong && currentPrice >= pos.stopLossPrice) {
                                isSlTriggered = true
                            }
                        }

                        if (isTpTriggered || isSlTriggered) {
                            val priceDiff = if (pos.isLong) {
                                currentPrice - pos.averageEntryPrice
                            } else {
                                pos.averageEntryPrice - currentPrice
                            }
                            val pnl = priceDiff * pos.quantity
                            val returnedCash = (pos.margin + pnl).coerceAtLeast(0.0)
                            
                            val latestPlayer = traderDao.getTraderById("player")
                            if (latestPlayer != null) {
                                traderDao.updateTrader(latestPlayer.copy(cash = latestPlayer.cash + returnedCash))
                            }
                            traderDao.deletePosition(pos)

                            val lang = settingsDao.getSettings()?.selectedLanguage ?: "TR"
                            val triggerLabel = if (lang == "TR") {
                                if (isTpTriggered) "KÂR AL (TP)" else "ZARAR DURDUR (SL)"
                            } else {
                                if (isTpTriggered) "TAKE PROFIT (TP)" else "STOP LOSS (SL)"
                            }
                            val orderMessage = if (lang == "TR") {
                                "⚡ $triggerLabel TETİKLENDİ: Kaldıraçlı ${if (pos.isLong) "LONG" else "SHORT"} pozisyonunuz $${String.format("%.2f", currentPrice)} fiyattan kapatıldı. Kâr/Zarar: $${String.format("%.2f", pnl)}, Cüzdana Eklenen Nakit: $${String.format("%.2f", returnedCash)}"
                            } else {
                                "⚡ $triggerLabel TRIGGERED: Your leveraged ${if (pos.isLong) "LONG" else "SHORT"} position was closed at $${String.format("%.2f", currentPrice)}. PnL: $${String.format("%.2f", pnl)}, Cash returned: $${String.format("%.2f", returnedCash)}"
                            }
                            newsDao.insertNews(
                                NewsLog(
                                    timestamp = System.currentTimeMillis(),
                                    traderName = if (lang == "TR") "ALGORİTMİK EMİR" else "ALGORITHMIC ORDER",
                                    message = orderMessage,
                                    symbol = pos.symbol,
                                    isSystemNews = true
                                )
                            )
                        }
                    }
                } else {
                    // Check Take Profit and Stop Loss triggers for Spot positions
                    var isTpTriggered = false
                    var isSlTriggered = false

                    if (pos.takeProfitPrice > 0.0) {
                        if (currentPrice >= pos.takeProfitPrice) {
                            isTpTriggered = true
                        }
                    }

                    if (pos.stopLossPrice > 0.0 && !isTpTriggered) {
                        if (currentPrice <= pos.stopLossPrice) {
                            isSlTriggered = true
                        }
                    }

                    if (isTpTriggered || isSlTriggered) {
                        val sellProceeds = pos.quantity * currentPrice
                        val pnl = sellProceeds - (pos.quantity * pos.averageEntryPrice)
                        
                        val latestPlayer = traderDao.getTraderById("player")
                        if (latestPlayer != null) {
                            traderDao.updateTrader(latestPlayer.copy(cash = latestPlayer.cash + sellProceeds))
                        }
                        traderDao.deletePosition(pos)

                        val lang = settingsDao.getSettings()?.selectedLanguage ?: "TR"
                        val triggerLabel = if (lang == "TR") {
                            if (isTpTriggered) "KÂR AL (TP)" else "ZARAR DURDUR (SL)"
                        } else {
                            if (isTpTriggered) "TAKE PROFIT (TP)" else "STOP LOSS (SL)"
                        }
                        val spotOrderMessage = if (lang == "TR") {
                            "⚡ $triggerLabel TETİKLENDİ: Spot ${pos.symbol} pozisyonunuzun tamamı (${String.format("%.4f", pos.quantity)} adet) $${String.format("%.2f", currentPrice)} fiyattan satıldı. Gelir: $${String.format("%.2f", sellProceeds)} (Kâr/Zarar: $${String.format("%.2f", pnl)})"
                        } else {
                            "⚡ $triggerLabel TRIGGERED: Your entire Spot ${pos.symbol} position (${String.format("%.4f", pos.quantity)} units) was sold at $${String.format("%.2f", currentPrice)}. Revenue: $${String.format("%.2f", sellProceeds)} (PnL: $${String.format("%.2f", pnl)})"
                        }
                        newsDao.insertNews(
                            NewsLog(
                                timestamp = System.currentTimeMillis(),
                                traderName = if (lang == "TR") "ALGORİTMİK EMİR" else "ALGORITHMIC ORDER",
                                message = spotOrderMessage,
                                symbol = pos.symbol,
                                isSystemNews = true
                            )
                        )
                    }
                }
            }

            // Check Debt Threshold Warnings
            val lang = settingsDao.getSettings()?.selectedLanguage ?: "TR"
            if (player.cash <= -1000.0 && !warn1000Sent) {
                val warningMsg = if (lang == "TR") {
                    "⚠️ UYARI: Net nakit bakiyeniz -1000$'ın altına düştü! Borçlarınızı kapatmak için acilen kârlı işlemler yapmalı veya mini işlerde çalışmalısınız."
                } else {
                    "⚠️ WARNING: Your net cash balance has dropped below -$1000! You must urgently make profitable trades or work in mini-jobs to cover your debts."
                }
                newsDao.insertNews(
                    NewsLog(
                        timestamp = System.currentTimeMillis(),
                        traderName = if (lang == "TR") "FİNANSAL DANIŞMAN" else "FINANCIAL ADVISOR",
                        message = warningMsg,
                        symbol = "PORTFOLIO",
                        isSystemNews = true
                    )
                )
                warn1000Sent = true
            } else if (player.cash > -1000.0) {
                warn1000Sent = false
            }

            if (player.cash <= -2000.0 && !warn2000Sent) {
                val criticalMsg = if (lang == "TR") {
                    "🚨 KRİTİK UYARI: Nakit borcunuz -2000$'ı aştı! Açlık sınırındasınız. Bakiye -3000$'a ulaşırsa borçlarınız sizi aç ve hasta bırakacak, hayati risk oluşacaktır!"
                } else {
                    "🚨 CRITICAL WARNING: Your cash debt has exceeded -$2000! You are near the hunger limit. If the balance reaches -$3000, your debts will leave you hungry and sick, creating a fatal risk!"
                }
                newsDao.insertNews(
                    NewsLog(
                        timestamp = System.currentTimeMillis(),
                        traderName = if (lang == "TR") "BANKA HUKUK DEPT" else "BANK LEGAL DEPT",
                        message = criticalMsg,
                        symbol = "PORTFOLIO",
                        isSystemNews = true
                    )
                )
                warn2000Sent = true
            } else if (player.cash > -2000.0) {
                warn2000Sent = false
            }
        }

        // 3. Perform AI Traders Trade Decisions
        val updatedTraders = mutableListOf<Trader>()
        val positionsToInsert = mutableListOf<TraderPosition>()
        val positionsToDelete = mutableListOf<TraderPosition>()

        traders.forEach { trader ->
            if (trader.isPlayer) {
                updatedTraders.add(trader)
                return@forEach
            }

            var cash = trader.cash
            val strategy = trader.archetype
            val traderPosList = allPositions[trader.id] ?: emptyList()

            val targetAsset = assets.random()
            val symbol = targetAsset.symbol
            val currentPrice = currentPrices[symbol] ?: targetAsset.startPrice
            val candles = previousCandlesMap[symbol] ?: emptyList()
            val position = traderPosList.firstOrNull { it.symbol == symbol }

            var action = "HOLD"
            val sma10 = calculateSMA(candles, 10)
            val rsi10 = calculateRSI(candles, 10)

            when (strategy) {
                "TREND_FOLLOWER" -> {
                    if (candles.isNotEmpty()) {
                        val lastClose = candles.last().close
                        if (lastClose > sma10 && position == null) {
                            action = "BUY"
                        } else if (lastClose < sma10 && position != null) {
                            action = "SELL"
                        }
                    }
                }
                "CONTRARIAN" -> {
                    if (rsi10 < 30.0 && position == null) {
                        action = "BUY"
                    } else if (rsi10 > 70.0 && position != null) {
                        action = "SELL"
                    }
                }
                "SCALPER" -> {
                    if (position == null) {
                        if (Random.nextDouble() < 0.4) action = "BUY"
                    } else {
                        val profitPct = (currentPrice - position.averageEntryPrice) / position.averageEntryPrice
                        if (profitPct > 0.015 || profitPct < -0.008) {
                            action = "SELL"
                        }
                    }
                }
                "WHALE" -> {
                    if (position == null) {
                        if (Random.nextDouble() < 0.15) action = "BUY"
                    } else {
                        val profitPct = (currentPrice - position.averageEntryPrice) / position.averageEntryPrice
                        if (profitPct > 0.40 || Random.nextDouble() < 0.02) {
                            action = "SELL"
                        }
                    }
                }
                "PANIC_SELLER" -> {
                    if (candles.size >= 2) {
                        val lastCandle = candles.last()
                        val drop = (lastCandle.close - lastCandle.open) / lastCandle.open
                        if (drop < -0.025 && position != null) {
                            action = "SELL"
                        } else if (position == null && Random.nextDouble() < 0.15) {
                            action = "BUY"
                        }
                    } else if (position == null && Random.nextDouble() < 0.1) {
                        action = "BUY"
                    }
                }
                "HODLER" -> {
                    if (position == null) {
                        if (Random.nextDouble() < 0.25) action = "BUY"
                    } else {
                        val profitPct = (currentPrice - position.averageEntryPrice) / position.averageEntryPrice
                        if (profitPct > 0.80) {
                            action = "SELL"
                        }
                    }
                }
                "CHAOS" -> {
                    if (Random.nextDouble() < 0.1) {
                        action = if (position == null) "BUY" else "SELL"
                    }
                }
            }

            if (action == "BUY" && cash > 100.0) {
                val buyFraction = if (strategy == "WHALE") Random.nextDouble(0.5, 0.9) else Random.nextDouble(0.2, 0.6)
                val buyCash = cash * buyFraction
                val qty = buyCash / currentPrice
                if (qty > 0.0) {
                    cash -= buyCash
                    val newPos = if (position != null) {
                        val totalQty = position.quantity + qty
                        val avgEntry = ((position.quantity * position.averageEntryPrice) + buyCash) / totalQty
                        TraderPosition("${trader.id}_$symbol", trader.id, symbol, totalQty, avgEntry)
                    } else {
                        TraderPosition("${trader.id}_$symbol", trader.id, symbol, qty, currentPrice)
                    }
                    positionsToInsert.add(newPos)
                    if (Random.nextDouble() < 0.03) {
                        generateTradeTweet(trader.name, strategy, symbol, "BUY", rsi10, currentPrice)
                    }
                }
            } else if (action == "SELL" && position != null) {
                val sellReturn = position.quantity * currentPrice
                cash += sellReturn
                positionsToDelete.add(position)
                if (Random.nextDouble() < 0.03) {
                    generateTradeTweet(trader.name, strategy, symbol, "SELL", rsi10, currentPrice)
                }
            }

            updatedTraders.add(trader.copy(cash = cash))
        }

        // Apply bulk AI trades
        if (positionsToInsert.isNotEmpty()) {
            positionsToInsert.forEach { traderDao.insertPosition(it) }
        }
        if (positionsToDelete.isNotEmpty()) {
            positionsToDelete.forEach { traderDao.deletePosition(it) }
        }

        // 4. Generate Social News Comments
        generateMarketCommentary(currentPrices, previousCandlesMap)

        // 5. Recalculate ranks and update traders
        val allUpdatedPositions = traderDao.getAllPositions().groupBy { it.traderId }
        val rankedTraders = updatedTraders.map { trader ->
            val posList = allUpdatedPositions[trader.id] ?: emptyList()
            val totalEquity = trader.cash + getTraderPortfolioValue(trader.id, posList, currentPrices)
            Pair(trader, totalEquity)
        }.sortedByDescending { it.second }

        val finalTradersList = rankedTraders.mapIndexed { idx, pair ->
            val finalWinRate = if (pair.first.isPlayer) {
                pair.first.winRate
            } else {
                val delta = if (pair.second > pair.first.initialCapital) 0.05 else -0.05
                (pair.first.winRate + delta).coerceIn(30.0, 95.0)
            }
            pair.first.copy(
                rank = idx + 1,
                winRate = finalWinRate
            )
        }
        traderDao.updateTraders(finalTradersList)

        // LIFE SIMULATION CALENDAR TICK
        val settings = getOrInitSettings()
        var day = settings.gameDayCount + 1
        var month = settings.gameMonthCount

        if (day >= 30) {
            day = 1
            month += 1

            // Monthly billing calculation
            val rent = when (settings.currentHouseId) {
                "kiralik_kotu" -> 150.0
                "kiralik_orta" -> 500.0
                else -> 0.0 // Owned condo or villa
            }
            val bills = when (settings.currentHouseId) {
                "kiralik_kotu" -> 50.0
                "kiralik_orta" -> 150.0
                "satinal_rezidans" -> 250.0
                "satinal_villa" -> 500.0
                else -> 50.0
            }
            val food = when (settings.foodPlanId) {
                1 -> 50.0
                2 -> 200.0
                3 -> 600.0
                else -> 50.0
            }

            val hasMiningRig = settings.furnitureBought.split(",").contains("mining_rig")
            val miningIncome = if (hasMiningRig) 150.0 else 0.0

            val totalExpenses = rent + bills + food - miningIncome

            val p = traderDao.getTraderById("player")
            if (p != null) {
                var newCash = p.cash - totalExpenses
                var houseId = settings.currentHouseId
                var furniture = settings.furnitureBought
                var foodId = settings.foodPlanId

                if (newCash < -1000.0) {
                    // Eviction!
                    newCash = 100.0
                    houseId = "kiralik_kotu"
                    furniture = ""
                    foodId = 1
                    
                    val lang = settings.selectedLanguage
                    val icraMessage = if (lang == "TR") {
                        "🚨 EV BOŞALTMA VE HACİZ ALARMI: Toplam borcunuz $${String.format("%.2f", p.cash - totalExpenses)} seviyesine ulaştı ve faturalarınızı ödeyemediniz! Ev sahibi sizi dışarı attı, tüm mobilyalarınıza el konuldu ve $100 nakit ile en kötü gecekonduya taşınmak zorunda kaldınız!"
                    } else {
                        "🚨 EVICTION AND SEIZURE ALERT: Your total debt reached $${String.format("%.2f", p.cash - totalExpenses)} and you failed to pay your bills! The landlord evicted you, all your furniture was seized, and you were forced to move to the poorest slum with only $100 cash!"
                    }
                    newsDao.insertNews(
                        NewsLog(
                            timestamp = System.currentTimeMillis(),
                            traderName = if (lang == "TR") "SİSTEM (İCRA)" else "SYSTEM (EVICTION)",
                            message = icraMessage,
                            symbol = "GENEL",
                            isSystemNews = true
                        )
                    )
                } else {
                    val lang = settings.selectedLanguage
                    val monthEndMessage = if (lang == "TR") {
                        "📆 Yeni aya geçildi! Kira: $${String.format("%.2f", rent)}, Faturalar: $${String.format("%.2f", bills)}, Yemek: $${String.format("%.2f", food)}.${if (hasMiningRig) " Mining Rig pasif gelir: +$150.00" else ""} Hesabınızdan net $${String.format("%.2f", totalExpenses)} kesildi."
                    } else {
                        "📆 A new month has started! Rent: $${String.format("%.2f", rent)}, Bills: $${String.format("%.2f", bills)}, Food: $${String.format("%.2f", food)}.${if (hasMiningRig) " Mining Rig passive income: +$150.00" else ""} Net -$${String.format("%.2f", totalExpenses)} was deducted from your account."
                    }
                    newsDao.insertNews(
                        NewsLog(
                            timestamp = System.currentTimeMillis(),
                            traderName = if (lang == "TR") "SİSTEM (AY BAŞI)" else "SYSTEM (MONTH END)",
                            message = monthEndMessage,
                            symbol = "GENEL",
                            isSystemNews = true
                        )
                    )
                }
                traderDao.updateTrader(p.copy(cash = newCash))
                updateSettings(
                    settings.copy(
                        gameDayCount = day,
                        gameMonthCount = month,
                        currentHouseId = houseId,
                        furnitureBought = furniture,
                        foodPlanId = foodId
                    )
                )
            }
        } else {
            updateSettings(settings.copy(gameDayCount = day))
        }
    }

    // Manual Trade Execution for User (Spot Trading)
    suspend fun executeUserTrade(
        symbol: String,
        isBuy: Boolean,
        quantity: Double,
        takeProfit: Double = 0.0,
        stopLoss: Double = 0.0
    ) = withContext(Dispatchers.IO) {
        val player = traderDao.getTraderById("player") ?: return@withContext
        val candles = marketDao.getCandlesList(symbol)
        if (candles.isEmpty()) return@withContext
        val currentPrice = candles.last().close

        val positionId = "player_${symbol}_spot"
        val position = traderDao.getPositionById(positionId)

        if (isBuy) {
            // Easy buy auto-clamping: if total cost exceeds cash, clamp to maximum affordable quantity
            var finalQty = quantity
            var totalCost = finalQty * currentPrice
            if (totalCost > player.cash) {
                finalQty = (player.cash / currentPrice).coerceAtLeast(0.0)
                totalCost = finalQty * currentPrice
            }

            if (finalQty > 0.0001) {
                val newCash = player.cash - totalCost
                val newPos = if (position != null) {
                    val newQty = position.quantity + finalQty
                    val avgPrice = ((position.quantity * position.averageEntryPrice) + totalCost) / newQty
                    TraderPosition(
                        id = positionId,
                        traderId = "player",
                        symbol = symbol,
                        quantity = newQty,
                        averageEntryPrice = avgPrice,
                        takeProfitPrice = takeProfit,
                        stopLossPrice = stopLoss
                    )
                } else {
                    TraderPosition(
                        id = positionId,
                        traderId = "player",
                        symbol = symbol,
                        quantity = finalQty,
                        averageEntryPrice = currentPrice,
                        takeProfitPrice = takeProfit,
                        stopLossPrice = stopLoss
                    )
                }
                traderDao.updateTrader(player.copy(cash = newCash))
                traderDao.insertPosition(newPos)

                newsDao.insertNews(
                    NewsLog(
                        timestamp = System.currentTimeMillis(),
                        traderName = "Siz (Kullanıcı)",
                        message = "$symbol varlığından Spot olarak ${String.format("%.4f", finalQty)} adet aldınız. Birim Fiyat: $${String.format("%.2f", currentPrice)}",
                        symbol = symbol,
                        isSystemNews = false
                    )
                )
            }
        } else {
            if (position != null && position.quantity > 0.0) {
                // Easy sell auto-clamping: if requested qty exceeds owned qty, clamp to owned qty
                val finalQty = quantity.coerceAtMost(position.quantity)
                if (finalQty > 0.0001) {
                    val sellProceeds = finalQty * currentPrice
                    val newCash = player.cash + sellProceeds
                    val remainingQty = position.quantity - finalQty

                    if (remainingQty <= 0.0001) {
                        traderDao.deletePosition(position)
                    } else {
                        traderDao.insertPosition(
                            position.copy(
                                quantity = remainingQty,
                                takeProfitPrice = takeProfit,
                                stopLossPrice = stopLoss
                            )
                        )
                    }

                    // Update Player Win Rate based on profitability
                    val profit = sellProceeds - (finalQty * position.averageEntryPrice)
                    val newWinRate = if (profit > 0) {
                        (player.winRate * 0.9 + 10.0).coerceIn(0.0, 100.0)
                    } else {
                        (player.winRate * 0.9).coerceIn(0.0, 100.0)
                    }

                    traderDao.updateTrader(player.copy(cash = newCash, winRate = newWinRate))

                    newsDao.insertNews(
                        NewsLog(
                            timestamp = System.currentTimeMillis(),
                            traderName = "Siz (Kullanıcı)",
                            message = "$symbol Spot varlığınızdan ${String.format("%.4f", finalQty)} adet sattınız. Gelir: $${String.format("%.2f", sellProceeds)} (Kâr/Zarar: $${String.format("%.2f", profit)})",
                            symbol = symbol,
                            isSystemNews = false
                        )
                    )
                }
            }
        }
    }

    // Manual Leverage Trade Execution for User (LONG / SHORT)
    suspend fun executeUserLeverageTrade(
        symbol: String,
        isLong: Boolean,      // true = LONG, false = SHORT
        marginAmount: Double, // The margin value committed (drawn from player's available cash)
        leverage: Int,        // Leverage multiplier (e.g., 2, 5, 10, 50, 100)
        takeProfit: Double = 0.0,
        stopLoss: Double = 0.0
    ) = withContext(Dispatchers.IO) {
        val player = traderDao.getTraderById("player") ?: return@withContext
        if (player.cash < marginAmount || marginAmount <= 0) return@withContext

        val candles = marketDao.getCandlesList(symbol)
        if (candles.isEmpty()) return@withContext
        val currentPrice = candles.last().close

        // Quantity = (Margin * Leverage) / CurrentPrice
        val quantity = (marginAmount * leverage) / currentPrice

        // Liquidation Price formula incorporating maintenance margin fraction (~3% buffer)
        val liquidationPrice = if (isLong) {
            currentPrice * (1.0 - (1.0 / leverage) + 0.03)
        } else {
            currentPrice * (1.0 + (1.0 / leverage) - 0.03)
        }

        val directionLabel = if (isLong) "long" else "short"
        val positionId = "player_${symbol}_leverage_$directionLabel"

        val newPosition = TraderPosition(
            id = positionId,
            traderId = "player",
            symbol = symbol,
            quantity = quantity,
            averageEntryPrice = currentPrice,
            isLeverage = true,
            leverage = leverage,
            isLong = isLong,
            margin = marginAmount,
            liquidationPrice = liquidationPrice,
            takeProfitPrice = takeProfit,
            stopLossPrice = stopLoss
        )

        val newCash = player.cash - marginAmount
        traderDao.updateTrader(player.copy(cash = newCash))
        traderDao.insertPosition(newPosition)

        newsDao.insertNews(
            NewsLog(
                timestamp = System.currentTimeMillis(),
                traderName = "Siz (Kullanıcı)",
                message = "$symbol $leverage Kaldıraçlı ${if (isLong) "LONG" else "SHORT"} pozisyonu açıldı! Teminat: $${String.format("%.2f", marginAmount)}, Likidasyon Fiyatı: $${String.format("%.2f", liquidationPrice)}",
                symbol = symbol,
                isSystemNews = false
            )
        )
    }

    // Close active leverage position manually
    suspend fun closeUserLeveragePosition(positionId: String) = withContext(Dispatchers.IO) {
        val player = traderDao.getTraderById("player") ?: return@withContext
        val position = traderDao.getPositionById(positionId) ?: return@withContext
        val candles = marketDao.getCandlesList(position.symbol)
        if (candles.isEmpty()) return@withContext
        val currentPrice = candles.last().close

        // Calculate leverage PnL
        val priceDiff = if (position.isLong) {
            currentPrice - position.averageEntryPrice
        } else {
            position.averageEntryPrice - currentPrice
        }
        val pnl = priceDiff * position.quantity

        // Margin + PnL returned to player's cash (capped at 0 in case of deficit, though it would usually liquidate first)
        val returnedCash = (position.margin + pnl).coerceAtLeast(0.0)

        val newCash = player.cash + returnedCash
        traderDao.deletePosition(position)

        val isProfit = pnl > 0
        val newWinRate = if (isProfit) {
            (player.winRate * 0.9 + 10.0).coerceIn(0.0, 100.0)
        } else {
            (player.winRate * 0.9).coerceIn(0.0, 100.0)
        }

        traderDao.updateTrader(player.copy(cash = newCash, winRate = newWinRate))

        newsDao.insertNews(
            NewsLog(
                timestamp = System.currentTimeMillis(),
                traderName = "Siz (Kullanıcı)",
                message = "$positionId kaldıraçlı pozisyonunu başarıyla kapattınız. Kâr/Zarar: $${String.format("%.2f", pnl)}, Çekilen Nakit: $${String.format("%.2f", returnedCash)}",
                symbol = position.symbol,
                isSystemNews = false
            )
        )
    }

    // Reset Game Simulation completely
    suspend fun resetSimulation() = withContext(Dispatchers.IO) {
        traderDao.clearAllPositions()
        
        // Remove existing traders
        val allTraders = traderDao.getAllTradersList()
        // Delete traders effectively by overwriting with empty and initializing again
        traderDao.insertTraders(emptyList()) 

        marketDao.clearAllCandles()
        newsDao.clearAllNews()

        // Reset life simulation settings
        val settings = getOrInitSettings()
        updateSettings(
            settings.copy(
                introSeen = false,
                outroSeen = false,
                currentHouseId = "kiralik_kotu",
                ownedCars = "",
                activeCarId = null,
                furnitureBought = "",
                foodPlanId = 1,
                gameDayCount = 1,
                gameMonthCount = 1
            )
        )
        
        // Initialize from scratch
        initializeGameIfNeeded()
    }

    // Calculate sum of assets for trader
    private fun getTraderPortfolioValue(traderId: String, positions: List<TraderPosition>, currentPrices: Map<String, Double>): Double {
        var sum = 0.0
        positions.forEach { pos ->
            val price = currentPrices[pos.symbol] ?: 0.0
            if (pos.isLeverage) {
                // For leverage, equity = committed margin + unrealized PnL
                val diff = if (pos.isLong) (price - pos.averageEntryPrice) else (pos.averageEntryPrice - price)
                val unrealizedPnl = diff * pos.quantity
                sum += (pos.margin + unrealizedPnl).coerceAtLeast(0.0)
            } else {
                sum += pos.quantity * price
            }
        }
        return sum
    }

    // Technical Analysis Indicators Calculations
    fun calculateSMA(candles: List<MarketCandle>, period: Int): Double {
        if (candles.size < period) {
            return if (candles.isEmpty()) 0.0 else candles.map { it.close }.average()
        }
        return candles.takeLast(period).map { it.close }.average()
    }

    fun calculateRSI(candles: List<MarketCandle>, period: Int): Double {
        if (candles.size < period + 1) return 50.0
        
        val closes = candles.takeLast(period + 1).map { it.close }
        var gains = 0.0
        var losses = 0.0

        for (i in 1 until closes.size) {
            val change = closes[i] - closes[i - 1]
            if (change > 0) {
                gains += change
            } else {
                losses += -change
            }
        }

        val avgGain = gains / period
        val avgLoss = losses / period

        if (avgLoss == 0.0) return 100.0
        val rs = avgGain / avgLoss
        return 100.0 - (100.0 / (1.0 + rs))
    }

    private suspend fun generateTradeTweet(traderName: String, strategy: String, symbol: String, type: String, rsi: Double, price: Double) {
        val messages = when (type) {
            "BUY" -> when (strategy) {
                "TREND_FOLLOWER" -> listOf(
                    "$symbol yükseliş trendinde! SMA kırıldı, pozisyon açıyorum. 📈🚀",
                    "Trend dostumuzdur! $symbol grafik harika duruyor, ekleme yaptım."
                )
                "CONTRARIAN" -> listOf(
                    "$symbol RSI değeri $rsi ile aşırı satımda! Buradan geri döner, topluyorum. 💸",
                    "Herkes korkarken al, herkes coşarken sat! RSI dipte, $symbol uzun pozisyon aldım."
                )
                "SCALPER" -> listOf(
                    "Kısa vadeli $symbol fırsatı yakalandı. Küçük kâr için daldım! ⚡",
                    "Hızlı bir scalping turu. Al-sat ekibi iş başında!"
                )
                "WHALE" -> listOf(
                    "$symbol tahtasındaki tüm satışları sildim. Buralar bizim! 🐳💎",
                    "Cüzdanı doldurma vakti geldi. $symbol uzun vadeli portföye eklendi."
                )
                "PANIC_SELLER" -> listOf(
                    "Herkes $symbol konuşuyor, kaçırmamak lazım (FOMO)! Aldık bakalım... 🫣",
                    "Umarım bu sefer tepeden almamışımdır. $symbol aldım."
                )
                "HODLER" -> listOf(
                    "Maliyet düşürme zamanı! $symbol miktarı artırıldı. Ömürlük tutuyorum! 💎🙌",
                    "Yıl sonuna kadar $symbol satmıyorum. Cüzdan kilitli!"
                )
                else -> listOf(
                    "Ufak bir miktar $symbol denemesi. Hadi bakalım! 🎲",
                    "Portföyü çeşitlendirmek iyidir. $symbol sepete girdi."
                )
            }
            else -> when (strategy) {
                "TREND_FOLLOWER" -> listOf(
                    "Destek kırıldı, yükseliş trendi bitti. $symbol elveda. 📉💔",
                    "Trend yön değiştirdi, kârı alıp kenara çekilme zamanı."
                )
                "CONTRARIAN" -> listOf(
                    "RSI değeri $rsi ile şişti (Aşırı Alım). $symbol short/satış pozisyonu. 🛑",
                    "Buralar çok pahalılaştı, tepeden satışlarımı yaptım. Kâr cepte!"
                )
                "SCALPER" -> listOf(
                    "Scalp hedefi tamamlandı. Kârı cebe attım, sıradaki işleme geçiyorum! 🎯💸",
                    "Hızlı kâr realizasyonu. Beklemeye gerek yok."
                )
                "WHALE" -> listOf(
                    "Biraz nakit kraldır. $symbol kâr realizasyonu tahtayı sallayabilir! 🐳💥",
                    "Hafifleme vakti. Büyük satış blokları girildi."
                )
                "PANIC_SELLER" -> listOf(
                    "Bu ne biçim düşüş! $symbol batıyoruz! Hepsini sattım kurtuldum! 😭📉",
                    "Büyük bir dump geliyor, can havliyle kaçtım!"
                )
                "HODLER" -> listOf(
                    "Mecburi satım. Yoksa hayatta vermezdim bu fiyata. 💔💎",
                    "Uzun süredir tuttuğum $symbol portföyünden ufak bir kâr aldım."
                )
                else -> listOf(
                    "Bu fiyattan $symbol satmak mantıklı geldi. Nakitte kalalım.",
                    "İşlem tamamlandı, $symbol pozisyonu başarıyla kapatıldı."
                )
            }
        }

        newsDao.insertNews(
            NewsLog(
                timestamp = System.currentTimeMillis(),
                traderName = traderName,
                message = messages.random(),
                symbol = symbol,
                isSystemNews = false
            )
        )
    }

    private suspend fun generateMarketCommentary(currentPrices: Map<String, Double>, oldCandles: Map<String, List<MarketCandle>>) {
        assets.forEach { asset ->
            val symbol = asset.symbol
            val price = currentPrices[symbol] ?: return@forEach
            val candles = oldCandles[symbol] ?: return@forEach
            if (candles.size < 2) return@forEach

            val lastClose = candles.last().close
            val change = (price - lastClose) / lastClose

            if (Random.nextDouble() < 0.15) {
                val text = when {
                    change > 0.08 -> listOf(
                        "FLAŞ HABER: $symbol fiyatında olağanüstü patlama! Balinalar devrede olabilir! 🚀📈",
                        "ANALİZ: $symbol direnç noktasını parçaladı. Alıcılar çılgınca saldırıyor!"
                    ).random()
                    change < -0.08 -> listOf(
                        "KORKU VE PANİK: $symbol cephesinde büyük çöküş! Sert satış baskısı sürüyor! 📉🩸",
                        "ACİL DURUM: $symbol desteği kırıldı! Likidasyon dalgası tetikleniyor."
                    ).random()
                    change > 0.03 -> listOf(
                        "$symbol son dakikalarda güzel toparladı, yükseliş kanalına girdi. 👍",
                        "Piyasa uzmanları $symbol için olumlu raporlar yayınlıyor."
                    ).random()
                    change < -0.03 -> listOf(
                        "$symbol üzerinde kâr satışları yoğunlaştı. Düzeltme süreci başladı mı? 🤔",
                        "Ayılar $symbol tahtasında kontrolü ele almaya çalışıyor."
                    ).random()
                    else -> null
                }

                if (text != null) {
                    newsDao.insertNews(
                        NewsLog(
                            timestamp = System.currentTimeMillis(),
                            traderName = "SİSTEM / MEDIA",
                            message = text,
                            symbol = symbol,
                            isSystemNews = true
                        )
                    )
                }
            }
        }
    }

    // LIFE SIMULATION REPOSITORY HELPER FUNCTIONS
    suspend fun buyOrRentHouse(houseId: String, price: Double, isPurchase: Boolean) = withContext(Dispatchers.IO) {
        val player = traderDao.getTraderById("player") ?: return@withContext
        val settings = getOrInitSettings()
        if (player.cash >= price) {
            val newCash = player.cash - price
            traderDao.updateTrader(player.copy(cash = newCash))
            updateSettings(settings.copy(currentHouseId = houseId))
            newsDao.insertNews(
                NewsLog(
                    timestamp = System.currentTimeMillis(),
                    traderName = "Siz (Kullanıcı)",
                    message = if (isPurchase) {
                        "Yeni bir ev SATIN ALDINIZ! 🏠 Sınıf atladınız. Fiyat: $${String.format("%.2f", price)}"
                    } else {
                        "Yeni bir kiralık eve taşındınız! Fiyat: $${String.format("%.2f", price)} (Depozito/İlk Kira)"
                    },
                    symbol = "GENEL",
                    isSystemNews = false
                )
            )
        }
    }

    suspend fun buyFurniture(furnitureId: String, name: String, price: Double) = withContext(Dispatchers.IO) {
        val player = traderDao.getTraderById("player") ?: return@withContext
        val settings = getOrInitSettings()
        val currentFurniture = settings.furnitureBought.split(",").filter { it.isNotEmpty() }.toMutableList()
        if (currentFurniture.contains(furnitureId)) return@withContext // Already bought
        
        if (player.cash >= price) {
            val newCash = player.cash - price
            traderDao.updateTrader(player.copy(cash = newCash))
            currentFurniture.add(furnitureId)
            updateSettings(settings.copy(furnitureBought = currentFurniture.joinToString(",")))
            newsDao.insertNews(
                NewsLog(
                    timestamp = System.currentTimeMillis(),
                    traderName = "Siz (Kullanıcı)",
                    message = "Eviniz için '$name' satın aldınız! Fiyat: $${String.format("%.2f", price)} 🛋️✨",
                    symbol = "GENEL",
                    isSystemNews = false
                )
            )
        }
    }

    suspend fun buyCar(carId: String, name: String, price: Double) = withContext(Dispatchers.IO) {
        val player = traderDao.getTraderById("player") ?: return@withContext
        val settings = getOrInitSettings()
        val currentCars = settings.ownedCars.split(",").filter { it.isNotEmpty() }.toMutableList()
        if (currentCars.contains(carId)) return@withContext // Already bought
        
        if (player.cash >= price) {
            val newCash = player.cash - price
            traderDao.updateTrader(player.copy(cash = newCash))
            currentCars.add(carId)
            updateSettings(
                settings.copy(
                    ownedCars = currentCars.joinToString(","),
                    activeCarId = carId
                )
            )
            newsDao.insertNews(
                NewsLog(
                    timestamp = System.currentTimeMillis(),
                    traderName = "Siz (Kullanıcı)",
                    message = "Görkemli bir yeni ARABA satın aldınız! '$name'. Fiyat: $${String.format("%.2f", price)} 🚗💨",
                    symbol = "GENEL",
                    isSystemNews = false
                )
            )
        }
    }

    suspend fun selectActiveCar(carId: String) = withContext(Dispatchers.IO) {
        val settings = getOrInitSettings()
        updateSettings(settings.copy(activeCarId = carId))
    }

    suspend fun changeFoodPlan(foodPlanId: Int, name: String) = withContext(Dispatchers.IO) {
        val settings = getOrInitSettings()
        updateSettings(settings.copy(foodPlanId = foodPlanId))
        newsDao.insertNews(
            NewsLog(
                timestamp = System.currentTimeMillis(),
                traderName = "Siz (Kullanıcı)",
                message = "Yemek planınızı değiştirdiniz: '$name'. 🍽️",
                symbol = "GENEL",
                isSystemNews = false
            )
        )
    }

    suspend fun setIntroSeen() = withContext(Dispatchers.IO) {
        val settings = getOrInitSettings()
        updateSettings(settings.copy(introSeen = true))
    }

    suspend fun setOutroSeen() = withContext(Dispatchers.IO) {
        val settings = getOrInitSettings()
        updateSettings(settings.copy(outroSeen = true))
    }
}
