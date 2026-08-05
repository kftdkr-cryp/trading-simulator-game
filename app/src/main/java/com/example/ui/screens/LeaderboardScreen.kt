package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.Trader
import com.example.ui.GameViewModel
import com.example.ui.components.TraderListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val tradersList by viewModel.traders.collectAsState()
    val playerTrader by viewModel.playerTraderState.collectAsState()
    val selectedTraderDetailId by viewModel.selectedTraderDetailId.collectAsState()

    var searchQuery by remember { mutableStateFlowOf("") }
    var selectedArchetypeFilter by remember { mutableStateFlowOf<String?>(null) }

    // List of unique archetypes for filtering
    val filterOptions = listOf(
        Pair("Tümü", null),
        Pair("Balina", "WHALE"),
        Pair("Trend Takipçisi", "TREND_FOLLOWER"),
        Pair("Karşıt Yatırımcı", "CONTRARIAN"),
        Pair("Kısa Vadeci", "SCALPER"),
        Pair("HODL", "HODLER"),
        Pair("Panik Satıcı", "PANIC_SELLER")
    )

    // Filtered list
    val filteredTraders = remember(tradersList, searchQuery, selectedArchetypeFilter) {
        tradersList.filter { trader ->
            val matchesSearch = trader.name.contains(searchQuery, ignoreCase = true)
            val matchesFilter = selectedArchetypeFilter == null || trader.archetype == selectedArchetypeFilter
            matchesSearch && matchesFilter
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B0E14))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Highlight Player's Standings Card
        playerTrader?.let { player ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1710)), // Warm glow
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFFFC107).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .testTag("player_standings_card")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(Color(0xFFFFC107), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Trophy",
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "SİZİN SIRALAMANIZ",
                                color = Color(0xFFFFC107),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = player.name,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "#${player.rank} / 201",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val roi = if (player.initialCapital > 0.0) {
                            ((player.cash - player.initialCapital) / player.initialCapital) * 100.0
                        } else {
                            0.0
                        }
                        Text(
                            text = "Kâr/Zarar: ${if (roi >= 0) "+" else ""}${String.format("%.2f", roi)}%",
                            color = if (roi >= 0) Color(0xFF00E676) else Color(0xFFFF1744),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // 2. Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Yatırımcı ismi ara...", color = Color.Gray) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFFFFC107),
                unfocusedBorderColor = Color(0xFF1E273A)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("trader_search_input")
        )

        // 3. Archetype filter options horizontal row
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(filterOptions) { (label, archetype) ->
                val active = selectedArchetypeFilter == archetype
                val color = if (active) Color(0xFFFFC107) else Color(0xFF1E2638)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(color)
                        .clickable { selectedArchetypeFilter = archetype }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        color = if (active) Color.Black else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 4. Leaders / Competitors List
        Box(modifier = Modifier.weight(1f)) {
            if (filteredTraders.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aramaya uygun yatırımcı bulunamadı.",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredTraders) { trader ->
                        TraderListItem(
                            trader = trader,
                            onClick = { viewModel.showTraderDetail(trader.id) }
                        )
                    }
                }
            }
        }
    }

    // 5. Trader Detail Dialog Modal
    selectedTraderDetailId?.let { detailId ->
        val selectedTrader = tradersList.firstOrNull { it.id == detailId }
        if (selectedTrader != null) {
            TraderDetailDialog(
                trader = selectedTrader,
                isPlayerCopying = playerTrader?.copyingTraderId == selectedTrader.id,
                onDismiss = { viewModel.showTraderDetail(null) },
                onToggleCopy = {
                    viewModel.toggleCopyTrading(selectedTrader.id)
                    viewModel.showTraderDetail(null)
                }
            )
        }
    }
}

