package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Public
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
import com.example.data.NewsLog

@Composable
fun NewsFeedList(
    newsList: List<NewsLog>,
    modifier: Modifier = Modifier
) {
    if (newsList.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Henüz bir sosyal akış veya haber yok. Piyasanın ilerlemesini bekleyin.",
                color = Color.Gray,
                fontSize = 13.sp,
                modifier = Modifier.padding(24.dp)
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier.testTag("news_feed_list"),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(newsList) { news ->
            val isSystem = news.isSystemNews || news.traderName == "SİSTEM" || news.traderName == "Medyatör"
            
            val containerColor = if (isSystem) {
                Color(0xFF1E142F) // Deep purple tint for system
            } else {
                Color(0xFF111522) // Tech dark slate for traders
            }

            val borderColor = if (isSystem) {
                Color(0xFF9C27B0).copy(alpha = 0.3f)
            } else {
                Color(0xFF1E2638)
            }

            val accentColor = when (news.symbol) {
                "MKTX" -> Color(0xFF00E676)
                "SOLR" -> Color(0xFFFFC107)
                "NEOM" -> Color(0xFF00B0FF)
                "VOID" -> Color(0xFFE040FB)
                else -> Color(0xFF90A4AE)
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = containerColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Avatar Circle
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (isSystem) Color(0xFF9C27B0).copy(alpha = 0.2f)
                                else Color(0xFF1E2638),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSystem) {
                            Icon(
                                imageVector = if (news.traderName == "Medyatör") Icons.Default.Public else Icons.Default.NotificationsActive,
                                contentDescription = "System News",
                                tint = if (news.traderName == "Medyatör") Color(0xFF00B0FF) else Color(0xFFFF1744),
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Text(
                                text = news.traderName.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = news.traderName,
                                color = if (isSystem) Color(0xFFCE93D8) else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            
                            // Asset Badge
                            if (news.symbol != "GENEL" && news.symbol.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = news.symbol,
                                        color = accentColor,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = news.message,
                            color = if (isSystem) Color.White else Color(0xFFB0BEC5),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}
