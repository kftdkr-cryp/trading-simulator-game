package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.GameRepository
import com.example.ui.GameViewModel
import com.example.ui.GameViewModelFactory
import com.example.ui.Localizer
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.MarketScreen
import com.example.ui.screens.NewsScreen
import com.example.ui.screens.MiniGamesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.IntroStoryScreen
import com.example.ui.screens.OutroStoryScreen
import com.example.ui.screens.LevelUpCinematicScreen
import com.example.ui.screens.CarPurchaseCinematicScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val database = AppDatabase.getDatabase(this)
        val repository = GameRepository(database)

        setContent {
            MyApplicationTheme {
                val gameViewModel: GameViewModel = viewModel(
                    factory = GameViewModelFactory(application, repository)
                )

                val settings by gameViewModel.settingsState.collectAsState()
                val lang = settings?.selectedLanguage ?: "TR"
                val isLoggedIn = settings?.loggedInUsername != null
                val playerTrader by gameViewModel.playerTraderState.collectAsState()
                val playerPositions by gameViewModel.playerPositions.collectAsState()
                val allLatestPrices by gameViewModel.allLatestPrices.collectAsState()

                if (!isLoggedIn) {
                    AuthScreen(viewModel = gameViewModel)
                } else if (settings?.introSeen == false) {
                    IntroStoryScreen(
                        lang = lang,
                        onComplete = { gameViewModel.setIntroSeen() }
                    )
                } else {
                    val cash = playerTrader?.cash ?: 0.0
                    val positionsValue = playerPositions.sumOf { pos ->
                        val latestPrice = allLatestPrices[pos.symbol] ?: pos.averageEntryPrice
                        if (pos.isLeverage) {
                            val priceDiff = if (pos.isLong) latestPrice - pos.averageEntryPrice
                                           else pos.averageEntryPrice - latestPrice
                            val pnl = priceDiff * pos.quantity
                            pos.margin + pnl
                        } else {
                            pos.quantity * latestPrice
                        }
                    }
                    val totalNetWorth = cash + positionsValue

                    if (totalNetWorth >= 1_000_000.0 && settings?.outroSeen == false) {
                        OutroStoryScreen(
                            lang = lang,
                            onContinue = { gameViewModel.setOutroSeen() },
                            onReset = { gameViewModel.resetGame() }
                        )
                    } else {
                        var activeTab by remember { mutableIntStateOf(0) }
                        var showSettings by remember { mutableStateOf(false) }

                        // Level-up cinematic state
                        var shownLevelUpAt by remember { mutableStateOf(setOf<Int>()) }
                        var pendingLevelUp by remember { mutableStateOf<Int?>(null) }
                        val currentLevel = when {
                            totalNetWorth >= 1_000_000.0 -> 5
                            totalNetWorth >= 100_000.0   -> 4
                            totalNetWorth >= 10_000.0    -> 3
                            totalNetWorth >= 2_000.0     -> 2
                            else -> 1
                        }
                        LaunchedEffect(currentLevel) {
                            if (currentLevel > 1 && !shownLevelUpAt.contains(currentLevel)) {
                                pendingLevelUp = currentLevel
                            }
                        }

                        // Car purchase cinematic state
                        val ownedCarsNow = settings?.ownedCars ?: ""
                        var lastCarCount by remember { mutableStateOf(ownedCarsNow.split(",").filter { it.isNotEmpty() }.size) }
                        var shownCarIds by remember { mutableStateOf(setOf<String>()) }
                        var pendingCarCinematic by remember { mutableStateOf<String?>(null) }
                        LaunchedEffect(ownedCarsNow) {
                            val cars = ownedCarsNow.split(",").filter { it.isNotEmpty() }
                            val luxuryCars = setOf("bmw_m3", "tesla_model_s", "ferrari")
                            cars.forEach { carId ->
                                if (carId in luxuryCars && carId !in shownCarIds) {
                                    pendingCarCinematic = carId
                                    shownCarIds = shownCarIds + carId
                                }
                            }
                            lastCarCount = cars.size
                        }

                        if (showSettings) {
                            SettingsScreen(
                                viewModel = gameViewModel,
                                onBack = { showSettings = false }
                            )
                        } else {
                            Scaffold(
                                topBar = {
                                    // Compact top bar with cash + settings icon
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF0B0E14))
                                            .statusBarsPadding()
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "📈 MARGIN CALL",
                                            color = Color(0xFFFFC107),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 1.sp
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            val playerCash = playerTrader?.cash ?: 0.0
                                            val cashColor = when {
                                                playerCash < -2000.0 -> Color(0xFFFF1744)
                                                playerCash < -1000.0 -> Color(0xFFFF5722)
                                                playerCash >= 0 -> Color(0xFF00E676)
                                                else -> Color(0xFFFF9800)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .background(Color(0xFF141A28), RoundedCornerShape(10.dp))
                                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = "$${String.format("%.0f", playerCash)}",
                                                    color = cashColor,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            IconButton(
                                                onClick = { showSettings = true },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.Gray)
                                            }
                                        }
                                    }
                                },
                                bottomBar = {
                                    NavigationBar(containerColor = Color(0xFF0B0E14), tonalElevation = 0.dp) {
                                        NavigationBarItem(
                                            selected = activeTab == 0,
                                            onClick = { activeTab = 0 },
                                            icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                                            label = { Text(Localizer.translate("portfolio", lang), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                            colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFFFFC107), selectedTextColor = Color(0xFFFFC107), indicatorColor = Color(0xFF1E2638))
                                        )
                                        NavigationBarItem(
                                            selected = activeTab == 1,
                                            onClick = { activeTab = 1 },
                                            icon = { Icon(Icons.Default.ShowChart, contentDescription = null) },
                                            label = { Text(Localizer.translate("market", lang), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                            colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFFFFC107), selectedTextColor = Color(0xFFFFC107), indicatorColor = Color(0xFF1E2638))
                                        )
                                        NavigationBarItem(
                                            selected = activeTab == 2,
                                            onClick = { activeTab = 2 },
                                            icon = { Icon(Icons.Default.Work, contentDescription = null) },
                                            label = { Text(Localizer.translate("mini_games", lang), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                            colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFFFFC107), selectedTextColor = Color(0xFFFFC107), indicatorColor = Color(0xFF1E2638))
                                        )
                                        NavigationBarItem(
                                            selected = activeTab == 3,
                                            onClick = { activeTab = 3 },
                                            icon = { Icon(Icons.Default.Forum, contentDescription = null) },
                                            label = { Text(Localizer.translate("social_feed", lang), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                            colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFFFFC107), selectedTextColor = Color(0xFFFFC107), indicatorColor = Color(0xFF1E2638))
                                        )
                                        NavigationBarItem(
                                            selected = activeTab == 4,
                                            onClick = { activeTab = 4 },
                                            icon = { Icon(Icons.Default.Leaderboard, contentDescription = null) },
                                            label = { Text(Localizer.translate("leaderboard", lang), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                            colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFFFFC107), selectedTextColor = Color(0xFFFFC107), indicatorColor = Color(0xFF1E2638))
                                        )
                                    }
                                }
                            ) { innerPadding ->
                                Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                                    when (activeTab) {
                                        0 -> DashboardScreen(viewModel = gameViewModel, onSelectAsset = { activeTab = 1 })
                                        1 -> MarketScreen(viewModel = gameViewModel)
                                        2 -> MiniGamesScreen(viewModel = gameViewModel)
                                        3 -> NewsScreen(viewModel = gameViewModel)
                                        4 -> LeaderboardScreen(viewModel = gameViewModel)
                                    }

                                    // Level-up cinematic overlay
                                    pendingLevelUp?.let { lvl ->
                                        LevelUpCinematicScreen(
                                            level = lvl,
                                            netWorth = totalNetWorth,
                                            lang = lang,
                                            onDismiss = {
                                                shownLevelUpAt = shownLevelUpAt + lvl
                                                pendingLevelUp = null
                                            }
                                        )
                                    }

                                    // Car purchase cinematic overlay
                                    pendingCarCinematic?.let { carId ->
                                        val carDisplayName = when (carId) {
                                            "ferrari" -> "Ferrari F40 🐎"
                                            "tesla_model_s" -> "Tesla Model S ⚡"
                                            "bmw_m3" -> "BMW M3 🏎️"
                                            else -> carId
                                        }
                                        CarPurchaseCinematicScreen(
                                            carName = carDisplayName,
                                            lang = lang,
                                            onDismiss = {
                                                pendingCarCinematic = null
                                                activeTab = 2 // Go to Garage tab
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
