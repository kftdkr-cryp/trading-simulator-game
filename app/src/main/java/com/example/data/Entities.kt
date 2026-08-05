package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "market_candles")
data class MarketCandle(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val symbol: String,
    val timestamp: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double
)

@Entity(tableName = "traders")
data class Trader(
    @PrimaryKey val id: String,
    val name: String,
    val archetype: String,
    val cash: Double,
    val initialCapital: Double,
    val winRate: Double,
    val isPlayer: Boolean,
    val copyingTraderId: String? = null,
    val rank: Int = 201
)

@Entity(tableName = "trader_positions")
data class TraderPosition(
    @PrimaryKey val id: String, // format: traderId_symbol_isLong
    val traderId: String,
    val symbol: String,
    val quantity: Double,
    val averageEntryPrice: Double,
    val isLeverage: Boolean = false,
    val leverage: Int = 1,
    val isLong: Boolean = true,
    val margin: Double = 0.0,
    val liquidationPrice: Double = 0.0,
    val takeProfitPrice: Double = 0.0,
    val stopLossPrice: Double = 0.0
)

@Entity(tableName = "news_logs")
data class NewsLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val traderName: String,
    val message: String,
    val symbol: String,
    val isSystemNews: Boolean
)

@Entity(tableName = "game_settings")
data class GameSettings(
    @PrimaryKey val id: Int = 1,
    val selectedLanguage: String = "TR",
    val googleEmail: String? = null,
    val googleName: String? = null,
    val googleAvatarUrl: String? = null,
    val activeIndicators: String = "SMA,RSI", // Comma-separated list of active indicators
    val drawingsJson: String = "[]", // Serialized user drawings
    val introSeen: Boolean = false,
    val outroSeen: Boolean = false,
    val currentHouseId: String = "kiralik_kotu",
    val ownedCars: String = "",
    val activeCarId: String? = null,
    val furnitureBought: String = "",
    val foodPlanId: Int = 1, // 1 = Kötü, 2 = Ortalama, 3 = İyi
    val gameDayCount: Int = 1,
    val gameMonthCount: Int = 1
)