@Composable
fun TraderDetailDialog(
    trader: Trader,
    isPlayerCopying: Boolean,
    onDismiss: () -> Unit,
    onToggleCopy: () -> Unit
) {
    val strategyName = when (trader.archetype) {
        "TREND_FOLLOWER" -> "Trend Takipçisi"
        "CONTRARIAN" -> "Karşıt Yatırımcı"
        "SCALPER" -> "Kısa Vadeci (Scalp)"
        "WHALE" -> "Balina (Piyasa Yapıcı)"
        "PANIC_SELLER" -> "Panik Al-Satçı"
        "HODLER" -> "HODL Odaklı"
        "CHAOS" -> "Rastgele Kaotik"
        else -> trader.archetype
    }

    val strategyDescription = when (trader.archetype) {
        "TREND_FOLLOWER" -> "Piyasanın yönünü (SMA hareketli ortalamalarını) takip eder. Fiyat ortalamanın üstündeyken alım yapar, altına düştüğünde satar."
        "CONTRARIAN" -> "RSI göstergesine göre hareket eder. Herkes satarken ve fiyat dipteyken (RSI < 30) satın alır, fiyat çok yükseldiğinde (RSI > 70) kâr alır."
        "SCALPER" -> "Dakikalık küçük grafik hareketlerinden yararlanır. Çok hızlı şekilde ufak kârlar elde eder veya küçük stop-losslar ile pozisyon kapatır."
        "WHALE" -> "Büyük miktarlarda sermayeye sahiptir. Zaman zaman piyasaya yüklü miktarda giriş yaparak trendleri doğrudan etkileyebilir."
        "PANIC_SELLER" -> "Fiyatlarda %2.5 ve üzeri sert düşüşler gördüğünde elindeki her şeyi panikle satar. Genellikle zirveden alıp dipten satmaya eğilimlidir."
        "HODLER" -> "Satın aldığı varlıkları çok uzun süre elinde tutar. Fiyat düşüşlerinden etkilenmez ve ancak çok büyük kâr hedeflerine (%80+) ulaştığında satış yapar."
        "CHAOS" -> "Grafik analizi veya teknik verilere bakmaz. Kararlarını tamamen rastgele veya hislerine dayanarak verir."
        else -> "Bilinmeyen strateji"
    }

    val profit = trader.cash - trader.initialCapital
    val profitPercent = if (trader.initialCapital > 0.0) {
        (profit / trader.initialCapital) * 100.0
    } else {
        0.0
    }
    val isProfit = profit >= 0

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141A28)),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF1E273A), RoundedCornerShape(20.dp))
                .testTag("trader_detail_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color(0xFF1E2638), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = trader.name.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = trader.name,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .background(Color(0xFFFFC107).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = strategyName,
                        color = Color(0xFFFFC107),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFF1E273A))
                Spacer(modifier = Modifier.height(14.dp))

                // Stats Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(text = "Mevcut Sermaye", color = Color.Gray, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$${String.format("%,.0f", trader.cash)}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(text = "Toplam Getiri", color = Color.Gray, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${if (isProfit) "+" else ""}${String.format("%.1f", profitPercent)}%",
                            color = if (isProfit) Color(0xFF00E676) else Color(0xFFFF1744),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(text = "Kazanma Oranı", color = Color.Gray, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "%${String.format("%.1f", trader.winRate)}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = Color(0xFF1E273A))
                Spacer(modifier = Modifier.height(14.dp))

                // Strategy Description
                Text(
                    text = "Yatırımcı Profili & Stratejisi",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = strategyDescription,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Copy Trade Action Button
                if (!trader.isPlayer) {
                    Button(
                        onClick = onToggleCopy,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPlayerCopying) Color(0xFFFF1744) else Color(0xFFFFC107)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("copy_trade_button")
                    ) {
                        Text(
                            text = if (isPlayerCopying) "KOPYALAMAYI DURDUR" else "BU YATIRIMCIYI KOPYALA",
                            color = if (isPlayerCopying) Color.White else Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Close Button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Kapat", color = Color.LightGray)
                }
            }
        }
    }
}

// Helper to update state-flows in compose safely
fun <T> mutableStateFlowOf(value: T): MutableState<T> = mutableStateOf(value)
