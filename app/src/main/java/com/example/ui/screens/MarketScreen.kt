package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.GameViewModel
import com.example.ui.Localizer
import com.example.ui.components.CandleChart
import com.example.ui.components.LineDrawing

// Helpers for Drawings Serialization
fun parseDrawings(serialized: String): List<LineDrawing> {
    if (serialized.isEmpty() || serialized == "[]") return emptyList()
    val list = mutableListOf<LineDrawing>()
    try {
        val lines = serialized.split(";")
        for (line in lines) {
            if (line.isEmpty()) continue
            val parts = line.split(",")
            if (parts.size >= 4) {
                val type = if (parts.size >= 5) parts[4] else "LINE"
                list.add(
                    LineDrawing(
                        parts[0].toFloat(),
                        parts[1].toFloat(),
                        parts[2].toFloat(),
                        parts[3].toFloat(),
                        type
                    )
                )
            }
        }
    } catch (e: Exception) {
        // ignore parsing errors
    }
    return list
}

fun serializeDrawings(drawings: List<LineDrawing>): String {
    return drawings.joinToString(";") { "${it.startX},${it.startY},${it.endX},${it.endY},${it.type}" }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val selectedAsset by viewModel.selectedAsset.collectAsState()
    val activeCandles by viewModel.activeCandles.collectAsState()
    val tradeQuantity by viewModel.tradeQuantity.collectAsState()

    val showSma10 by viewModel.showSma10.collectAsState()
    val showSma20 by viewModel.showSma20.collectAsState()
    val showRsi by viewModel.showRsi.collectAsState()

    val playerPositions by viewModel.playerPositions.collectAsState()
    val playerTraderState by viewModel.playerTraderState.collectAsState()
    val settings by viewModel.settingsState.collectAsState()

    val lang = settings?.selectedLanguage ?: "TR"

    // Parse persisted drawings
    val drawingsSerialized = settings?.drawingsJson ?: "[]"
    val drawingsList = remember(drawingsSerialized) { parseDrawings(drawingsSerialized) }

    // Drawing mode toggle
    var drawingModeEnabled by remember { mutableStateOf(false) }
    var selectedDrawingTool by remember { mutableStateOf("LINE") }
    var chartMaximized by remember { mutableStateOf(false) }

    var spotTakeProfitInput by remember { mutableStateOf("") }
    var spotStopLossInput by remember { mutableStateOf("") }

    var levTakeProfitInput by remember { mutableStateOf("") }
    var levStopLossInput by remember { mutableStateOf("") }

    var aiAnalysisResult by remember { mutableStateOf("") }
    var aiAnalysisLoading by remember { mutableStateOf(false) }

    // Trade Tab (Spot = 0, Leverage = 1)
    var tradingModeTab by remember { mutableStateOf(0) }

    // Leverage specific parameters
    var selectedLeverageMultiplier by remember { mutableStateOf(5) }
    var leverageIsLong by remember { mutableStateOf(true) }
    var marginAmountInput by remember { mutableStateOf("100") }

    val activeSpotPosition = playerPositions.firstOrNull { it.symbol == selectedAsset && !it.isLeverage }
    val activeLeveragePositions = playerPositions.filter { it.symbol == selectedAsset && it.isLeverage }

    val latestCandle = activeCandles.lastOrNull()
    val latestPrice = latestCandle?.close ?: 0.0

    val assetDetails = viewModel.assets.firstOrNull { it.symbol == selectedAsset } ?: viewModel.assets[0]

    // Calculate technical indicators on demand
    val sma10Values = remember(activeCandles) { viewModel.getSmaValues(activeCandles, 10) }
    val sma20Values = remember(activeCandles) { viewModel.getSmaValues(activeCandles, 20) }
    val rsiValues = remember(activeCandles) { viewModel.getRsiValues(activeCandles, 10) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B0E14))
            .verticalScroll(scrollState)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. Asset Selection Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            viewModel.assets.forEach { asset ->
                val active = asset.symbol == selectedAsset
                val color = when (asset.symbol) {
                    "MKTX" -> Color(0xFF00E676)
                    "SOLR" -> Color(0xFFFFC107)
                    "NEOM" -> Color(0xFF00B0FF)
                    else -> Color(0xFFE040FB)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (active) color.copy(alpha = 0.15f) else Color(0xFF141A28))
                        .border(1.dp, if (active) color else Color(0xFF1E273A), RoundedCornerShape(10.dp))
                        .clickable { viewModel.selectAsset(asset.symbol) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = asset.symbol,
                        color = if (active) color else Color.LightGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // 2. Active Asset Info Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141A28)),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF1E273A), RoundedCornerShape(14.dp))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = assetDetails.displayName,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = assetDetails.description,
                        color = Color.Gray,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        modifier = Modifier.widthIn(max = 240.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$${String.format("%,.2f", latestPrice)}",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )
                    
                    latestCandle?.let { candle ->
                        val change = (candle.close - candle.open) / candle.open
                        val changePercent = change * 100.0
                        val isBull = changePercent >= 0
                        Text(
                            text = "${if (isBull) "+" else ""}${String.format("%.2f", changePercent)}%",
                            color = if (isBull) Color(0xFF00E676) else Color(0xFFFF1744),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // 3. Technical Indicators & Drawing Controls Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Side: SMA & RSI chip selectors
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                FilterChip(
                    selected = showSma10,
                    onClick = { viewModel.toggleSma10() },
                    label = { Text("SMA10", fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFFC107).copy(alpha = 0.2f),
                        selectedLabelColor = Color(0xFFFFC107)
                    )
                )

                FilterChip(
                    selected = showSma20,
                    onClick = { viewModel.toggleSma20() },
                    label = { Text("SMA20", fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF00B0FF).copy(alpha = 0.2f),
                        selectedLabelColor = Color(0xFF00B0FF)
                    )
                )

                FilterChip(
                    selected = showRsi,
                    onClick = { viewModel.toggleRsi() },
                    label = { Text("RSI", fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFE040FB).copy(alpha = 0.2f),
                        selectedLabelColor = Color(0xFFE040FB)
                    )
                )
            }

            // Right Side: Draw Mode & Clear Draw buttons
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Brush drawing mode selector
                IconButton(
                    onClick = { drawingModeEnabled = !drawingModeEnabled },
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            if (drawingModeEnabled) Color.Yellow.copy(alpha = 0.2f) else Color(0xFF141A28),
                            RoundedCornerShape(8.dp)
                        )
                        .border(1.dp, if (drawingModeEnabled) Color.Yellow else Color(0xFF1E273A), RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Brush,
                        contentDescription = "Draw Mode",
                        tint = if (drawingModeEnabled) Color.Yellow else Color.LightGray,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Maximize chart toggle button (Zoom)
                IconButton(
                    onClick = { chartMaximized = !chartMaximized },
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            if (chartMaximized) Color(0xFF00E5FF).copy(alpha = 0.2f) else Color(0xFF141A28),
                            RoundedCornerShape(8.dp)
                        )
                        .border(1.dp, if (chartMaximized) Color(0xFF00E5FF) else Color(0xFF1E273A), RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = if (chartMaximized) "➖" else "➕",
                        color = if (chartMaximized) Color(0xFF00E5FF) else Color.LightGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Delete drawings button
                IconButton(
                    onClick = { viewModel.updateDrawings("[]") },
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF261820), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFF3F1924), RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear Drawings",
                        tint = Color(0xFFFF1744),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // 4. Drawing instruction hint and Tool Selection when active
        if (drawingModeEnabled) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Yellow.copy(alpha = 0.12f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "✍️ " + Localizer.translate("drawing_tools", lang),
                        color = Color.Yellow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { selectedDrawingTool = "LINE" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedDrawingTool == "LINE") Color.Yellow else Color(0xFF1A2234)
                            ),
                            modifier = Modifier.height(28.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) {
                            Text(
                                text = "Çizgi (Line)",
                                color = if (selectedDrawingTool == "LINE") Color.Black else Color.LightGray,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { selectedDrawingTool = "RAY" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedDrawingTool == "RAY") Color.Yellow else Color(0xFF1A2234)
                            ),
                            modifier = Modifier.height(28.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) {
                            Text(
                                text = "Işın (Ray)",
                                color = if (selectedDrawingTool == "RAY") Color.Black else Color.LightGray,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { selectedDrawingTool = "HORIZONTAL" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedDrawingTool == "HORIZONTAL") Color.Yellow else Color(0xFF1A2234)
                            ),
                            modifier = Modifier.height(28.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) {
                            Text(
                                text = "Yatay (H-Line)",
                                color = if (selectedDrawingTool == "HORIZONTAL") Color.Black else Color.LightGray,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 5. Candlestick Chart Window (Maximized or Standard height)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (chartMaximized) 420.dp else 260.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFF1E2638), RoundedCornerShape(12.dp))
        ) {
            CandleChart(
                candles = activeCandles,
                sma10 = sma10Values,
                sma20 = sma20Values,
                rsi = rsiValues,
                showSma10 = showSma10,
                showSma20 = showSma20,
                showRsi = showRsi,
                drawings = drawingsList,
                drawingModeEnabled = drawingModeEnabled,
                selectedDrawingTool = selectedDrawingTool,
                onDrawLineAdded = { newDrawing ->
                    val updated = drawingsList + newDrawing
                    viewModel.updateDrawings(serializeDrawings(updated))
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // 6. Dual Mode Trading Tab Bar (Spot vs Leverage)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF141A28), RoundedCornerShape(10.dp))
                .padding(4.dp)
        ) {
            Button(
                onClick = { tradingModeTab = 0 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (tradingModeTab == 0) MaterialTheme.colorScheme.primary else Color.Transparent
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = Localizer.translate("spot_trade", lang),
                    color = if (tradingModeTab == 0) Color.Black else Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Button(
                onClick = { tradingModeTab = 1 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (tradingModeTab == 1) MaterialTheme.colorScheme.primary else Color.Transparent
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = Localizer.translate("leverage_trade", lang),
                    color = if (tradingModeTab == 1) Color.Black else Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        // Yapay Zeka Teknik Analiz Öngörü Modülü
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF111622)),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF1F293D), RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🧠 Yapay Zeka Teknik Analiz Sinyali",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "%65 Başarı",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (aiAnalysisResult.isNotEmpty()) {
                    Text(
                        text = aiAnalysisResult,
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Button(
                    onClick = {
                        aiAnalysisLoading = true
                        viewModel.runAiAnalysis(selectedAsset) { result ->
                            aiAnalysisResult = result
                            aiAnalysisLoading = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(34.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    if (aiAnalysisLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                    } else {
                        Text("Pariteyi Analiz Et / Forecast $selectedAsset", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 7. Trading Terminals
        if (tradingModeTab == 0) {
            // SPOT TRADING TERMINAL
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141A28)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1E273A), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "SPOT " + Localizer.translate("available_cash", lang) + ": $${String.format("%,.2f", playerTraderState?.cash ?: 0.0)}",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = tradeQuantity,
                        onValueChange = { viewModel.setTradeQuantity(it) },
                        label = { Text("Miktar / Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color(0xFF1E273A)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Take Profit and Stop Loss fields for Spot Order
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = spotTakeProfitInput,
                            onValueChange = { spotTakeProfitInput = it },
                            label = { Text("Kâr Al (TP)", fontSize = 10.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF00E676),
                                unfocusedBorderColor = Color(0xFF1E273A)
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = spotStopLossInput,
                            onValueChange = { spotStopLossInput = it },
                            label = { Text("Zarar Durdur (SL)", fontSize = 10.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFFF1744),
                                unfocusedBorderColor = Color(0xFF1E273A)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Spot positions held summary
                    if (activeSpotPosition != null) {
                        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2234)), modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Spot Varlık: ${String.format("%.4f", activeSpotPosition.quantity)} $selectedAsset", color = Color.White, fontSize = 12.sp)
                                Text("Ort: $${String.format("%.2f", activeSpotPosition.averageEntryPrice)}", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                val tp = spotTakeProfitInput.toDoubleOrNull() ?: 0.0
                                val sl = spotStopLossInput.toDoubleOrNull() ?: 0.0
                                viewModel.buyAsset(takeProfit = tp, stopLoss = sl)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(text = Localizer.translate("buy", lang), color = Color.Black, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val tp = spotTakeProfitInput.toDoubleOrNull() ?: 0.0
                                val sl = spotStopLossInput.toDoubleOrNull() ?: 0.0
                                viewModel.sellAsset(takeProfit = tp, stopLoss = sl)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(text = Localizer.translate("sell", lang), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // LEVERAGE FUTURES TERMINAL
            val marginDouble = marginAmountInput.toDoubleOrNull() ?: 0.0
            val liqPreviewPrice = if (marginDouble > 0) {
                if (leverageIsLong) {
                    latestPrice * (1.0 - (1.0 / selectedLeverageMultiplier) + 0.03)
                } else {
                    latestPrice * (1.0 + (1.0 / selectedLeverageMultiplier) - 0.03)
                }
            } else 0.0

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141A28)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1E273A), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "FUTURES " + Localizer.translate("available_cash", lang) + ": $${String.format("%,.2f", playerTraderState?.cash ?: 0.0)}",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Long / Short toggle buttons
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { leverageIsLong = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (leverageIsLong) Color(0xFF00E676) else Color(0xFF1E2638)
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(Localizer.translate("long", lang), color = if (leverageIsLong) Color.Black else Color.Gray, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { leverageIsLong = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!leverageIsLong) Color(0xFFFF1744) else Color(0xFF1E2638)
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(Localizer.translate("short", lang), color = if (!leverageIsLong) Color.White else Color.Gray, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Leverage multiplier slider / chips selection
                    Text(
                        text = Localizer.translate("leverage_multiplier", lang) + ": ${selectedLeverageMultiplier}x",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(2, 5, 10, 25, 50, 100).forEach { lev ->
                            val active = lev == selectedLeverageMultiplier
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (active) MaterialTheme.colorScheme.primary else Color(0xFF1E2638))
                                    .clickable { selectedLeverageMultiplier = lev }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${lev}x",
                                    color = if (active) Color.Black else Color.LightGray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Margin amount input
                    OutlinedTextField(
                        value = marginAmountInput,
                        onValueChange = { marginAmountInput = it },
                        label = { Text(Localizer.translate("margin_amount", lang)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color(0xFF1E273A)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Take Profit and Stop Loss inputs for Leverage Order
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = levTakeProfitInput,
                            onValueChange = { levTakeProfitInput = it },
                            label = { Text("Kâr Al (TP)", fontSize = 10.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF00E676),
                                unfocusedBorderColor = Color(0xFF1E273A)
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = levStopLossInput,
                            onValueChange = { levStopLossInput = it },
                            label = { Text("Zarar Durdur (SL)", fontSize = 10.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFFF1744),
                                unfocusedBorderColor = Color(0xFF1E273A)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Liquidation Preview Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1A14)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF3F2F1C), RoundedCornerShape(8.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "💥 " + Localizer.translate("liquidation_price", lang) + ":",
                                color = Color(0xFFFF9100),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$${String.format("%,.2f", liqPreviewPrice)}",
                                color = Color(0xFFFF9100),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val marginDouble = marginAmountInput.toDoubleOrNull() ?: 0.0
                            val tp = levTakeProfitInput.toDoubleOrNull() ?: 0.0
                            val sl = levStopLossInput.toDoubleOrNull() ?: 0.0
                            if (marginDouble > 0) {
                                viewModel.executeUserLeverageTrade(
                                    symbol = selectedAsset,
                                    isLong = leverageIsLong,
                                    marginAmount = marginDouble,
                                    leverage = selectedLeverageMultiplier,
                                    takeProfit = tp,
                                    stopLoss = sl
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (leverageIsLong) Color(0xFF00E676) else Color(0xFFFF1744)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "POZİSYON AÇ / OPEN POSITION",
                            color = if (leverageIsLong) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 8. Active Leverage Positions Header & List
        if (activeLeveragePositions.isNotEmpty()) {
            Text(
                text = Localizer.translate("active_positions", lang),
                color = Color.LightGray,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            activeLeveragePositions.forEach { pos ->
                val pnlDiff = if (pos.isLong) {
                    latestPrice - pos.averageEntryPrice
                } else {
                    pos.averageEntryPrice - latestPrice
                }
                val pnlVal = pnlDiff * pos.quantity
                val pnlColor = if (pnlVal >= 0) Color(0xFF00E676) else Color(0xFFFF1744)

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141A28)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF1E273A), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = if (pos.isLong) "LONG" else "SHORT",
                                    color = if (pos.isLong) Color(0xFF00E676) else Color(0xFFFF1744),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "${pos.leverage}x",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = pos.symbol,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                            }

                            // PnL display
                            Text(
                                text = "PnL: ${if (pnlVal >= 0) "+" else ""}$${String.format("%.2f", pnlVal)}",
                                color = pnlColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Divider(color = Color(0xFF1E273A))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Giriş / Entry", color = Color.Gray, fontSize = 10.sp)
                                Text("$${String.format("%,.2f", pos.averageEntryPrice)}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Teminat / Margin", color = Color.Gray, fontSize = 10.sp)
                                Text("$${String.format("%,.2f", pos.margin)}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Likidasyon / Liq", color = Color(0xFFFF9100), fontSize = 10.sp)
                                Text("$${String.format("%,.2f", pos.liquidationPrice)}", color = Color(0xFFFF9100), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = { viewModel.closeUserLeveragePosition(pos.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF261820)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = Localizer.translate("close_position", lang),
                                color = Color(0xFFFF1744),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
