package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [MarketCandle::class, Trader::class, TraderPosition::class, NewsLog::class, GameSettings::class, UserAccount::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun marketDao(): MarketDao
    abstract fun traderDao(): TraderDao
    abstract fun newsDao(): NewsDao
    abstract fun settingsDao(): SettingsDao
    abstract fun userAccountDao(): UserAccountDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "trader_simulator_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
