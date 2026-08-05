package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.GameViewModel

data class LanguageItem(
    val code: String,
    val flag: String,
    val name: String
)

@Composable
fun LanguageSelectionScreen(
    viewModel: GameViewModel,
    onLanguageSelected: () -> Unit
) {
    val context = LocalContext.current
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F1420),
                        Color(0xFF07090E)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header Icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color(0xFF1E2638), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "Language Selector",
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(36.dp)
                )
            }

            // Title
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "LÜTFEN DİL SEÇİN",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "PLEASE SELECT LANGUAGE",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Grid of 10 languages
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                items(languages) { item ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF141A28)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .clickable {
                                // Save selection in ViewModel
                                viewModel.updateLanguage(item.code)
                                // Save selection flag in SharedPreferences
                                val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                                prefs.edit()
                                    .putBoolean("has_chosen_lang", true)
                                    .putString("selected_lang", item.code)
                                    .apply()
                                // Proceed
                                onLanguageSelected()
                            }
                            .border(1.dp, Color(0xFF1E273A), RoundedCornerShape(12.dp)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text(
                                text = item.flag,
                                fontSize = 24.sp,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Text(
                                text = item.name,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "MARGIN CALL — PREMIUM TRADING GAME",
                color = Color.Gray.copy(alpha = 0.5f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )
        }
    }
}
