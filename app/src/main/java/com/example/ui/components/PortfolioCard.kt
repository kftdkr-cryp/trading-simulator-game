package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TraderPosition
import com.example.ui.Localizer

@Composable
fun PortfolioCard(
    cash: Double,
    positions: List<TraderPosition>,
    currentPrices: Map<String, Double>,
    lang: String = "TR"
) {
    // Calculate total assets value
    val holdingsValue = positions.sumOf { pos ->
        val price = currentPrices[pos.symbol] ?: 0.0
        if (pos.isLeverage) {
            val diff = if (pos.isLong) (price - pos.averageEntryPrice) else (pos.averageEntryPrice - price)
            val unrealizedPnl = diff * pos.quantity
            (pos.margin + unrealizedPnl).coerceAtLeast(0.0)
        } else {
            pos.quantity * price
        }
    }
    val totalEquity = cash + holdingsValue
    
    // Player starts with 0 capital, so let's show growth starting from a $100 baseline or relative to 0
    val baseline = 100.0
    val profitPercent = if (totalEquity > 0) (totalEquity / baseline) * 100.0 else 0.0

    val isProfit = totalEquity >= 0
    val trendColor = if (isProfit) Color(0xFF00E676) else Color(0xFFFF1744)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF141A28)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF1E273A), RoundedCornerShape(20.dp))
            .testTag("portfolio_card")
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = Localizer.translate("total_portfolio_value", lang).uppercase(),
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$${String.format("%,.2f", totalEquity)}",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Icon(
                    imageVector = Icons.Default.Wallet,
                    contentDescription = "Wallet",
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = Localizer.translate("available_cash", lang), color = Color.Gray, fontSize = 12.sp)
                    Text(
                        text = "$${String.format("%,.2f", cash)}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Total Return Score", color = Color.Gray, fontSize = 12.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isProfit) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = "Profit Trend",
                            tint = trendColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${if (isProfit) "+" else ""}${String.format("%.1f", profitPercent)}%",
                            color = trendColor,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = Color(0xFF1E273A))
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = Localizer.translate("active_positions", lang),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (positions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Henüz açık pozisyonunuz yok. / No active positions yet.",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    positions.forEach { pos ->
                        val currentPrice = currentPrices[pos.symbol] ?: 0.0
                        val currentVal = pos.quantity * currentPrice
                        val costVal = pos.quantity * pos.averageEntryPrice
                        val pnl = if (pos.isLeverage) {
                            // Futures leverage PnL
                            val diff = if (pos.isLong) (currentPrice - pos.averageEntryPrice) else (pos.averageEntryPrice - currentPrice)
                            diff * pos.quantity
                        } else {
                            // Spot PnL
                            currentVal - costVal
                        }
                        val pnlPercent = if (costVal > 0) (pnl / costVal) * 100.0 else 0.0

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0C101B), RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(
                                                when (pos.symbol) {
                                                    "MKTX" -> Color(0xFF00E676)
                                                    "SOLR" -> Color(0xFFFFC107)
                                                    "NEOM" -> Color(0xFF00B0FF)
                                                    else -> Color(0xFFE040FB)
                                                }.copy(alpha = 0.2f),
                                                RoundedCornerShape(6.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = pos.symbol.take(1),
                                            color = when (pos.symbol) {
                                                "MKTX" -> Color(0xFF00E676)
                                                "SOLR" -> Color(0xFFFFC107)
                                                "NEOM" -> Color(0xFF00B0FF)
                                                else -> Color(0xFFE040FB)
                                            },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = pos.symbol + if (pos.isLeverage) " ${pos.leverage}x " + (if (pos.isLong) "LONG" else "SHORT") else " SPOT",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${String.format("%.4f", pos.quantity)} @ $${String.format("%.2f", pos.averageEntryPrice)}",
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (pos.isLeverage) "Margin: $${String.format("%,.2f", pos.margin)}" else "$${String.format("%,.2f", currentVal)}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "PnL: ${if (pnl >= 0) "+" else ""}$${String.format("%.2f", pnl)}",
                                    color = if (pnl >= 0) Color(0xFF00E676) else Color(0xFFFF1744),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
