package com.example.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import kotlin.random.Random

class GameRepository(private val db: AppDatabase) {

    private val marketDao = db.marketDao()
    private val traderDao = db.traderDao()
    private val newsDao = db.newsDao()
    private val settingsDao = db.settingsDao()
    private val userAccountDao = db.userAccountDao()

    val tradersFlow: Flow<List<Trader>> = traderDao.getAllTradersFlow()
    val newsLogsFlow: Flow<List<NewsLog>> = newsDao.getNewsFlow()
    val settingsFlow: Flow<GameSettings?> = settingsDao.getSettingsFlow()

    fun getCandlesFlow(symbol: String): Flow<List<MarketCandle>> = marketDao.getCandlesFlow(symbol)
    fun getPositionsForTraderFlow(traderId: String): Flow<List<TraderPosition>> = traderDao.getPositionsForTraderFlow(traderId)

    val assets = listOf(
        AssetInfo("MKTX", "Piyasa Endeksi", "Stabil, dengeli küresel borsa endeksi", 100.0, 0.00005, 0.035, false),
        AssetInfo("SOLR", "Solar Enerji", "Yüksek volatilite, haber odaklı temiz enerji hissesi", 50.0, 0.0002, 0.085, true),
        AssetInfo("NEOM", "Teknoloji Devi", "İstikrarlı büyüyen, premium teknoloji şirketi", 250.0, 0.00008, 0.028, true),
        AssetInfo("VOID", "Meme Coin", "Aşırı spekülatif, çılgın fiyat hareketleri olan kripto para", 5.0, 0.0003, 0.20, false)
    )

    data class AssetInfo(
        val symbol: String,
        val displayName: String,
        val description: String,
        val startPrice: Double,
        val drift: Double,
        val volatility: Double,
        val isPredictable: Boolean = false
    )

    // ────────────────────────────────────────────────────────────────────────
    // AUTH - Local username/password (max 2 accounts per device)
    // ────────────────────────────────────────────────────────────────────────

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    suspend fun getAccountCount(): Int = withContext(Dispatchers.IO) {
        userAccountDao.countAccounts()
    }

    /**
     * Register new account. Returns null on success, error string on failure.
     */
    suspend fun registerAccount(username: String, password: String, displayName: String): String? = withContext(Dispatchers.IO) {
        val trimmedUsername = username.trim()
        if (trimmedUsername.length < 3) return@withContext "Kullanıcı adı en az 3 karakter olmalı"
        if (password.length < 4) return@withContext "Şifre en az 4 karakter olmalı"
        if (userAccountDao.countAccounts() >= 2) return@withContext "Bu cihazda en fazla 2 hesap açılabilir"
        val existing = userAccountDao.getByUsername(trimmedUsername)
        if (existing != null) return@withContext "Bu kullanıcı adı zaten alınmış"

        try {
            userAccountDao.insertAccount(
                UserAccount(
                    username = trimmedUsername,
                    passwordHash = hashPassword(password),
                    displayName = displayName.trim().ifEmpty { trimmedUsername }
                )
            )
        } catch (e: Exception) {
            return@withContext "Hesap oluşturulamadı: ${e.message}"
        }
        null // success
    }

    /**
     * Login. Returns null on success, error string on failure.
     */
    suspend fun loginAccount(username: String, password: String): String? = withContext(Dispatchers.IO) {
        val account = userAccountDao.getByUsername(username.trim())
            ?: return@withContext "Kullanıcı adı bulunamadı"
        if (account.passwordHash != hashPassword(password)) return@withContext "Şifre yanlış"

        val settings = getOrInitSettings()
        updateSettings(settings.copy(loggedInUsername = account.username))

        // Ensure player trader exists
        val player = traderDao.getTraderById("player")
        if (player == null) {
            initializeGameIfNeeded()
        } else {
            // Update player display name to match account
            traderDao.updateTrader(player.copy(name = account.displayName))
        }
        null
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        val settings = getOrInitSettings()
        updateSettings(settings.copy(loggedInUsername = null))
    }

    // ────────────────────────────────────────────────────────────────────────
    // SETTINGS
    // ────────────────────────────────────────────────────────────────────────

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

    // ────────────────────────────────────────────────────────────────────────
    // GAME INIT / RESET
    // ────────────────────────────────────────────────────────────────────────

