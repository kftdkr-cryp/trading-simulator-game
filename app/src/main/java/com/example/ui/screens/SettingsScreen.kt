package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.GameViewModel
import com.example.ui.Localizer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settingsState.collectAsState()
    val lang = settings?.selectedLanguage ?: "TR"

    val scrollState = rememberScrollState()
    var showResetDialog by remember { mutableStateOf(false) }

    // Confirm reset dialog
    if (showResetDialog) {
        Dialog(onDismissRequest = { showResetDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141A28)),
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF1E2638), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = Localizer.translate("reset_confirm_title", lang),
                        color = Color(0xFFFF5252),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = Localizer.translate("reset_confirm_desc", lang),
                        color = Color.Gray,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { showResetDialog = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) { Text(Localizer.translate("cancel", lang)) }
                        Button(
                            onClick = {
                                showResetDialog = false
                                viewModel.resetGame()
                                onBack()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                            shape = RoundedCornerShape(10.dp)
                        ) { Text(Localizer.translate("reset_confirm_btn", lang), color = Color.White, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B0E14))
            .verticalScroll(scrollState)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF141A28))
                .padding(horizontal = 8.dp, vertical = 12.dp)
                .statusBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(Localizer.translate("settings", lang), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

            // Language Selection
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141A28)),
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF1E2638), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Language, contentDescription = "Language", tint = Color(0xFFFFC107), modifier = Modifier.size(24.dp))
                        Text(Localizer.translate("language", lang), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    val languages = listOf(
                        Pair("TR", "🇹🇷 Türkçe"),
                        Pair("EN", "🇺🇸 English"),
                        Pair("HI", "🇮🇳 हिन्दी"),
                        Pair("ZH", "🇨🇳 中文"),
                        Pair("FR", "🇫🇷 Français"),
                        Pair("RU", "🇷🇺 Русский"),
                        Pair("AZ", "🇦🇿 Azərbaycanca")
                    )

                    languages.chunked(2).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { (code, label) ->
                                val isSelected = settings?.selectedLanguage == code
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) Color(0xFF1E2D4A) else Color(0xFF0F141F))
                                        .border(
                                            1.5.dp,
                                            if (isSelected) Color(0xFFFFC107) else Color(0xFF1E2638),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { viewModel.updateLanguage(code) }
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) Color(0xFFFFC107) else Color(0xFFB0BEC5),
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                            if (row.size < 2) Spacer(modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // Market Speed
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141A28)),
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF1E2638), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Speed, contentDescription = null, tint = Color(0xFF00BCD4), modifier = Modifier.size(24.dp))
                        Text(Localizer.translate("market_speed", lang), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    val speeds = listOf(
                        Triple(6000L, Localizer.translate("speed_slow", lang), Color(0xFF4CAF50)),
                        Triple(3000L, Localizer.translate("speed_normal", lang), Color(0xFFFFC107)),
                        Triple(1500L, Localizer.translate("speed_fast", lang), Color(0xFFFF9800)),
                        Triple(500L, Localizer.translate("speed_turbo", lang), Color(0xFFFF5252))
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        speeds.forEach { (speed, label, color) ->
                            val isSelected = settings?.marketSpeed == speed
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) color.copy(alpha = 0.15f) else Color(0xFF0F141F))
                                    .border(1.5.dp, if (isSelected) color else Color(0xFF1E2638), RoundedCornerShape(10.dp))
                                    .clickable { viewModel.updateMarketSpeed(speed) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(label, color = if (isSelected) color else Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Reset Game
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141A28)),
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF3A1414), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(24.dp))
                        Text(Localizer.translate("reset_game", lang), color = Color(0xFFFF5252), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = Localizer.translate("reset_game_desc", lang),
                        color = Color.Gray,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                    Button(
                        onClick = { showResetDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(Localizer.translate("reset_game_btn", lang), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // About
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141A28)),
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF1E2638), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Info, contentDescription = "About", tint = Color(0xFFE040FB), modifier = Modifier.size(24.dp))
                        Text(Localizer.translate("about", lang), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("MARGIN CALL v2.0", color = Color(0xFFFFC107), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(Localizer.translate("about_desc", lang), color = Color.Gray, fontSize = 12.sp, lineHeight = 16.sp)
                }
            }
        }
    }
}
