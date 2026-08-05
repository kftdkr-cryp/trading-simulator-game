package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Trader

@Composable
fun TraderListItem(
    trader: Trader,
    onClick: () -> Unit
) {
    val isPlayer = trader.isPlayer
    
    // Archetype Localization Map
    val strategyName = when (trader.archetype) {
        "TREND_FOLLOWER" -> "Trend Takipçisi"
        "CONTRARIAN" -> "Karşıt Yatırımcı"
        "SCALPER" -> "Kısa Vadeci (Scalp)"
        "WHALE" -> "Balina (Büyük Oyuncu)"
        "PANIC_SELLER" -> "Panik Al-Sat"
        "HODLER" -> "HODL Odaklı"
        "CHAOS" -> "Rastgele (Kaos)"
        "USER_CHOICE" -> "Siz (Kullanıcı)"
        else -> trader.archetype
    }

    val strategyColor = when (trader.archetype) {
        "WHALE" -> Color(0xFF00E676)       // Green
        "TREND_FOLLOWER" -> Color(0xFF00B0FF) // Blue
        "CONTRARIAN" -> Color(0xFFFF9100)    // Orange
        "HODLER" -> Color(0xFFFFD600)        // Gold
        "SCALPER" -> Color(0xFFE040FB)       // Purple
        "PANIC_SELLER" -> Color(0xFFFF1744)  // Red
        "USER_CHOICE" -> Color(0xFFFFC107)    // Gold
        else -> Color.Gray
    }

    // Highlighting style if the user
    val backgroundBrush = if (isPlayer) {
        Brush.horizontalGradient(
            colors = listOf(Color(0xFF261D10), Color(0xFF141926))
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(Color(0xFF101420), Color(0xFF101420))
        )
    }

    val borderModifier = if (isPlayer) {
        Modifier.border(1.dp, Color(0xFFFFC107).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
    } else {
        Modifier.border(1.dp, Color(0xFF1E2638).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(borderModifier)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundBrush)
            .clickable(onClick = onClick)
            .padding(14.dp)
            .testTag("trader_item_${trader.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Rank Circle
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            if (isPlayer) Color(0xFFFFC107).copy(alpha = 0.2f)
                            else Color(0xFF1E2638),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#${trader.rank}",
                        color = if (isPlayer) Color(0xFFFFC107) else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = trader.name,
                            color = if (isPlayer) Color(0xFFFFC107) else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        if (isPlayer) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Player",
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Archetype badge
                    Box(
                        modifier = Modifier
                            .background(strategyColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = strategyName,
                            color = strategyColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$${String.format("%,.0f", trader.cash)}",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "%${String.format("%.1f", trader.winRate)} Kazanma",
                        color = if (trader.winRate >= 55) Color(0xFF00E676) else Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Detail",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
