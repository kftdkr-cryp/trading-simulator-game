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

    private val _selectedAsset = MutableStateFlow("MKTX")
    val selectedAsset: StateFlow<String> = _selectedAsset.asStateFlow()

    private val _tradeQuantity = MutableStateFlow("10")
    val tradeQuantity: StateFlow<String> = _tradeQuantity.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _tickSpeedMs = MutableStateFlow(3000L)
    val tickSpeedMs: StateFlow<Long> = _tickSpeedMs.asStateFlow()

    private val _showSma10 = MutableStateFlow(true)
    val showSma10: StateFlow<Boolean> = _showSma10.asStateFlow()

    private val _showSma20 = MutableStateFlow(false)
    val showSma20: StateFlow<Boolean> = _showSma20.asStateFlow()

    private val _showRsi = MutableStateFlow(true)
    val showRsi: StateFlow<Boolean> = _showRsi.asStateFlow()

    private val _selectedTraderDetailId = MutableStateFlow<String?>(null)
    val selectedTraderDetailId: StateFlow<String?> = _selectedTraderDetailId.asStateFlow()

    // Auth error message
    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _accountCount = MutableStateFlow(0)
    val accountCount: StateFlow<Int> = _accountCount.asStateFlow()

    private var simJob: Job? = null

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
        "MKTX" to 100.0, "SOLR" to 50.0, "NEOM" to 250.0, "VOID" to 5.0
    ))

    init {
        viewModelScope.launch {
            repository.getOrInitSettings()
            repository.initializeGameIfNeeded()
            _accountCount.value = repository.getAccountCount()
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // AUTH
    // ────────────────────────────────────────────────────────────────────────

    fun register(username: String, password: String, displayName: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            val error = repository.registerAccount(username, password, displayName)
            _accountCount.value = repository.getAccountCount()
            onResult(error)
        }
    }

    fun login(username: String, password: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            val error = repository.loginAccount(username, password)
            onResult(error)
        }
    }

    fun logout() {
        viewModelScope.launch {
            if (_isPlaying.value) togglePlayPause()
            repository.logout()
        }
    }

    fun clearAuthError() { _authError.value = null }

    // ────────────────────────────────────────────────────────────────────────
    // LIFE SIMULATION
    // ────────────────────────────────────────────────────────────────────────

    fun earnMiniGameCash(amount: Double, gameName: String) {
        viewModelScope.launch { repository.earnMiniGameCash(amount, gameName) }
    }

    fun buyOrRentHouse(houseId: String, price: Double, isPurchase: Boolean) {
        viewModelScope.launch { repository.buyOrRentHouse(houseId, price, isPurchase) }
    }

    fun buyFurniture(furnitureId: String, name: String, price: Double) {
        viewModelScope.launch { repository.buyFurniture(furnitureId, name, price) }
    }

    fun sellFurniture(furnitureId: String, name: String, salePrice: Double) {
        viewModelScope.launch { repository.sellFurniture(furnitureId, name, salePrice) }
    }

    fun buyCar(carId: String, name: String, price: Double) {
        viewModelScope.launch { repository.buyCar(carId, name, price) }
    }

    fun sellCar(carId: String, name: String, salePrice: Double) {
        viewModelScope.launch { repository.sellCar(carId, name, salePrice) }
    }

    fun selectActiveCar(carId: String) {
        viewModelScope.launch { repository.selectActiveCar(carId) }
    }

    fun changeFoodPlan(foodPlanId: Int, name: String) {
        viewModelScope.launch { repository.changeFoodPlan(foodPlanId, name) }
    }

    // Properties
    fun buyProperty(propId: String, name: String, price: Double) {
        viewModelScope.launch { repository.buyProperty(propId, name, price) }
    }

    fun listPropertyForSale(propId: String, askingPrice: Double) {
        viewModelScope.launch { repository.listPropertyForSale(propId, askingPrice) }
    }

    fun cancelPropertyListing(propId: String) {
        viewModelScope.launch { repository.cancelPropertyListing(propId) }
    }

    // ────────────────────────────────────────────────────────────────────────
    // TRADING
    // ────────────────────────────────────────────────────────────────────────

    fun executeUserLeverageTrade(symbol: String, isLong: Boolean, marginAmount: Double, leverage: Int,
                                  takeProfitPercent: Double?, stopLossPercent: Double?) {
        viewModelScope.launch { repository.executeUserLeverageTrade(symbol, isLong, marginAmount, leverage, takeProfitPercent, stopLossPercent) }
    }

    fun closeUserLeveragePosition(positionId: String) {
        viewModelScope.launch { repository.closeUserLeveragePosition(positionId) }
    }

    fun updatePositionTpSl(positionId: String, tpPercent: Double?, slPercent: Double?) {
        viewModelScope.launch { repository.updatePositionTpSl(positionId, tpPercent, slPercent) }
    }

    // ────────────────────────────────────────────────────────────────────────
    // SETTINGS
    // ────────────────────────────────────────────────────────────────────────

    fun updateLanguage(lang: String) {
        viewModelScope.launch {
            val currentSettings = repository.getOrInitSettings()
            repository.updateSettings(currentSettings.copy(selectedLanguage = lang))
        }
    }

    fun updateTheme(isDark: Boolean) {
        viewModelScope.launch {
            val currentSettings = repository.getOrInitSettings()
            repository.updateSettings(currentSettings.copy(isDarkTheme = isDark))
        }
    }

    fun updateMarketSpeed(speed: Long) {
        viewModelScope.launch {
            val currentSettings = repository.getOrInitSettings()
            repository.updateSettings(currentSettings.copy(marketSpeed = speed))
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

    // ────────────────────────────────────────────────────────────────────────
    // GAME CONTROL
    // ────────────────────────────────────────────────────────────────────────

    fun selectAsset(symbol: String) { _selectedAsset.value = symbol }
    fun setTradeQuantity(qty: String) { _tradeQuantity.value = qty }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            simJob?.cancel()
            _isPlaying.value = false
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
        viewModelScope.launch { repository.advanceTurn() }
    }

    private fun startSimulationJob() {
        simJob = viewModelScope.launch {
            while (true) {
                delay(_tickSpeedMs.value)
                repository.advanceTurn()
                repository.checkPropertySales()
            }
        }
    }

    fun toggleSma10() { _showSma10.value = !_showSma10.value }
    fun toggleSma20() { _showSma20.value = !_showSma20.value }
    fun toggleRsi() { _showRsi.value = !_showRsi.value }
    fun showTraderDetail(traderId: String?) { _selectedTraderDetailId.value = traderId }

    fun buyAsset() {
        viewModelScope.launch {
            val qty = _tradeQuantity.value.toDoubleOrNull() ?: return@launch
            repository.executeUserTrade(_selectedAsset.value, isBuy = true, quantity = qty)
        }
    }

    fun sellAsset() {
        viewModelScope.launch {
            val qty = _tradeQuantity.value.toDoubleOrNull() ?: return@launch
            repository.executeUserTrade(_selectedAsset.value, isBuy = false, quantity = qty)
        }
    }

    fun resetGame() {
        viewModelScope.launch {
            if (_isPlaying.value) togglePlayPause()
            repository.resetSimulation()
        }
    }

    fun toggleCopyTrading(traderId: String) {
        viewModelScope.launch {
            val player = playerTraderState.value ?: return@launch
            val db = AppDatabase.getDatabase(getApplication())
            if (player.copyingTraderId == traderId) {
                db.traderDao().updateTrader(player.copy(copyingTraderId = null))
            } else {
                val targetTrader = db.traderDao().getTraderById(traderId) ?: return@launch
                db.traderDao().updateTrader(player.copy(copyingTraderId = traderId))
                db.newsDao().insertNews(NewsLog(
                    timestamp = System.currentTimeMillis(), traderName = "SİSTEM",
                    message = "${targetTrader.name} adlı uzman yatırımcıyı kopyalamaya başladınız!",
                    symbol = "GENEL", isSystemNews = true
                ))
            }
        }
    }

    fun setIntroSeen() { viewModelScope.launch { repository.setIntroSeen() } }
    fun setOutroSeen() { viewModelScope.launch { repository.setOutroSeen() } }

    // ────────────────────────────────────────────────────────────────────────
    // UI HELPERS
    // ────────────────────────────────────────────────────────────────────────

    fun getSmaValues(candles: List<MarketCandle>, period: Int): List<Float?> {
        val smaList = mutableListOf<Float?>()
        for (i in candles.indices) {
            if (i < period - 1) { smaList.add(null); continue }
            val avg = candles.subList(i - period + 1, i + 1).map { it.close }.average()
            smaList.add(avg.toFloat())
        }
        return smaList
    }

    fun getRsiValues(candles: List<MarketCandle>, period: Int): List<Float?> {
        val rsiList = mutableListOf<Float?>()
        for (i in 0 until minOf(period, candles.size)) rsiList.add(50f)
        for (i in (period + 1) until candles.size) {
            val subCandles = candles.subList(i - period, i + 1)
            val closes = subCandles.map { it.close }
            var gains = 0f; var losses = 0f
            for (j in 1 until closes.size) {
                val diff = (closes[j] - closes[j - 1]).toFloat()
                if (diff > 0) gains += diff else losses += -diff
            }
            val avgGain = gains / period; val avgLoss = losses / period
            if (avgLoss == 0f) { rsiList.add(100f); continue }
            val rs = avgGain / avgLoss
            rsiList.add(100f - (100f / (1f + rs)))
        }
        return rsiList
    }

    override fun onCleared() {
        super.onCleared()
        simJob?.cancel()
    }
}

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
