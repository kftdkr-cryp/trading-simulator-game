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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.GameRepository
import com.example.ui.GameViewModel
import com.example.ui.GameViewModelFactory
import com.example.ui.Localizer
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.border

import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize local Room Database & Repository
        val database = AppDatabase.getDatabase(this)
        val repository = GameRepository(database)

        setContent {
            val context = LocalContext.current
            val prefs = remember(context) { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
            
            var hasChosenLang by remember { mutableStateOf(prefs.getBoolean("has_chosen_lang", false)) }
            var isDarkMode by remember { mutableStateOf(prefs.getBoolean("is_dark_mode", true)) }

            MyApplicationTheme(darkTheme = isDarkMode) {
                // Initialize ViewModel using custom Factory
                val gameViewModel: GameViewModel = viewModel(
                    factory = GameViewModelFactory(application, repository)
                )

                val settings by gameViewModel.settingsState.collectAsState()
                val lang = settings?.selectedLanguage ?: "TR"
                val isLoggedIn = settings?.googleEmail != null
                val playerTrader by gameViewModel.playerTraderState.collectAsState()
                val playerPositions by gameViewModel.playerPositions.collectAsState()
                val allLatestPrices by gameViewModel.allLatestPrices.collectAsState()

                // Intercept game screens based on user state
                if (!hasChosenLang) {
                    LanguageSelectionScreen(
                        viewModel = gameViewModel,
                        onLanguageSelected = {
                            hasChosenLang = true
                        }
                    )
                } else if (!isLoggedIn) {
                    // Force Google Login Screen at the beginning with language selector
                    GoogleLoginScreen(viewModel = gameViewModel)
                } else {
                    val cash = playerTrader?.cash ?: 0.0
                    
                    if (cash <= -3000.0) {
                        // Hospital / Death sequence when in severe debt
                        HospitalGameOverScreen(
                            lang = lang,
                            onReset = {
                                gameViewModel.resetGame()
                            }
                        )
                    } else if (settings?.introSeen == false) {
                        // Force Intro Animation screen before the game starts
                        com.example.ui.screens.IntroStoryScreen(
                            lang = lang,
                            onComplete = { gameViewModel.setIntroSeen() }
                        )
                    } else {
                        // Calculate total Net Worth to see if they won the game
                        val positionsValue = playerPositions.sumOf { pos ->
                            val latestPrice = allLatestPrices[pos.symbol] ?: pos.averageEntryPrice
                            if (pos.isLeverage) {
                                val priceDiff = if (pos.isLong) {
                                    latestPrice - pos.averageEntryPrice
                                } else {
                                    pos.averageEntryPrice - latestPrice
                                }
                                val pnl = priceDiff * pos.quantity
                                pos.margin + pnl
                            } else {
                                pos.quantity * latestPrice
                            }
                        }
                        val netWorth = cash + positionsValue

                        if (settings?.outroSeen == false && netWorth >= 1000000.0) {
                            // Force Outro Animation screen upon game completion
                            com.example.ui.screens.OutroStoryScreen(
                                lang = lang,
                                onReset = {
                                    gameViewModel.resetGame()
                                },
                                onContinue = {
                                    gameViewModel.setOutroSeen()
                                }
                            )
                        } else {
                            // Fully logged in & intro completed - unlock the main game dashboard
                            var activeTab by remember { mutableIntStateOf(0) }
                            val coroutineScope = rememberCoroutineScope()
                            var showSettingsDialog by remember { mutableStateOf(false) }

                            // Dynamic Theme Colors based on Selected Mode
                            val themeBg = if (isDarkMode) Color(0xFF0B0E14) else Color(0xFFF1F5F9)
                            val themeSurface = if (isDarkMode) Color(0xFF141A28) else Color.White
                            val themeText = if (isDarkMode) Color.White else Color(0xFF0F172A)
                            val themeSubText = if (isDarkMode) Color.LightGray else Color(0xFF475569)

                            Scaffold(
                                modifier = Modifier.fillMaxSize(),
                                containerColor = themeBg,
                                topBar = {
                                    Column(
                                        modifier = Modifier
                                            .background(themeBg)
                                            .statusBarsPadding()
                                            .padding(horizontal = 16.dp, vertical = 4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                IconButton(
                                                    onClick = { showSettingsDialog = true }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Settings,
                                                        contentDescription = "Settings",
                                                        tint = Color(0xFFFFC107)
                                                    )
                                                }
                                                Text(
                                                    text = Localizer.translate("app_title", lang),
                                                    color = Color(0xFFFFC107),
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Black,
                                                    letterSpacing = 1.2.sp,
                                                    modifier = Modifier.testTag("app_logo_title")
                                                )
                                            }

                                            // Display logged in user tag and quick log-out option
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(if (isDarkMode) Color(0xFF1E2638) else Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                                                        .clickable {
                                                            // Quick log-out simulation to let the user re-sign in
                                                            coroutineScope.launch {
                                                                gameViewModel.syncGoogleProfile("", "", "")
                                                                // Trigger settings update
                                                                val db = AppDatabase.getDatabase(this@MainActivity)
                                                                val s = db.settingsDao().getSettings()
                                                                if (s != null) {
                                                                    db.settingsDao().insertOrUpdateSettings(s.copy(googleEmail = null))
                                                                }
                                                            }
                                                        }
                                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        text = "SIGN OUT",
                                                        color = themeSubText,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .background(if (isDarkMode) Color(0xFF261D0F) else Color(0xFFFEF3C7), RoundedCornerShape(20.dp))
                                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        text = "200 TRADERS",
                                                        color = Color(0xFFFFB300),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                },
                                bottomBar = {
                                    NavigationBar(
                                        containerColor = themeSurface,
                                        contentColor = if (isDarkMode) Color.Gray else Color(0xFF64748B),
                                        tonalElevation = 8.dp,
                                        modifier = Modifier
                                            .navigationBarsPadding()
                                            .testTag("bottom_navigation_bar")
                                    ) {
                                        NavigationBarItem(
                                            selected = activeTab == 0,
                                            onClick = { activeTab = 0 },
                                            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Portfolio") },
                                            label = { Text(Localizer.translate("portfolio", lang), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = Color(0xFFFFC107),
                                                selectedTextColor = Color(0xFFFFC107),
                                                unselectedIconColor = if (isDarkMode) Color.Gray else Color(0xFF64748B),
                                                unselectedTextColor = if (isDarkMode) Color.Gray else Color(0xFF64748B),
                                                indicatorColor = if (isDarkMode) Color(0xFF1E2638) else Color(0xFFE2E8F0)
                                            )
                                        )

                                        NavigationBarItem(
                                            selected = activeTab == 1,
                                            onClick = { activeTab = 1 },
                                            icon = { Icon(Icons.Default.ShowChart, contentDescription = "Charts") },
                                            label = { Text(Localizer.translate("market", lang), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = Color(0xFFFFC107),
                                                selectedTextColor = Color(0xFFFFC107),
                                                unselectedIconColor = if (isDarkMode) Color.Gray else Color(0xFF64748B),
                                                unselectedTextColor = if (isDarkMode) Color.Gray else Color(0xFF64748B),
                                                indicatorColor = if (isDarkMode) Color(0xFF1E2638) else Color(0xFFE2E8F0)
                                            )
                                        )

                                        NavigationBarItem(
                                            selected = activeTab == 2,
                                            onClick = { activeTab = 2 },
                                            icon = { Icon(Icons.Default.Casino, contentDescription = "Jobs & Games") },
                                            label = { Text(Localizer.translate("mini_games", lang), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = Color(0xFFFFC107),
                                                selectedTextColor = Color(0xFFFFC107),
                                                unselectedIconColor = if (isDarkMode) Color.Gray else Color(0xFF64748B),
                                                unselectedTextColor = if (isDarkMode) Color.Gray else Color(0xFF64748B),
                                                indicatorColor = if (isDarkMode) Color(0xFF1E2638) else Color(0xFFE2E8F0)
                                            )
                                        )

                                        NavigationBarItem(
                                            selected = activeTab == 3,
                                            onClick = { activeTab = 3 },
                                            icon = { Icon(Icons.Default.Forum, contentDescription = "Social Feed") },
                                            label = { Text(Localizer.translate("social_feed", lang), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = Color(0xFFFFC107),
                                                selectedTextColor = Color(0xFFFFC107),
                                                unselectedIconColor = if (isDarkMode) Color.Gray else Color(0xFF64748B),
                                                unselectedTextColor = if (isDarkMode) Color.Gray else Color(0xFF64748B),
                                                indicatorColor = if (isDarkMode) Color(0xFF1E2638) else Color(0xFFE2E8F0)
                                            )
                                        )

                                        NavigationBarItem(
                                            selected = activeTab == 4,
                                            onClick = { activeTab = 4 },
                                            icon = { Icon(Icons.Default.Leaderboard, contentDescription = "Leaderboard") },
                                            label = { Text(Localizer.translate("leaderboard", lang), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = Color(0xFFFFC107),
                                                selectedTextColor = Color(0xFFFFC107),
                                                unselectedIconColor = if (isDarkMode) Color.Gray else Color(0xFF64748B),
                                                unselectedTextColor = if (isDarkMode) Color.Gray else Color(0xFF64748B),
                                                indicatorColor = if (isDarkMode) Color(0xFF1E2638) else Color(0xFFE2E8F0)
                                            )
                                        )
                                    }
                                }
                            ) { innerPadding ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(themeBg)
                                        .padding(innerPadding)
                                ) {
                                    when (activeTab) {
                                        0 -> DashboardScreen(
                                            viewModel = gameViewModel,
                                            onSelectAsset = { activeTab = 1 } // Open chart tab directly when ticker clicked
                                        )
                                        1 -> MarketScreen(viewModel = gameViewModel)
                                        2 -> MiniGamesScreen(viewModel = gameViewModel)
                                        3 -> NewsScreen(viewModel = gameViewModel)
                                        4 -> LeaderboardScreen(viewModel = gameViewModel)
                                    }
                                }
                            }

                            // Interactive Settings Dialog Overlay
                            if (showSettingsDialog) {
                                AlertDialog(
                                    onDismissRequest = { showSettingsDialog = false },
                                    confirmButton = {
                                        Button(
                                            onClick = { showSettingsDialog = false },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107))
                                        ) {
                                            Text(Localizer.translate("close", lang), color = Color.Black, fontWeight = FontWeight.Bold)
                                        }
                                    },
                                    title = {
                                        Text(
                                            text = Localizer.translate("settings_title", lang),
                                            color = themeText,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                    },
                                    text = {
                                        Column(
                                            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                                            verticalArrangement = Arrangement.spacedBy(20.dp)
                                        ) {
                                            // Theme Toggle Row
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = Localizer.translate("theme_mode", lang),
                                                    color = themeSubText,
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 14.sp
                                                )

                                                Row(
                                                    modifier = Modifier
                                                        .background(
                                                            if (isDarkMode) Color(0xFF1E2638) else Color(0xFFE2E8F0),
                                                            RoundedCornerShape(20.dp)
                                                        )
                                                        .padding(4.dp),
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .background(
                                                                if (isDarkMode) Color(0xFFFFC107) else Color.Transparent,
                                                                RoundedCornerShape(16.dp)
                                                            )
                                                            .clickable {
                                                                isDarkMode = true
                                                                prefs.edit().putBoolean("is_dark_mode", true).apply()
                                                            }
                                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                                    ) {
                                                        Text(
                                                            text = Localizer.translate("dark_mode", lang),
                                                            color = if (isDarkMode) Color.Black else Color.Gray,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }

                                                    Box(
                                                        modifier = Modifier
                                                            .background(
                                                                if (!isDarkMode) Color(0xFFFFC107) else Color.Transparent,
                                                                RoundedCornerShape(16.dp)
                                                            )
                                                            .clickable {
                                                                isDarkMode = false
                                                                prefs.edit().putBoolean("is_dark_mode", false).apply()
                                                            }
                                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                                    ) {
                                                        Text(
                                                            text = Localizer.translate("light_mode", lang),
                                                            color = if (!isDarkMode) Color.Black else Color.Gray,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }

                                            // Thin divider box (perfectly compile-safe)
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(1.dp)
                                                    .background(if (isDarkMode) Color(0xFF1E273A) else Color(0xFFE2E8F0))
                                             )

                                            // Language Selector Column
                                            Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Text(
                                                    text = if (lang == "TR") "Dil Seçimi" else "Select Language",
                                                    color = themeSubText,
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 14.sp
                                                )

                                                val languages = listOf(
                                                    LanguageItem("TR", "🇹🇷", "Türkçe"),
                                                    LanguageItem("EN", "🇺🇸", "English"),
                                                    LanguageItem("ZH", "🇨🇳", "中文"),
                                                    LanguageItem("ES", "🇪🇸", "Español"),
                                                    LanguageItem("RU", "🇷🇺", "Русский"),
                                                    LanguageItem("HI", "🇮🇳", "हिन्दी"),
                                                    LanguageItem("AZ", "🇦🇿", "Azərbaycanca"),
                                                    LanguageItem("FR", "🇫🇷", "Français"),
                                                    LanguageItem("TH", "🇹🇭", "ไทย"),
                                                    LanguageItem("DE", "🇩🇪", "Deutsch")
                                                )

                                                var expandedSettingLang by remember { mutableStateOf(false) }
                                                val activeLangItem = languages.firstOrNull { it.code == lang } ?: languages[0]

                                                Box(modifier = Modifier.fillMaxWidth()) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .background(
                                                                if (isDarkMode) Color(0xFF1E2638) else Color(0xFFE2E8F0),
                                                                RoundedCornerShape(12.dp)
                                                            )
                                                            .border(1.dp, if (isDarkMode) Color(0xFF1E273A) else Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                                                            .clickable { expandedSettingLang = true }
                                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Text(
                                                            text = "${activeLangItem.flag}  ${activeLangItem.name}",
                                                            color = themeText,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 14.sp
                                                        )
                                                        Icon(
                                                            imageVector = Icons.Default.ArrowDropDown,
                                                            contentDescription = "Dropdown",
                                                            tint = if (isDarkMode) Color.Gray else Color(0xFF475569)
                                                        )
                                                    }

                                                    DropdownMenu(
                                                        expanded = expandedSettingLang,
                                                        onDismissRequest = { expandedSettingLang = false },
                                                        modifier = Modifier
                                                            .background(if (isDarkMode) Color(0xFF141A28) else Color.White)
                                                    ) {
                                                        languages.forEach { item ->
                                                            DropdownMenuItem(
                                                                text = {
                                                                    Text(
                                                                        text = "${item.flag}  ${item.name}",
                                                                        color = if (isDarkMode) Color.White else Color(0xFF0F172A),
                                                                        fontWeight = FontWeight.Medium
                                                                    )
                                                                },
                                                                onClick = {
                                                                    gameViewModel.updateLanguage(item.code)
                                                                    prefs.edit().putString("selected_lang", item.code).apply()
                                                                    expandedSettingLang = false
                                                                },
                                                                modifier = Modifier.background(
                                                                    if (item.code == lang) Color.White.copy(alpha = 0.05f) else Color.Transparent
                                                                )
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    containerColor = themeSurface,
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.border(1.dp, if (isDarkMode) Color(0xFF1E273A) else Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
