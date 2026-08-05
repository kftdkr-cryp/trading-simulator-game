package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GameViewModel(
    application: Application,
    private val repository: GameRepository
) : AndroidViewModel(application) {

    val assets = repository.assets

    // Active UI states
    private val _selectedAsset = MutableStateFlow("MKTX")
    val selectedAsset: StateFlow<String> = _selectedAsset.asStateFlow()

    private val _tradeQuantity = MutableStateFlow("10")
    val tradeQuantity: StateFlow<String> = _tradeQuantity.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _tickSpeedMs = MutableStateFlow(3000L) // Normal speed: 3s
    val tickSpeedMs: StateFlow<Long> = _tickSpeedMs.asStateFlow()

    // Chart toggle states
    private val _showSma10 = MutableStateFlow(true)
    val showSma10: StateFlow<Boolean> = _showSma10.asStateFlow()

    private val _showSma20 = MutableStateFlow(false)
    val showSma20: StateFlow<Boolean> = _showSma20.asStateFlow()

    private val _showRsi = MutableStateFlow(true)
    val showRsi: StateFlow<Boolean> = _showRsi.asStateFlow()

    // Selected Trader Detail for overlay dialog
    private val _selectedTraderDetailId = MutableStateFlow<String?>(null)
    val selectedTraderDetailId: StateFlow<String?> = _selectedTraderDetailId.asStateFlow()

    // Simulation running job
    private var simJob: Job? = null

    // Combined/Reactive Data Flows
    val traders: StateFlow<List<Trader>> = repository.tradersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val newsLogs: StateFlow<List<NewsLog>> = repository.newsLogsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playerTraderState: StateFlow<Trader?> = repository.tradersFlow
        .map { list -> list.firstOrNull { it.isPlayer } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val playerPositions: StateFlow<List<TraderPosition>> = repository.getPositionsForTraderFlow("player")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settingsState: StateFlow<GameSettings?> = repository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun earnMiniGameCash(amount: Double, gameName: String) {
        viewModelScope.launch {
            repository.earnMiniGameCash(amount, gameName)
        }
    }

    fun syncGoogleProfile(email: String, name: String, avatarUrl: String) {
        viewModelScope.launch {
            repository.syncGoogleProfile(email, name, avatarUrl)
        }
    }

    fun executeUserLeverageTrade(
        symbol: String,
        isLong: Boolean,
        marginAmount: Double,
        leverage: Int,
        takeProfit: Double = 0.0,
        stopLoss: Double = 0.0
    ) {
        viewModelScope.launch {
            repository.executeUserLeverageTrade(symbol, isLong, marginAmount, leverage, takeProfit, stopLoss)
        }
    }

    fun closeUserLeveragePosition(positionId: String) {
        viewModelScope.launch {
            repository.closeUserLeveragePosition(positionId)
        }
    }

    // LIFE SIMULATION VIEWMODEL FUNCTIONS
    fun buyOrRentHouse(houseId: String, price: Double, isPurchase: Boolean) {
        viewModelScope.launch {
            repository.buyOrRentHouse(houseId, price, isPurchase)
        }
    }

    fun buyFurniture(furnitureId: String, name: String, price: Double) {
        viewModelScope.launch {
            repository.buyFurniture(furnitureId, name, price)
        }
    }

    fun buyCar(carId: String, name: String, price: Double) {
        viewModelScope.launch {
            repository.buyCar(carId, name, price)
        }
    }

    fun selectActiveCar(carId: String) {
        viewModelScope.launch {
            repository.selectActiveCar(carId)
        }
    }

    fun changeFoodPlan(foodPlanId: Int, name: String) {
        viewModelScope.launch {
            repository.changeFoodPlan(foodPlanId, name)
        }
    }

    fun setIntroSeen() {
        viewModelScope.launch {
            repository.setIntroSeen()
        }
    }

    fun setOutroSeen() {
        viewModelScope.launch {
            repository.setOutroSeen()
        }
    }

    fun sellHouse(houseId: String, name: String, refundPrice: Double) {
        viewModelScope.launch {
            repository.sellHouse(houseId, name, refundPrice)
        }
    }

    fun sellFurniture(furnitureId: String, name: String, refundPrice: Double) {
        viewModelScope.launch {
            repository.sellFurniture(furnitureId, name, refundPrice)
        }
    }

    fun sellCar(carId: String, name: String, refundPrice: Double) {
        viewModelScope.launch {
            repository.sellCar(carId, name, refundPrice)
        }
    }

    fun updateLanguage(lang: String) {
        viewModelScope.launch {
            val currentSettings = repository.getOrInitSettings()
            repository.updateSettings(currentSettings.copy(selectedLanguage = lang))
        }
    }

    fun updateDrawings(drawingsJson: String) {
        viewModelScope.launch {
            val currentSettings = repository.getOrInitSettings()
            repository.updateSettings(currentSettings.copy(drawingsJson = drawingsJson))
        }
    }

    fun updateActiveIndicators(indicatorsList: String) {
        viewModelScope.launch {
            val currentSettings = repository.getOrInitSettings()
            repository.updateSettings(currentSettings.copy(activeIndicators = indicatorsList))
        }
    }

    // Fetch candles dynamically for selected asset
    val activeCandles: StateFlow<List<MarketCandle>> = _selectedAsset
        .flatMapLatest { symbol -> repository.getCandlesFlow(symbol) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLatestPrices: StateFlow<Map<String, Double>> = combine(
        repository.getCandlesFlow("MKTX"),
        repository.getCandlesFlow("SOLR"),
        repository.getCandlesFlow("NEOM"),
        repository.getCandlesFlow("VOID")
    ) { mktx, solr, neom, void ->
        mapOf(
            "MKTX" to (mktx.lastOrNull()?.close ?: 100.0),
            "SOLR" to (solr.lastOrNull()?.close ?: 50.0),
            "NEOM" to (neom.lastOrNull()?.close ?: 250.0),
            "VOID" to (void.lastOrNull()?.close ?: 5.0)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), mapOf(
        "MKTX" to 100.0,
        "SOLR" to 50.0,
        "NEOM" to 250.0,
        "VOID" to 5.0
    ))

    init {
        viewModelScope.launch {
            // First time initialization
            repository.initializeGameIfNeeded()
            // Make simulation persistent and active by default
            _isPlaying.value = true
            startSimulationJob()
        }
    }

    fun selectAsset(symbol: String) {
        _selectedAsset.value = symbol
    }

    fun setTradeQuantity(qty: String) {
        _tradeQuantity.value = qty
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            _isPlaying.value = false
            simJob?.cancel()
        } else {
            _isPlaying.value = true
            startSimulationJob()
        }
    }

    fun setTickSpeed(ms: Long) {
        _tickSpeedMs.value = ms
        if (_isPlaying.value) {
            simJob?.cancel()
            startSimulationJob()
        }
    }

    fun stepForward() {
        viewModelScope.launch {
            repository.advanceTurn()
        }
    }

    private fun startSimulationJob() {
        simJob = viewModelScope.launch {
            while (_isPlaying.value) {
                delay(_tickSpeedMs.value)
                repository.advanceTurn()
            }
        }
    }

    // Toggle indicators
    fun toggleSma10() { _showSma10.value = !_showSma10.value }
    fun toggleSma20() { _showSma20.value = !_showSma20.value }
    fun toggleRsi() { _showRsi.value = !_showRsi.value }

    fun showTraderDetail(traderId: String?) {
        _selectedTraderDetailId.value = traderId
    }

    // Trader actions
    fun buyAsset(takeProfit: Double = 0.0, stopLoss: Double = 0.0) {
        val qty = _tradeQuantity.value.toDoubleOrNull() ?: return
        viewModelScope.launch {
            repository.executeUserTrade(_selectedAsset.value, isBuy = true, quantity = qty, takeProfit = takeProfit, stopLoss = stopLoss)
        }
    }

    fun sellAsset(takeProfit: Double = 0.0, stopLoss: Double = 0.0) {
        val qty = _tradeQuantity.value.toDoubleOrNull() ?: return
        viewModelScope.launch {
            repository.executeUserTrade(_selectedAsset.value, isBuy = false, quantity = qty, takeProfit = takeProfit, stopLoss = stopLoss)
        }
    }

    fun resetGame() {
        viewModelScope.launch {
            if (_isPlaying.value) {
                togglePlayPause()
            }
            repository.resetSimulation()
        }
    }

    // Copy trade actions
    fun toggleCopyTrading(traderId: String) {
        viewModelScope.launch {
            val player = playerTraderState.value ?: return@launch
            if (player.copyingTraderId == traderId) {
                // Stop copying
                repository.executeUserTrade("MKTX", isBuy = false, quantity = 0.0) // Simply clean up state
                val updatedPlayer = player.copy(copyingTraderId = null)
                // Save
                val db = AppDatabase.getDatabase(getApplication())
                db.traderDao().updateTrader(updatedPlayer)
                
                db.newsDao().insertNews(
                    NewsLog(
                        timestamp = System.currentTimeMillis(),
                        traderName = "SİSTEM",
                        message = "Kopya işlemi durduruldu. Ayrılan fonlar portföyünüze iade edildi.",
                        symbol = "GENEL",
                        isSystemNews = true
                    )
                )
            } else {
                // Start copying this trader
                val db = AppDatabase.getDatabase(getApplication())
                val targetTrader = db.traderDao().getTraderById(traderId) ?: return@launch
                val updatedPlayer = player.copy(copyingTraderId = traderId)
                db.traderDao().updateTrader(updatedPlayer)

                db.newsDao().insertNews(
                    NewsLog(
                        timestamp = System.currentTimeMillis(),
                        traderName = "SİSTEM",
                        message = "${targetTrader.name} adlı uzman yatırımcıyı kopyalamaya başladınız! Onun yaptığı işlemler orantılı olarak hesabınıza yansıyacak.",
                        symbol = "GENEL",
                        isSystemNews = true
                    )
                )
            }
        }
    }

    // Helper functions for UI (SMA and RSI arrays)
    fun getSmaValues(candles: List<MarketCandle>, period: Int): List<Float?> {
        val smaList = mutableListOf<Float?>()
        for (i in candles.indices) {
            if (i < period - 1) {
                smaList.add(null)
            } else {
                val sub = candles.subList(i - period + 1, i + 1)
                val avg = sub.map { it.close }.average().toFloat()
                smaList.add(avg)
            }
        }
        return smaList
    }

    fun getRsiValues(candles: List<MarketCandle>, period: Int): List<Float?> {
        val rsiList = mutableListOf<Float?>()
        if (candles.size < period + 1) {
            return List(candles.size) { 50f }
        }

        // Initialize first elements to neutral
        for (i in 0..period) {
            rsiList.add(50f)
        }

        for (i in (period + 1) until candles.size) {
            val subCandles = candles.subList(i - period, i + 1)
            val closes = subCandles.map { it.close }
            var gains = 0f
            var losses = 0f

            for (j in 1 until closes.size) {
                val diff = (closes[j] - closes[j - 1]).toFloat()
                if (diff > 0) {
                    gains += diff
                } else {
                    losses += -diff
                }
            }

            val avgGain = gains / period
            val avgLoss = losses / period
            if (avgLoss == 0f) {
                rsiList.add(100f)
            } else {
                val rs = avgGain / avgLoss
                val rsiVal = 100f - (100f / (1f + rs))
                rsiList.add(rsiVal)
            }
        }
        return rsiList
    }

    fun runAiAnalysis(symbol: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            kotlinx.coroutines.delay(1200)
            val candles = repository.getCandlesList(symbol)
            if (candles.isEmpty()) {
                onSuccess("Analiz için yetersiz veri var.")
                return@launch
            }
            // exactly 65% probability of correct trend prediction
            val isCorrect = kotlin.random.Random.nextDouble() < 0.65
            val sma10 = candles.takeLast(10).map { it.close }.average()
            val lastPrice = candles.last().close
            val actualTrendIsUp = lastPrice > sma10
            
            val predictedUp = if (isCorrect) actualTrendIsUp else !actualTrendIsUp
            
            val text = if (predictedUp) {
                "🟢 YAPAY ZEKA SİNYALİ: AL (BUY)\n" +
                "Analiz: SMA ve RSI göstergeleri dip seviyelerden dönüşü doğruluyor. Bir sonraki fiyat hareketi %65 ihtimalle YUKARI yönlü olacaktır."
            } else {
                "🔴 YAPAY ZEKA SİNYALİ: SAT (SELL)\n" +
                "Analiz: Göreceli Güç Endeksi (RSI) aşırı alım bölgesinde tepe yaptı. Bir sonraki fiyat hareketi %65 ihtimalle AŞAĞI yönlü olacaktır."
            }
            onSuccess(text)
        }
    }

    override fun onCleared() {
        super.onCleared()
        simJob?.cancel()
    }
}

// Custom ViewModel Factory
class GameViewModelFactory(
    private val application: Application,
    private val repository: GameRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
