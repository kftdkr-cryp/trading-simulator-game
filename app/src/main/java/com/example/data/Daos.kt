package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MarketDao {
    @Query("SELECT * FROM market_candles WHERE symbol = :symbol ORDER BY timestamp ASC")
    fun getCandlesFlow(symbol: String): Flow<List<MarketCandle>>

    @Query("SELECT * FROM market_candles WHERE symbol = :symbol ORDER BY timestamp ASC")
    fun getCandlesList(symbol: String): List<MarketCandle>

    @Query("SELECT * FROM market_candles ORDER BY timestamp ASC")
    fun getAllCandles(): List<MarketCandle>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCandles(candles: List<MarketCandle>)

    @Query("DELETE FROM market_candles")
    suspend fun clearAllCandles()
}

@Dao
interface TraderDao {
    @Query("SELECT * FROM traders ORDER BY rank ASC, cash + initialCapital DESC")
    fun getAllTradersFlow(): Flow<List<Trader>>

    @Query("SELECT * FROM traders")
    fun getAllTradersList(): List<Trader>

    @Query("SELECT * FROM traders WHERE id = :id")
    suspend fun getTraderById(id: String): Trader?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTraders(traders: List<Trader>)

    @Update
    suspend fun updateTrader(trader: Trader)

    @Update
    suspend fun updateTraders(traders: List<Trader>)

    @Query("SELECT * FROM trader_positions WHERE traderId = :traderId")
    fun getPositionsForTraderFlow(traderId: String): Flow<List<TraderPosition>>

    @Query("SELECT * FROM trader_positions WHERE traderId = :traderId")
    suspend fun getPositionsForTraderList(traderId: String): List<TraderPosition>

    @Query("SELECT * FROM trader_positions")
    suspend fun getAllPositions(): List<TraderPosition>

    @Query("SELECT * FROM trader_positions WHERE id = :id")
    suspend fun getPositionById(id: String): TraderPosition?

    @Query("SELECT * FROM trader_positions WHERE traderId = :traderId AND symbol = :symbol")
    suspend fun getPosition(traderId: String, symbol: String): TraderPosition?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosition(position: TraderPosition)

    @Delete
    suspend fun deletePosition(position: TraderPosition)

    @Query("DELETE FROM trader_positions WHERE traderId = :traderId AND symbol = :symbol")
    suspend fun deletePositionBySymbol(traderId: String, symbol: String)

    @Query("DELETE FROM trader_positions")
    suspend fun clearAllPositions()
}

@Dao
interface NewsDao {
    @Query("SELECT * FROM news_logs ORDER BY timestamp DESC LIMIT 100")
    fun getNewsFlow(): Flow<List<NewsLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNews(news: NewsLog)

    @Query("DELETE FROM news_logs")
    suspend fun clearAllNews()
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM game_settings WHERE id = 1")
    fun getSettingsFlow(): Flow<GameSettings?>

    @Query("SELECT * FROM game_settings WHERE id = 1")
    suspend fun getSettings(): GameSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: GameSettings)
}

@Dao
interface UserAccountDao {
    @Query("SELECT * FROM user_accounts ORDER BY createdAt ASC")
    suspend fun getAllAccounts(): List<UserAccount>

    @Query("SELECT * FROM user_accounts WHERE username = :username LIMIT 1")
    suspend fun getByUsername(username: String): UserAccount?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAccount(account: UserAccount)

    @Query("SELECT COUNT(*) FROM user_accounts")
    suspend fun countAccounts(): Int
}