    suspend fun initializeGameIfNeeded() = withContext(Dispatchers.IO) {
        val existingTraders = traderDao.getAllTradersList()
        if (existingTraders.isEmpty()) {
            Log.d("GameRepository", "Database is empty. Initializing simulation...")
            val generatedTraders = generate200Traders()
            traderDao.insertTraders(generatedTraders)

            val initialCandles = mutableListOf<MarketCandle>()
            assets.forEach { asset ->
                var currentPrice = asset.startPrice
                var timestamp = System.currentTimeMillis() - (50 * 60000)
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
                    timestamp += 60000
                }
            }
            marketDao.insertCandles(initialCandles)

            newsDao.insertNews(
                NewsLog(
                    timestamp = System.currentTimeMillis(),
                    traderName = "SİSTEM",
                    message = "Yeni bir oyuncu, borsaya $0 nakit ile katıldı. Bu oyuncunun bir gün sıralamada zirveye çıkacağı öngörülüyor...",
                    symbol = "GENEL",
                    isSystemNews = true
                )
            )
        }
    }

    suspend fun resetSimulation() = withContext(Dispatchers.IO) {
        traderDao.clearAllPositions()
        traderDao.insertTraders(emptyList())
        marketDao.clearAllCandles()
        newsDao.clearAllNews()

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
                gameMonthCount = 1,
                ownedProperties = "",
                listedProperties = ""
            )
        )

        initializeGameIfNeeded()
    }

    // ────────────────────────────────────────────────────────────────────────
    // TRADING
    // ────────────────────────────────────────────────────────────────────────

    suspend fun earnMiniGameCash(amount: Double, gameName: String) = withContext(Dispatchers.IO) {
        val player = traderDao.getTraderById("player") ?: return@withContext
        val newCash = player.cash + amount
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

    suspend fun advanceTurn() = withContext(Dispatchers.IO) {
        val traders = traderDao.getAllTradersList().toMutableList()
        val allPositionsList = traderDao.getAllPositions()
        val allPositions = allPositionsList.groupBy { it.traderId }

        val currentPrices = mutableMapOf<String, Double>()
        val previousCandlesMap = mutableMapOf<String, List<MarketCandle>>()

        assets.forEach { asset ->
            val candles = marketDao.getCandlesList(asset.symbol)
            if (candles.isEmpty()) return@forEach
            previousCandlesMap[asset.symbol] = candles

            val lastCandle = candles.last()
            val recentCandles = if (candles.size >= 5) candles.takeLast(5) else candles
            val consecutiveGreen = recentCandles.takeLast(3).count { it.close > it.open }
            val consecutiveRed = recentCandles.takeLast(3).count { it.close < it.open }

            val sma10 = calculateSMA(candles, 10)
            val sma20 = calculateSMA(candles, 20)
            val rsi10 = calculateRSI(candles, 10)

            var indDir = 0
            if (lastCandle.close > sma10) indDir += 1 else indDir -= 1
            if (lastCandle.close > sma20) indDir += 1 else indDir -= 1
            if (sma10 > sma20) indDir += 1 else indDir -= 1
            if (rsi10 < 30.0) indDir += 2
            if (rsi10 > 70.0) indDir -= 2
            if (rsi10 < 40.0) indDir += 1
            if (rsi10 > 60.0) indDir -= 1
            if (consecutiveGreen >= 3) indDir -= 2
            if (consecutiveRed >= 3) indDir += 2

            val indicatorDirection = when {
                indDir > 2 -> 1.0
                indDir < -2 -> -1.0
                indDir > 0 -> 0.5
                indDir < 0 -> -0.5
                else -> 0.0
            }

            val totalSymbolPositions = allPositionsList.count { it.symbol == asset.symbol }
            val strategyDirection = if (totalSymbolPositions > 25) 0.5 else -0.5

            var predictedMovement = 0.0
            val indicatorProb = if (asset.isPredictable) 0.75 else 0.55
            val strategyProb = if (asset.isPredictable) 0.50 else 0.40
            if (Random.nextDouble() < indicatorProb) predictedMovement += indicatorDirection * asset.volatility * 0.6
            if (Random.nextDouble() < strategyProb) predictedMovement += strategyDirection * asset.volatility * 0.5

            val randomEventRoll = Random.nextDouble()
            var shockFactor = 0.0
            when {
                randomEventRoll < 0.02 -> shockFactor = -asset.volatility * 3.0
                randomEventRoll < 0.04 -> shockFactor = asset.volatility * 3.0
                randomEventRoll < 0.08 -> shockFactor = -asset.volatility * 1.5
                randomEventRoll < 0.12 -> shockFactor = asset.volatility * 1.5
            }

            val priceDistanceFromSMA = (lastCandle.close - sma10) / sma10
            val meanReversion = -priceDistanceFromSMA * 0.1 * asset.volatility

            val changePercent = asset.drift + predictedMovement + shockFactor + meanReversion
            val open = lastCandle.close
            val close = (open * (1.0 + changePercent)).coerceAtLeast(0.01)
            val high = (maxOf(open, close) * (1.0 + Random.nextDouble(0.0, asset.volatility / 2))).coerceAtLeast(0.01)
            val low = (minOf(open, close) * (1.0 - Random.nextDouble(0.0, asset.volatility / 2))).coerceAtLeast(0.01)

            marketDao.insertCandles(listOf(
                MarketCandle(
                    symbol = asset.symbol,
                    timestamp = System.currentTimeMillis(),
                    open = open, high = high, low = low, close = close,
                    volume = Random.nextDouble(1000.0, 20000.0)
                )
            ))
            currentPrices[asset.symbol] = close
        }

        // Check player leverage liquidations / TP / SL
        val playerPositions = allPositions["player"] ?: emptyList()
        val positionsToInsert = mutableListOf<TraderPosition>()
        val positionsToDelete = mutableListOf<TraderPosition>()

        playerPositions.filter { it.isLeverage }.forEach { pos ->
            val currentPrice = currentPrices[pos.symbol] ?: return@forEach
            var closeReason: String? = null
            var closeEmoji = "🔔"

            // Check TP
            if (pos.takeProfitPrice != null) {
                if (pos.isLong && currentPrice >= pos.takeProfitPrice) { closeReason = "TP Hedefi Ulaşıldı"; closeEmoji = "🎯" }
                else if (!pos.isLong && currentPrice <= pos.takeProfitPrice) { closeReason = "TP Hedefi Ulaşıldı"; closeEmoji = "🎯" }
            }
            // Check SL
            if (closeReason == null && pos.stopLossPrice != null) {
                if (pos.isLong && currentPrice <= pos.stopLossPrice) { closeReason = "Stop-Loss Tetiklendi"; closeEmoji = "🛡️" }
                else if (!pos.isLong && currentPrice >= pos.stopLossPrice) { closeReason = "Stop-Loss Tetiklendi"; closeEmoji = "🛡️" }
            }
            // Check Liquidation
            if (closeReason == null) {
                if (pos.isLong && currentPrice <= pos.liquidationPrice) { closeReason = "LİKİDASYON"; closeEmoji = "💀" }
                else if (!pos.isLong && currentPrice >= pos.liquidationPrice) { closeReason = "LİKİDASYON"; closeEmoji = "💀" }
            }

            if (closeReason != null) {
                val priceDiff = if (pos.isLong) currentPrice - pos.averageEntryPrice else pos.averageEntryPrice - currentPrice
                val pnl = priceDiff * pos.quantity
                val returnAmount = if (closeReason == "LİKİDASYON") 0.0 else (pos.margin + pnl).coerceAtLeast(0.0)
                val player2 = traderDao.getTraderById("player")
                if (player2 != null) {
                    traderDao.updateTrader(player2.copy(cash = player2.cash + returnAmount))
                }
                positionsToDelete.add(pos)
                newsDao.insertNews(NewsLog(
                    timestamp = System.currentTimeMillis(),
                    traderName = "Siz (Kullanıcı)",
                    message = "$closeEmoji $closeReason: ${pos.symbol} pozisyonu $${String.format("%.2f", currentPrice)} fiyatından kapatıldı! PnL: $${String.format("%.2f", pnl)}",
                    symbol = pos.symbol,
                    isSystemNews = true
                ))
            }
        }

        // AI Trader decisions
        val updatedTraders = traders.map { trader ->
            if (trader.isPlayer) return@map trader
            var cash = trader.cash
            val positions = (allPositions[trader.id] ?: emptyList()).toMutableList()
            val strategy = trader.archetype

            assets.forEach { asset ->
                val symbol = asset.symbol
                val currentPrice = currentPrices[symbol] ?: asset.startPrice
                val candles = previousCandlesMap[symbol] ?: return@forEach
                val rsi10 = calculateRSI(candles, 10)
                val sma10 = calculateSMA(candles, 10)
                val position = positions.firstOrNull { it.symbol == symbol && !it.isLeverage }

                val shouldBuy: Boolean
                val shouldSell: Boolean

                when (strategy) {
                    "TREND_FOLLOWER" -> {
                        shouldBuy = currentPrice > sma10 && rsi10 < 70.0 && cash > 100
                        shouldSell = position != null && (currentPrice < sma10 || rsi10 > 80.0)
                    }
                    "CONTRARIAN" -> {
                        shouldBuy = rsi10 < 30.0 && cash > 100
                        shouldSell = position != null && rsi10 > 70.0
                    }
                    "SCALPER" -> {
                        shouldBuy = Random.nextDouble() < 0.3 && cash > 50
                        shouldSell = position != null && Random.nextDouble() < 0.3
                    }
                    "WHALE" -> {
                        shouldBuy = currentPrice < sma10 * 0.97 && cash > 500
                        shouldSell = position != null && currentPrice > sma10 * 1.05
                    }
                    "PANIC_SELLER" -> {
                        shouldBuy = rsi10 < 40.0 && Random.nextDouble() < 0.2 && cash > 100
                        shouldSell = position != null && (currentPrice < position.averageEntryPrice * 0.97 || rsi10 > 65.0)
                    }
                    "HODLER" -> {
                        shouldBuy = cash > 200 && Random.nextDouble() < 0.1
                        shouldSell = false
                    }
                    else -> {
                        shouldBuy = Random.nextDouble() < 0.15 && cash > 100
                        shouldSell = position != null && Random.nextDouble() < 0.15
                    }
                }

                if (shouldBuy && cash > 50) {
                    val buyCash = cash * Random.nextDouble(0.05, 0.25)
                    val qty = buyCash / currentPrice
                    cash -= buyCash
                    val existing = positions.firstOrNull { it.symbol == symbol && !it.isLeverage }
                    if (existing != null) {
                        val newQty = existing.quantity + qty
                        val newAvg = ((existing.quantity * existing.averageEntryPrice) + buyCash) / newQty
                        positions.removeAll { it.symbol == symbol && !it.isLeverage }
                        positions.add(existing.copy(quantity = newQty, averageEntryPrice = newAvg))
                        positionsToInsert.add(existing.copy(quantity = newQty, averageEntryPrice = newAvg))
                    } else {
                        val newPos = TraderPosition("${trader.id}_$symbol", trader.id, symbol, qty, currentPrice)
                        positions.add(newPos)
                        positionsToInsert.add(newPos)
                    }
                    if (Random.nextDouble() < 0.15) generateTradeTweet(trader.name, strategy, symbol, "BUY", rsi10, currentPrice)
                } else if (shouldSell && position != null) {
                    val sellReturn = position.quantity * currentPrice
                    cash += sellReturn
                    positions.removeAll { it.symbol == symbol && !it.isLeverage }
                    positionsToDelete.add(position)
                    if (Random.nextDouble() < 0.15) generateTradeTweet(trader.name, strategy, symbol, "SELL", rsi10, currentPrice)
                }
            }

            trader.copy(cash = cash)
        }

        if (positionsToInsert.isNotEmpty()) positionsToInsert.forEach { traderDao.insertPosition(it) }
        if (positionsToDelete.isNotEmpty()) positionsToDelete.forEach { traderDao.deletePosition(it) }

        generateMarketCommentary(currentPrices, previousCandlesMap)

        val allUpdatedPositions = traderDao.getAllPositions().groupBy { it.traderId }
        val rankedTraders = updatedTraders.map { trader ->
            val posList = allUpdatedPositions[trader.id] ?: emptyList()
            val totalEquity = trader.cash + getTraderPortfolioValue(trader.id, posList, currentPrices)
            Pair(trader, totalEquity)
        }.sortedByDescending { it.second }

        val finalTradersList = rankedTraders.mapIndexed { idx, pair ->
            val finalWinRate = if (pair.first.isPlayer) pair.first.winRate
            else {
                val delta = if (pair.second > pair.first.initialCapital) 0.05 else -0.05
                (pair.first.winRate + delta).coerceIn(30.0, 95.0)
            }
            pair.first.copy(rank = idx + 1, winRate = finalWinRate)
        }
        traderDao.updateTraders(finalTradersList)

        // LIFE SIMULATION CALENDAR TICK
        val settings = getOrInitSettings()
        var day = settings.gameDayCount + 1
        var month = settings.gameMonthCount

        if (day >= 30) {
            day = 1
            month += 1

            val rent = when (settings.currentHouseId) {
                "kiralik_kotu" -> 150.0
                "kiralik_orta" -> 500.0
                else -> 0.0
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

            // Sadece mining rig pasif gelir - başka pasif gelir yok
            val hasMiningRig = settings.furnitureBought.split(",").contains("mining_rig")
            val miningIncome = if (hasMiningRig) 150.0 else 0.0

            // Property rental income - satılık değil, sadece kira geliri olmaz (mülkler değer için)
            val totalExpenses = rent + bills + food - miningIncome

            val p = traderDao.getTraderById("player")
            if (p != null) {
                var newCash = p.cash - totalExpenses
                var houseId = settings.currentHouseId
                var furniture = settings.furnitureBought
                var foodId = settings.foodPlanId

                // İflas uyarıları
                val playerBeforeDeduction = p.cash
                if (playerBeforeDeduction <= -1000.0 && playerBeforeDeduction > -2000.0) {
                    newsDao.insertNews(NewsLog(
                        timestamp = System.currentTimeMillis(),
                        traderName = "⚠️ UYARI / İFLAS RİSKİ",
                        message = "⚠️ DİKKAT! Bakiyeniz -$${String.format("%.0f", -playerBeforeDeduction)} seviyesine düştü! İflas eşiği olan -$3000'a yaklaşıyorsunuz. Varlık satın veya trading yaparak nakit kazanın!",
                        symbol = "GENEL",
                        isSystemNews = true
                    ))
                } else if (playerBeforeDeduction <= -2000.0 && playerBeforeDeduction > -3000.0) {
                    newsDao.insertNews(NewsLog(
                        timestamp = System.currentTimeMillis(),
                        traderName = "🚨 KRİTİK UYARI",
                        message = "🚨 ACİL! Bakiyeniz -$${String.format("%.0f", -playerBeforeDeduction)}! İFLASA ÇOK YAKIN! -$3000'a düşerseniz oyun biter. Hemen harekete geçin!",
                        symbol = "GENEL",
                        isSystemNews = true
                    ))
                }

                if (newCash < -3000.0) {
                    newCash = 100.0
                    houseId = "kiralik_kotu"
                    furniture = ""
                    foodId = 1
                    newsDao.insertNews(NewsLog(
                        timestamp = System.currentTimeMillis(),
                        traderName = "SİSTEM / İCRA",
                        message = "🚨 İFLAS! Toplam borcunuz -$3000 sınırını aştı! Tüm varlıklarınıza el konuldu. $100 nakit ile en kötü gecekonduya taşındınız.",
                        symbol = "GENEL",
                        isSystemNews = true
                    ))
                } else {
                    newsDao.insertNews(NewsLog(
                        timestamp = System.currentTimeMillis(),
                        traderName = "SİSTEM / AY BAŞI",
                        message = "📆 Yeni aya geçildi! Kira: $${String.format("%.2f", rent)}, Faturalar: $${String.format("%.2f", bills)}, Yemek: $${String.format("%.2f", food)}.${if (hasMiningRig) " Mining Rig pasif gelir: +$150.00" else ""} Hesabınızdan net $${String.format("%.2f", totalExpenses)} kesildi.",
                        symbol = "GENEL",
                        isSystemNews = true
                    ))
                }

                traderDao.updateTrader(p.copy(cash = newCash))
                updateSettings(settings.copy(
                    gameDayCount = day,
                    gameMonthCount = month,
                    currentHouseId = houseId,
                    furnitureBought = furniture,
                    foodPlanId = foodId
                ))
            }
        } else {
            updateSettings(settings.copy(gameDayCount = day))
        }

        // --- EVERY-TURN GAME OVER CHECK (BELOW -$3000) ---
        val finalPlayer = traderDao.getTraderById("player")
        if (finalPlayer != null && finalPlayer.cash < -3000.0) {
            traderDao.updateTrader(finalPlayer.copy(cash = 100.0))
            val currentSettings = getOrInitSettings()
            updateSettings(currentSettings.copy(
                currentHouseId = "kiralik_kotu",
                furnitureBought = "",
                foodPlanId = 1
            ))
            newsDao.insertNews(NewsLog(
                timestamp = System.currentTimeMillis(),
                traderName = "SİSTEM / İCRA",
                message = "🚨 İFLAS! Toplam borcunuz -$3000 sınırını aştı! Tüm varlıklarınıza el konuldu. $100 nakit ile en kötü gecekonduya taşındınız.",
                symbol = "GENEL",
                isSystemNews = true
            ))
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // MANUAL TRADING
    // ────────────────────────────────────────────────────────────────────────

    suspend fun executeUserTrade(symbol: String, isBuy: Boolean, quantity: Double) = withContext(Dispatchers.IO) {
        val player = traderDao.getTraderById("player") ?: return@withContext
        val candles = marketDao.getCandlesList(symbol)
        if (candles.isEmpty()) return@withContext
        val currentPrice = candles.last().close
        val totalCost = quantity * currentPrice
        val positionId = "player_${symbol}_spot"
        val position = traderDao.getPositionById(positionId)

        if (isBuy) {
            if (player.cash >= totalCost && quantity > 0) {
                val newCash = player.cash - totalCost
                val newPos = if (position != null) {
                    val newQty = position.quantity + quantity
                    val avgPrice = ((position.quantity * position.averageEntryPrice) + totalCost) / newQty
                    TraderPosition(positionId, "player", symbol, newQty, avgPrice)
                } else {
                    TraderPosition(positionId, "player", symbol, quantity, currentPrice)
                }
                traderDao.updateTrader(player.copy(cash = newCash))
                traderDao.insertPosition(newPos)
                newsDao.insertNews(NewsLog(
                    timestamp = System.currentTimeMillis(),
                    traderName = "Siz (Kullanıcı)",
                    message = "$symbol varlığından Spot olarak $quantity adet aldınız. Birim Fiyat: $${String.format("%.2f", currentPrice)}",
                    symbol = symbol, isSystemNews = false
                ))
            }
        } else {
            if (position != null && position.quantity >= quantity && quantity > 0) {
                val sellProceeds = quantity * currentPrice
                val newCash = player.cash + sellProceeds
                val remainingQty = position.quantity - quantity
                if (remainingQty <= 0.0001) traderDao.deletePosition(position)
                else traderDao.insertPosition(position.copy(quantity = remainingQty))
                val profit = sellProceeds - (quantity * position.averageEntryPrice)
                val newWinRate = if (profit > 0) (player.winRate * 0.9 + 10.0).coerceIn(0.0, 100.0)
                                 else (player.winRate * 0.9).coerceIn(0.0, 100.0)
                traderDao.updateTrader(player.copy(cash = newCash, winRate = newWinRate))
                newsDao.insertNews(NewsLog(
                    timestamp = System.currentTimeMillis(),
                    traderName = "Siz (Kullanıcı)",
                    message = "$symbol Spot varlığınızdan $quantity adet sattınız. Gelir: $${String.format("%.2f", sellProceeds)} (Kâr/Zarar: $${String.format("%.2f", profit)})",
                    symbol = symbol, isSystemNews = false
                ))
            }
        }
    }

    suspend fun executeUserLeverageTrade(symbol: String, isLong: Boolean, marginAmount: Double, leverage: Int,
                                         takeProfitPercent: Double? = null, stopLossPercent: Double? = null) = withContext(Dispatchers.IO) {
        val player = traderDao.getTraderById("player") ?: return@withContext
        if (player.cash < marginAmount || marginAmount <= 0) return@withContext
        val candles = marketDao.getCandlesList(symbol)
        if (candles.isEmpty()) return@withContext
        val currentPrice = candles.last().close
        val quantity = (marginAmount * leverage) / currentPrice
        val liquidationPrice = if (isLong) currentPrice * (1.0 - (1.0 / leverage) + 0.03)
                               else currentPrice * (1.0 + (1.0 / leverage) - 0.03)
        val tpPrice = if (takeProfitPercent != null) {
            if (isLong) currentPrice * (1.0 + takeProfitPercent / 100.0)
            else currentPrice * (1.0 - takeProfitPercent / 100.0)
        } else null
        val slPrice = if (stopLossPercent != null) {
            if (isLong) currentPrice * (1.0 - stopLossPercent / 100.0)
            else currentPrice * (1.0 + stopLossPercent / 100.0)
        } else null
        val directionLabel = if (isLong) "long" else "short"
        val positionId = "player_${symbol}_leverage_$directionLabel"
        val newPosition = TraderPosition(
            id = positionId, traderId = "player", symbol = symbol, quantity = quantity,
            averageEntryPrice = currentPrice, isLeverage = true, leverage = leverage,
            isLong = isLong, margin = marginAmount, liquidationPrice = liquidationPrice,
            takeProfitPrice = tpPrice, stopLossPrice = slPrice
        )
        traderDao.updateTrader(player.copy(cash = player.cash - marginAmount))
        traderDao.insertPosition(newPosition)
        var message = "$symbol $leverage Kaldıraçlı ${if (isLong) "LONG" else "SHORT"} pozisyonu açıldı! Teminat: $${String.format("%.2f", marginAmount)}"
        if (tpPrice != null) message += ", TP: $${String.format("%.2f", tpPrice)}"
        if (slPrice != null) message += ", SL: $${String.format("%.2f", slPrice)}"
        newsDao.insertNews(NewsLog(timestamp = System.currentTimeMillis(), traderName = "Siz (Kullanıcı)", message = message, symbol = symbol, isSystemNews = false))
    }

    suspend fun updatePositionTpSl(positionId: String, tpPercent: Double?, slPercent: Double?) = withContext(Dispatchers.IO) {
        val pos = traderDao.getPositionById(positionId) ?: return@withContext
        val tpPrice = if (tpPercent != null) {
            if (pos.isLong) pos.averageEntryPrice * (1.0 + tpPercent / 100.0)
            else pos.averageEntryPrice * (1.0 - tpPercent / 100.0)
        } else null
        val slPrice = if (slPercent != null) {
            if (pos.isLong) pos.averageEntryPrice * (1.0 - slPercent / 100.0)
            else pos.averageEntryPrice * (1.0 + slPercent / 100.0)
        } else null
        traderDao.insertPosition(pos.copy(takeProfitPrice = tpPrice, stopLossPrice = slPrice))
    }

    suspend fun closeUserLeveragePosition(positionId: String) = withContext(Dispatchers.IO) {
        val pos = traderDao.getPositionById(positionId) ?: return@withContext
        val candles = marketDao.getCandlesList(pos.symbol)
        val currentPrice = candles.lastOrNull()?.close ?: pos.averageEntryPrice
        val priceDiff = if (pos.isLong) currentPrice - pos.averageEntryPrice else pos.averageEntryPrice - currentPrice
        val pnl = priceDiff * pos.quantity
        val returnAmount = (pos.margin + pnl).coerceAtLeast(0.0)
        val player = traderDao.getTraderById("player")
        if (player != null) traderDao.updateTrader(player.copy(cash = player.cash + returnAmount))
        traderDao.deletePosition(pos)
        newsDao.insertNews(NewsLog(
            timestamp = System.currentTimeMillis(), traderName = "Siz (Kullanıcı)",
            message = "${pos.symbol} kaldıraçlı pozisyon manuel kapatıldı. PnL: $${String.format("%.2f", pnl)} | Geri Alınan: $${String.format("%.2f", returnAmount)}",
            symbol = pos.symbol, isSystemNews = false
        ))
    }

    // ────────────────────────────────────────────────────────────────────────
    // LIFE SIMULATION
    // ────────────────────────────────────────────────────────────────────────

    suspend fun buyOrRentHouse(houseId: String, price: Double, isPurchase: Boolean) = withContext(Dispatchers.IO) {
        val player = traderDao.getTraderById("player") ?: return@withContext
        val settings = getOrInitSettings()
        if (player.cash >= price) {
            traderDao.updateTrader(player.copy(cash = player.cash - price))
            updateSettings(settings.copy(currentHouseId = houseId))
            newsDao.insertNews(NewsLog(
                timestamp = System.currentTimeMillis(), traderName = "Siz (Kullanıcı)",
                message = if (isPurchase) "Yeni bir ev SATIN ALDINIZ! 🏠 Fiyat: $${String.format("%.2f", price)}"
                          else "Yeni kiralık eve taşındınız! Fiyat: $${String.format("%.2f", price)}",
                symbol = "GENEL", isSystemNews = false
            ))
        }
    }

    suspend fun buyFurniture(furnitureId: String, name: String, price: Double) = withContext(Dispatchers.IO) {
        val player = traderDao.getTraderById("player") ?: return@withContext
        val settings = getOrInitSettings()
        val currentFurniture = settings.furnitureBought.split(",").filter { it.isNotEmpty() }.toMutableList()
        if (currentFurniture.contains(furnitureId)) return@withContext
        if (player.cash >= price) {
            traderDao.updateTrader(player.copy(cash = player.cash - price))
            currentFurniture.add(furnitureId)
            updateSettings(settings.copy(furnitureBought = currentFurniture.joinToString(",")))
            newsDao.insertNews(NewsLog(
                timestamp = System.currentTimeMillis(), traderName = "Siz (Kullanıcı)",
                message = "Eviniz için '$name' satın aldınız! $${String.format("%.2f", price)} 🛋️",
                symbol = "GENEL", isSystemNews = false
            ))
        }
    }

    suspend fun sellFurniture(furnitureId: String, name: String, salePrice: Double) = withContext(Dispatchers.IO) {
        val player = traderDao.getTraderById("player") ?: return@withContext
        val settings = getOrInitSettings()
        val currentFurniture = settings.furnitureBought.split(",").filter { it.isNotEmpty() }.toMutableList()
        if (!currentFurniture.contains(furnitureId)) return@withContext
        currentFurniture.remove(furnitureId)
        traderDao.updateTrader(player.copy(cash = player.cash + salePrice))
        updateSettings(settings.copy(furnitureBought = currentFurniture.joinToString(",")))
        newsDao.insertNews(NewsLog(
            timestamp = System.currentTimeMillis(), traderName = "Siz (Kullanıcı)",
            message = "'$name' eşyasını $${String.format("%.2f", salePrice)} karşılığında sattınız 💰",
            symbol = "GENEL", isSystemNews = false
        ))
    }

    suspend fun buyCar(carId: String, name: String, price: Double) = withContext(Dispatchers.IO) {
        val player = traderDao.getTraderById("player") ?: return@withContext
        val settings = getOrInitSettings()
        val currentCars = settings.ownedCars.split(",").filter { it.isNotEmpty() }.toMutableList()
        if (currentCars.contains(carId)) return@withContext
        if (player.cash >= price) {
            traderDao.updateTrader(player.copy(cash = player.cash - price))
            currentCars.add(carId)
            updateSettings(settings.copy(ownedCars = currentCars.joinToString(","), activeCarId = carId))
            newsDao.insertNews(NewsLog(
                timestamp = System.currentTimeMillis(), traderName = "Siz (Kullanıcı)",
                message = "Yeni ARABA satın aldınız! '$name'. Fiyat: $${String.format("%.2f", price)} 🚗💨",
                symbol = "GENEL", isSystemNews = false
            ))
        }
    }

    suspend fun sellCar(carId: String, name: String, salePrice: Double) = withContext(Dispatchers.IO) {
        val player = traderDao.getTraderById("player") ?: return@withContext
        val settings = getOrInitSettings()
        val currentCars = settings.ownedCars.split(",").filter { it.isNotEmpty() }.toMutableList()
        if (!currentCars.contains(carId)) return@withContext
        currentCars.remove(carId)
        val newActiveCarId = if (settings.activeCarId == carId) currentCars.lastOrNull() else settings.activeCarId
        traderDao.updateTrader(player.copy(cash = player.cash + salePrice))
        updateSettings(settings.copy(ownedCars = currentCars.joinToString(","), activeCarId = newActiveCarId))
        newsDao.insertNews(NewsLog(
            timestamp = System.currentTimeMillis(), traderName = "Siz (Kullanıcı)",
            message = "'$name' aracınızı $${String.format("%.2f", salePrice)} karşılığında sattınız 💸",
            symbol = "GENEL", isSystemNews = false
        ))
    }

    suspend fun selectActiveCar(carId: String) = withContext(Dispatchers.IO) {
        val settings = getOrInitSettings()
        updateSettings(settings.copy(activeCarId = carId))
    }

    suspend fun changeFoodPlan(foodPlanId: Int, name: String) = withContext(Dispatchers.IO) {
        val settings = getOrInitSettings()
        updateSettings(settings.copy(foodPlanId = foodPlanId))
        newsDao.insertNews(NewsLog(
            timestamp = System.currentTimeMillis(), traderName = "Siz (Kullanıcı)",
            message = "Yemek planınız değiştirildi: '$name'. 🍽️",
            symbol = "GENEL", isSystemNews = false
        ))
    }

    // ────────────────────────────────────────────────────────────────────────
    // PROPERTIES (Mülkler)
    // ────────────────────────────────────────────────────────────────────────

    suspend fun buyProperty(propId: String, name: String, price: Double) = withContext(Dispatchers.IO) {
        val player = traderDao.getTraderById("player") ?: return@withContext
        val settings = getOrInitSettings()
        val owned = settings.ownedProperties.split(",").filter { it.isNotEmpty() }.toMutableList()
        if (owned.contains(propId)) return@withContext
        if (player.cash >= price) {
            traderDao.updateTrader(player.copy(cash = player.cash - price))
            owned.add(propId)
            updateSettings(settings.copy(ownedProperties = owned.joinToString(",")))
            newsDao.insertNews(NewsLog(
                timestamp = System.currentTimeMillis(), traderName = "Siz (Kullanıcı)",
                message = "🏢 Yeni mülk satın aldınız: '$name'. Değer: $${String.format("%.2f", price)}",
                symbol = "GENEL", isSystemNews = false
            ))
        }
    }

    private val _offersState = MutableStateFlow<List<GameOffer>>(emptyList())
    val offersState: StateFlow<List<GameOffer>> = _offersState.asStateFlow()

    fun getItemMarketValue(itemId: String): Double {
        return when (itemId) {
            // Properties
            "studio", "prop_studio" -> 15000.0
            "apartment", "prop_apartment" -> 45000.0
            "penthouse", "prop_penthouse" -> 250000.0
            "office", "prop_office" -> 80000.0
            "warehouse", "prop_warehouse" -> 60000.0
            "shop", "prop_shop" -> 35000.0
            "resort", "prop_resort" -> 500000.0

            // Cars
            "rust_bucket" -> 800.0
            "tofas_sahin" -> 2500.0
            "bmw_m3" -> 25000.0
            "tesla_model_s" -> 75000.0
            "ferrari" -> 250000.0

            // Houses
            "satinal_rezidans" -> 15000.0
            "satinal_villa" -> 100000.0
            else -> 10000.0
        }
    }

    fun getItemNameTR(itemId: String): String {
        return when (itemId) {
            "studio", "prop_studio" -> "Stüdyo Daire"
            "apartment", "prop_apartment" -> "Normal Daire"
            "penthouse", "prop_penthouse" -> "Çatı Katı"
            "office", "prop_office" -> "Ofis Katı"
            "warehouse", "prop_warehouse" -> "Depo/Fabrika"
            "shop", "prop_shop" -> "Dükkan/Mağaza"
            "resort", "prop_resort" -> "Tatil Köyü"
            "rust_bucket" -> "Paslı Tofaş (Murat 124)"
            "tofas_sahin" -> "Modifiyeli Tofaş Şahin"
            "bmw_m3" -> "BMW M3"
            "tesla_model_s" -> "Tesla Model S"
            "ferrari" -> "Kırmızı Ferrari F40"
            "satinal_rezidans" -> "Lüks Rezidans"
            "satinal_villa" -> "Ultra Malikane"
            else -> itemId
        }
    }

    fun getItemNameEN(itemId: String): String {
        return when (itemId) {
            "studio", "prop_studio" -> "Studio Apt"
            "apartment", "prop_apartment" -> "Apartment"
            "penthouse", "prop_penthouse" -> "Penthouse"
            "office", "prop_office" -> "Office Floor"
            "warehouse", "prop_warehouse" -> "Warehouse"
            "shop", "prop_shop" -> "Shop"
            "resort", "prop_resort" -> "Resort"
            "rust_bucket" -> "Rust Murat 124"
            "tofas_sahin" -> "Tuned Tofas Sahin"
            "bmw_m3" -> "BMW M3"
            "tesla_model_s" -> "Tesla Model S"
            "ferrari" -> "Red Ferrari F40"
            "satinal_rezidans" -> "Luxury Condo"
            "satinal_villa" -> "Ultra Villa"
            else -> itemId
        }
    }

    fun getItemType(itemId: String): String {
        return when (itemId) {
            "studio", "apartment", "penthouse", "office", "warehouse", "shop", "resort",
            "prop_studio", "prop_apartment", "prop_penthouse", "prop_office", "prop_warehouse", "prop_shop", "prop_resort" -> "PROPERTY"
            "rust_bucket", "tofas_sahin", "bmw_m3", "tesla_model_s", "ferrari" -> "CAR"
            "satinal_rezidans", "satinal_villa" -> "HOUSE"
            else -> "PROPERTY"
        }
    }

    suspend fun listPropertyForSale(propId: String, askingPrice: Double) = withContext(Dispatchers.IO) {
        val settings = getOrInitSettings()
        val listed = settings.listedProperties.split(",").filter { it.isNotEmpty() }.toMutableList()
        // Remove any existing listing for this propId
        listed.removeAll { it.startsWith("$propId:") }
        listed.add("$propId:${askingPrice.toLong()}")
        updateSettings(settings.copy(listedProperties = listed.joinToString(",")))
    }

    suspend fun cancelPropertyListing(propId: String) = withContext(Dispatchers.IO) {
        val settings = getOrInitSettings()
        val listed = settings.listedProperties.split(",").filter { it.isNotEmpty() }.toMutableList()
        listed.removeAll { it.startsWith("$propId:") }
        updateSettings(settings.copy(listedProperties = listed.joinToString(",")))
        _offersState.value = _offersState.value.filter { it.itemId != propId }
    }

    /** Called periodically - simulates buyers making offers at fair prices */
    suspend fun checkPropertySales() = withContext(Dispatchers.IO) {
        val settings = getOrInitSettings()
        if (settings.listedProperties.isEmpty()) return@withContext
        val listed = settings.listedProperties.split(",").filter { it.isNotEmpty() }
        if (listed.isEmpty()) return@withContext

        // 35% chance to generate an offer on a random listed item each tick
        if (Random.nextDouble() >= 0.35) return@withContext

        val listedEntry = listed.random()
        val parts = listedEntry.split(":")
        if (parts.isEmpty()) return@withContext
        val itemId = parts[0]
        val askingPrice = parts.getOrNull(1)?.toLongOrNull()?.toDouble() ?: return@withContext

        // Check if there is already an offer for this item
        val currentOffers = _offersState.value
        if (currentOffers.any { it.itemId == itemId }) return@withContext

        // Generate offer price between 85% and 120% of market value
        val marketPrice = getItemMarketValue(itemId)
        val offerPercent = Random.nextDouble(0.85, 1.20)
        val offerPrice = Math.round(marketPrice * offerPercent).toDouble()

        val buyerNames = listOf(
            "Milyarder Kerem", "Yatırımcı Cengiz", "Koleksiyoner Can", "Emlakçı Selim",
            "Trader Arda", "Yazılımcı Mert", "Memur Ahmet", "Galeri Sahibi Burak",
            "Crypto Whale 🐳", "Doktor Elif", "Avukat Serkan", "Fon Yöneticisi Derin"
        )
        val buyer = buyerNames.random()

        val newOffer = GameOffer(
            id = java.util.UUID.randomUUID().toString(),
            itemId = itemId,
            itemType = getItemType(itemId),
            itemNameTR = getItemNameTR(itemId),
            itemNameEN = getItemNameEN(itemId),
            buyerName = buyer,
            offerPrice = offerPrice,
            marketPrice = marketPrice
        )

        _offersState.value = currentOffers + newOffer

        newsDao.insertNews(NewsLog(
            timestamp = System.currentTimeMillis(),
            traderName = "SİSTEM / TEKLİF",
            message = "🏷️ Gelen Teklif: '${newOffer.itemNameTR}' için $buyer tarafından $${String.format("%.0f", offerPrice)} teklif verildi! (İlan fiyatı: $${String.format("%.0f", askingPrice)})",
            symbol = "GENEL",
            isSystemNews = true
        ))
    }

    suspend fun acceptOffer(offerId: String) = withContext(Dispatchers.IO) {
        val offer = _offersState.value.find { it.id == offerId } ?: return@withContext
        val player = traderDao.getTraderById("player") ?: return@withContext
        val settings = getOrInitSettings()

        // 1. Remove item from owned list
        val ownedCarsList = settings.ownedCars.split(",").filter { it.isNotEmpty() }.toMutableList()
        val ownedPropsList = settings.ownedProperties.split(",").filter { it.isNotEmpty() }.toMutableList()
        var houseId = settings.currentHouseId
        var activeCarId = settings.activeCarId

        when (offer.itemType) {
            "CAR" -> {
                ownedCarsList.remove(offer.itemId)
                if (activeCarId == offer.itemId) {
                    activeCarId = ownedCarsList.lastOrNull()
                }
            }
            "PROPERTY" -> {
                ownedPropsList.remove(offer.itemId)
                // support prop_ prefixed IDs if any
                ownedPropsList.remove("prop_${offer.itemId}")
                ownedPropsList.remove(offer.itemId.removePrefix("prop_"))
            }
            "HOUSE" -> {
                if (houseId == offer.itemId) {
                    houseId = "kiralik_kotu" // default slums
                }
            }
        }

        // 2. Remove from listings
        val listedList = settings.listedProperties.split(",").filter { it.isNotEmpty() }.toMutableList()
        listedList.removeAll { it.startsWith("${offer.itemId}:") }

        // 3. Award cash
        val newCash = player.cash + offer.offerPrice

        // 4. Update Database
        traderDao.updateTrader(player.copy(cash = newCash))
        updateSettings(settings.copy(
            ownedCars = ownedCarsList.joinToString(","),
            ownedProperties = ownedPropsList.joinToString(","),
            currentHouseId = houseId,
            activeCarId = activeCarId,
            listedProperties = listedList.joinToString(",")
        ))

        // 5. Clean up offers for this item
        _offersState.value = _offersState.value.filter { it.itemId != offer.itemId }

        newsDao.insertNews(NewsLog(
            timestamp = System.currentTimeMillis(),
            traderName = "SİSTEM / SATIŞ",
            message = "🏢✅ Satış gerçekleşti! '${offer.itemNameTR}' mülkünüz $${String.format("%.0f", offer.offerPrice)} karşılığında ${offer.buyerName} kişisine satıldı.",
            symbol = "GENEL",
            isSystemNews = false
        ))
    }

    suspend fun rejectOffer(offerId: String) = withContext(Dispatchers.IO) {
        val offer = _offersState.value.find { it.id == offerId } ?: return@withContext
        _offersState.value = _offersState.value.filter { it.id != offerId }

        newsDao.insertNews(NewsLog(
            timestamp = System.currentTimeMillis(),
            traderName = "SİSTEM / TEKLİF",
            message = "❌ '${offer.itemNameTR}' için ${offer.buyerName} tarafından yapılan $${String.format("%.0f", offer.offerPrice)} tutarındaki teklifi reddettiniz.",
            symbol = "GENEL",
            isSystemNews = false
        ))
    }

    // ────────────────────────────────────────────────────────────────────────
    // STORY FLAGS
    // ────────────────────────────────────────────────────────────────────────

    suspend fun setIntroSeen() = withContext(Dispatchers.IO) {
        updateSettings(getOrInitSettings().copy(introSeen = true))
    }

    suspend fun setOutroSeen() = withContext(Dispatchers.IO) {
        updateSettings(getOrInitSettings().copy(outroSeen = true))
    }

    // ────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ────────────────────────────────────────────────────────────────────────

    private fun getTraderPortfolioValue(traderId: String, positions: List<TraderPosition>, currentPrices: Map<String, Double>): Double {
        var sum = 0.0
        positions.forEach { pos ->
            val price = currentPrices[pos.symbol] ?: 0.0
            if (pos.isLeverage) {
                val diff = if (pos.isLong) (price - pos.averageEntryPrice) else (pos.averageEntryPrice - price)
                sum += pos.margin + (diff * pos.quantity)
            } else {
                sum += pos.quantity * price
            }
        }
        return sum
    }

    fun calculateSMA(candles: List<MarketCandle>, period: Int): Double {
        if (candles.size < period) return candles.lastOrNull()?.close ?: 0.0
        return candles.takeLast(period).map { it.close }.average()
    }

    fun calculateRSI(candles: List<MarketCandle>, period: Int): Double {
        if (candles.size < period + 1) return 50.0
        val closes = candles.takeLast(period + 1).map { it.close }
        var gains = 0.0; var losses = 0.0
        for (i in 1 until closes.size) {
            val change = closes[i] - closes[i - 1]
            if (change > 0) gains += change else losses += -change
        }
        val avgGain = gains / period; val avgLoss = losses / period
        if (avgLoss == 0.0) return 100.0
        val rs = avgGain / avgLoss
        return 100.0 - (100.0 / (1.0 + rs))
    }

    private suspend fun generateTradeTweet(traderName: String, strategy: String, symbol: String, type: String, rsi: Double, price: Double) {
        val messages = when (type) {
            "BUY" -> listOf(
                "$symbol aldım! ${if (rsi < 40) "RSI dipte" else "Trend güçlü"}. 📈🚀",
                "Pozisyon açıldı: $symbol @ $${String.format("%.2f", price)} 💎",
                "$symbol fırsatı kaçmaz! Girdim. ⚡"
            )
            else -> listOf(
                "$symbol sattım. Kâr cepte! 💸",
                "Pozisyon kapatıldı: $symbol @ $${String.format("%.2f", price)} 📉",
                "$symbol çıktım, bekliyorum. 🎯"
            )
        }
        newsDao.insertNews(NewsLog(timestamp = System.currentTimeMillis(), traderName = traderName, message = messages.random(), symbol = symbol, isSystemNews = false))
    }

    private suspend fun generateMarketCommentary(currentPrices: Map<String, Double>, oldCandles: Map<String, List<MarketCandle>>) {
        assets.forEach { asset ->
            val oldPrice = oldCandles[asset.symbol]?.lastOrNull()?.close ?: return@forEach
            val newPrice = currentPrices[asset.symbol] ?: return@forEach
            val changePercent = ((newPrice - oldPrice) / oldPrice) * 100
            if (Math.abs(changePercent) > 3.0) {
                val direction = if (changePercent > 0) "⬆️ YUKARI" else "⬇️ AŞAĞI"
                val msg = "${asset.displayName} (${ asset.symbol}) $direction! Değişim: ${String.format("%.2f", changePercent)}% @ $${String.format("%.2f", newPrice)}"
                newsDao.insertNews(NewsLog(timestamp = System.currentTimeMillis(), traderName = "SİSTEM / BORSA", message = msg, symbol = asset.symbol, isSystemNews = true))
            }
        }
    }

    private fun generate200Traders(): List<Trader> {
        val archetypes = listOf("TREND_FOLLOWER", "CONTRARIAN", "SCALPER", "WHALE", "CHAOS", "HODLER", "PANIC_SELLER")
        val traders = mutableListOf<Trader>()
        val firstNames = listOf("Ahmet", "Mert", "Emre", "Burak", "Arda", "Deniz", "Kemal", "Ali", "Can", "Taha",
            "James", "Liam", "Noah", "Lucas", "Oliver", "Wang", "Liu", "Zhang", "Li", "Chen")
        val lastNames = listOf("Yılmaz", "Kaya", "Demir", "Şahin", "Çelik", "Aydın", "Arslan", "Koç",
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller")
        val suffixes = listOf("Trader", "Pro", "Wolf", "Bull", "Bear", "King", "Guru", "98", "X", "Elite")
        val uniqueNames = mutableSetOf<String>()
        var count = 1

        while (count <= 200) {
            val formatType = Random.nextInt(3)
            val name = when (formatType) {
                0 -> "${firstNames.random()} ${lastNames.random()}"
                1 -> "${lastNames.random()}${suffixes.random()}"
                else -> "${firstNames.random()}${Random.nextInt(10, 9999)}"
            }.also { uniqueNames.add(it) }

            val arch = if (count <= 5) "WHALE" else archetypes.random()
            val capital = when (arch) {
                "WHALE" -> Random.nextDouble(50000.0, 200000.0)
                "HODLER" -> Random.nextDouble(5000.0, 30000.0)
                else -> Random.nextDouble(500.0, 15000.0)
            }
            traders.add(Trader(id = "ai_$count", name = name, archetype = arch, cash = capital, initialCapital = capital,
                winRate = Random.nextDouble(35.0, 85.0), isPlayer = false, rank = count + 1))
            count++
        }

        traders.add(Trader(id = "player", name = "Sen", archetype = "PLAYER", cash = 0.0,
            initialCapital = 0.0, winRate = 50.0, isPlayer = true, rank = 201))
        return traders
    }
}
