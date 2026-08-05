package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.GameViewModel
import com.example.ui.components.NewsFeedList

@Composable
fun NewsScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val newsLogs by viewModel.newsLogs.collectAsState()
    var selectedSymbolFilter by remember { mutableStateOf<String?>(null) }

    val symbolsFilter = listOf(
        Pair("Tümü", null),
        Pair("MKTX", "MKTX"),
        Pair("SOLR", "SOLR"),
        Pair("NEOM", "NEOM"),
        Pair("VOID", "VOID"),
        Pair("Genel", "GENEL")
    )

    // Filtered news
    val filteredNews = remember(newsLogs, selectedSymbolFilter) {
        if (selectedSymbolFilter == null) {
            newsLogs
        } else {
            newsLogs.filter { it.symbol == selectedSymbolFilter }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B0E14))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SOSYAL AKIŞ VE PİYASA ANALİZLERİ",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Yapay Zeka Yatırımcı Akışı",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            }

            // Quick reset game option
            IconButton(
                onClick = { viewModel.resetGame() },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color(0xFF26181F),
                    contentColor = Color(0xFFFF1744)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Reset Game",
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Symbol filter badges row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            symbolsFilter.forEach { (label, symbol) ->
                val active = selectedSymbolFilter == symbol
                val color = when (symbol) {
                    "MKTX" -> Color(0xFF00E676)
                    "SOLR" -> Color(0xFFFFC107)
                    "NEOM" -> Color(0xFF00B0FF)
                    "VOID" -> Color(0xFFE040FB)
                    else -> Color(0xFF90A4AE)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (active) color.copy(alpha = 0.2f) else Color(0xFF141A28))
                        .clickable { selectedSymbolFilter = symbol }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (active) color else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // News List
        Box(modifier = Modifier.weight(1f)) {
            NewsFeedList(newsList = filteredNews, modifier = Modifier.fillMaxSize())
        }
    }
}
