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
    @PrimaryKey val id: String,
    val traderId: String,
    val symbol: String,
    val quantity: Double,
    val averageEntryPrice: Double,
    val isLeverage: Boolean = false,
    val leverage: Int = 1,
    val isLong: Boolean = true,
    val margin: Double = 0.0,
    val liquidationPrice: Double = 0.0,
    val takeProfitPrice: Double? = null,
    val stopLossPrice: Double? = null
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

/** Local user account - stored per device, max 2 accounts */
@Entity(tableName = "user_accounts")
data class UserAccount(
    @PrimaryKey val username: String,
    val passwordHash: String,   // SHA-256 hex
    val displayName: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "game_settings")
data class GameSettings(
    @PrimaryKey val id: Int = 1,
    val selectedLanguage: String = "TR",
    val isDarkTheme: Boolean = true,
    val marketSpeed: Long = 3000L,
    // Auth (now local username-based, not Google)
    val loggedInUsername: String? = null,
    val activeIndicators: String = "SMA,RSI",
    val drawingsJson: String = "[]",
    val introSeen: Boolean = false,
    val outroSeen: Boolean = false,
    val currentHouseId: String = "kiralik_kotu",
    val ownedCars: String = "",
    val activeCarId: String? = null,
    val furnitureBought: String = "",
    val foodPlanId: Int = 1,
    val gameDayCount: Int = 1,
    val gameMonthCount: Int = 1,
    // Properties (comma-sep propertyId:listingPrice or just propertyId if not listed)
    val ownedProperties: String = "",
    val listedProperties: String = "" // "propId:price,propId2:price2"
)
