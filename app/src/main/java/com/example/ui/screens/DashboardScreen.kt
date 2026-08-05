package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.GameViewModel
import com.example.ui.Localizer
import com.example.ui.components.PortfolioCard

@Composable
fun DashboardScreen(
    viewModel: GameViewModel,
    onSelectAsset: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isPlaying by viewModel.isPlaying.collectAsState()
    val tickSpeedMs by viewModel.tickSpeedMs.collectAsState()
    val playerTrader by viewModel.playerTraderState.collectAsState()
    val playerPositions by viewModel.playerPositions.collectAsState()
    val selectedAsset by viewModel.selectedAsset.collectAsState()
    val settings by viewModel.settingsState.collectAsState()
    val allLatestPrices by viewModel.allLatestPrices.collectAsState()

    val lang = settings?.selectedLanguage ?: "TR"

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B0E14))
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 0. High-Fidelity Google Active Profile Banner
        settings?.let { s ->
            if (s.googleEmail != null) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141A28)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF1E273A), RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // User Avatar
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    ),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (s.googleName ?: "P").take(1).uppercase(),
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }

                        // User profile details
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = s.googleName ?: "Trader Player",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                // Small green active online badge
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color(0xFF00E676), CircleShape)
                                )
                            }
                            Text(
                                text = s.googleEmail ?: "",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }

                        // Linked Status
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF1B2418), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFF2E4324), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "GOOGLE SECURE",
                                color = Color(0xFF00E676),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }

        // Net Worth & Career Story calculation
        val currentPrices = allLatestPrices
        val cash = playerTrader?.cash ?: 0.0
        val positionsValue = playerPositions.sumOf { pos ->
            val latestPrice = currentPrices[pos.symbol] ?: pos.averageEntryPrice
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

        val milestoneTitle: String
        val milestoneDesc: String
        val milestoneTarget: Double
        val milestoneIcon: String

        if (netWorth < 500.0) {
            milestoneTitle = "Bölüm 1: Sıfırdan Başlangıç"
            milestoneDesc = "Borsada adını duyurmak için ilk adımı attın. Mini oyunlarla veya akıllı işlemlerle sermaye biriktir."
            milestoneTarget = 500.0
            milestoneIcon = "🌱"
        } else if (netWorth < 2000.0) {
            milestoneTitle = "Bölüm 2: Amatör Yatırımcı"
            milestoneDesc = "Piyasanın acımasız olduğunu fark ettin. İlk kaldıraçlı işlemlerinde likit olmamaya çalış!"
            milestoneTarget = 2000.0
            milestoneIcon = "📈"
        } else if (netWorth < 10000.0) {
            milestoneTitle = "Bölüm 3: Balina Avcısı"
            milestoneDesc = "Artık büyük balıklar seni fark etmeye başladı. Twitter akışındaki uzmanları kopyalayarak güç kazan."
            milestoneTarget = 10000.0
            milestoneIcon = "🐋"
        } else if (netWorth < 100000.0) {
            milestoneTitle = "Bölüm 4: Borsa Efendisi"
            milestoneDesc = "Milyonerler kulübüne giriş bileti! Sektörün en büyük 10 yapay zekasını alt etmek için son viraj."
            milestoneTarget = 100000.0
            milestoneIcon = "👑"
        } else {
            milestoneTitle = "Bölüm 5: Finansal Özgürlük"
            milestoneDesc = "Piyasanın efendisi oldun, 200 yapay zekayı dize getirdin ve Margin Call kabusunu sonsuza dek bitirdin!"
            milestoneTarget = 1000000.0
            milestoneIcon = "🏆"
        }

        val progressPct = (netWorth / milestoneTarget).coerceIn(0.0, 1.0).toFloat()

        // Career Story Progress Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131722)),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF1E2638), RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = milestoneIcon, fontSize = 24.sp)
                    Column {
                        Text(
                            text = milestoneTitle,
                            color = Color(0xFFFFC107),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Kariyer Hedefiniz / Career Progress",
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                    }
                }

                Text(
                    text = milestoneDesc,
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { progressPct },
                    color = Color(0xFFFFC107),
                    trackColor = Color(0xFF1E2638),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Net Değer: $${String.format("%,.2f", netWorth)}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Hedef: $${String.format("%,.2f", milestoneTarget)}",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // 1. Live Market Feed Status & Speed Controls
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141A28)),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF1E273A), RoundedCornerShape(12.dp))
                .testTag("simulation_controls_card")
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Blinking green status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color(0xFF00E676), CircleShape)
                    )
                    Text(
                        text = "PİYASA AKTİF / LIVE MARKET",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                // Right: Speed Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        Pair("SLOW", 6000L),
                        Pair("NORMAL", 3000L),
                        Pair("FAST", 1000L)
                    ).forEach { (label, speed) ->
                        val active = tickSpeedMs == speed
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (active) MaterialTheme.colorScheme.primary else Color(0xFF1E2638))
                                .clickable { viewModel.setTickSpeed(speed) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (active) Color.Black else Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 2. Asset Tickers Row
        Column {
            Text(
                text = Localizer.translate("market_watchlist", lang),
                color = Color.Gray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                viewModel.assets.forEach { asset ->
                    AssetTickerCard(
                        symbol = asset.symbol,
                        name = asset.displayName,
                        isSelected = selectedAsset == asset.symbol,
                        onClick = {
                            viewModel.selectAsset(asset.symbol)
                            onSelectAsset(asset.symbol)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. User Portfolio Card
        Column {
            Text(
                text = Localizer.translate("portfolio_overview", lang),
                color = Color.Gray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            playerTrader?.let { trader ->
                PortfolioCard(
                    cash = trader.cash,
                    positions = playerPositions,
                    currentPrices = allLatestPrices
                )
            }
        }

        // 4. Quick Tips Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131722)),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF1E2638), RoundedCornerShape(12.dp))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Gameplay Tip",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = Localizer.translate("tips_description", lang),
                        color = Color.Gray,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AssetTickerCard(
    symbol: String,
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = when (symbol) {
        "MKTX" -> Color(0xFF00E676)
        "SOLR" -> Color(0xFFFFC107)
        "NEOM" -> Color(0xFF00B0FF)
        else -> Color(0xFFE040FB)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFF1E2638) else Color(0xFF141A28))
            .border(
                1.dp,
                if (isSelected) color else Color(0xFF1E273A),
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp)
            .testTag("ticker_card_$symbol")
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = symbol.take(1),
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = symbol,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = name,
                color = Color.Gray,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}
