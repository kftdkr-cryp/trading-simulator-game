package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.GameViewModel
import com.example.ui.Localizer

data class LangItem(val code: String, val flag: String, val label: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(viewModel: GameViewModel) {
    val settings by viewModel.settingsState.collectAsState()
    val lang = settings?.selectedLanguage ?: "TR"
    val accountCount by viewModel.accountCount.collectAsState()

    var mode by remember { mutableStateOf("choose") } // "choose", "login", "register"
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var expandedLang by remember { mutableStateOf(false) }

    val languages = listOf(
        LangItem("TR", "🇹🇷", "Türkçe"),
        LangItem("EN", "🇺🇸", "English"),
        LangItem("HI", "🇮🇳", "हिन्दी"),
        LangItem("ZH", "🇨🇳", "中文"),
        LangItem("FR", "🇫🇷", "Français"),
        LangItem("RU", "🇷🇺", "Русский"),
        LangItem("AZ", "🇦🇿", "Azərbaycanca")
    )
    val currentLang = languages.firstOrNull { it.code == lang } ?: languages[0]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0F1420), Color(0xFF07090E)))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Language selector
            Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd).fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF141A28))
                            .border(1.dp, Color(0xFF1E273A), RoundedCornerShape(20.dp))
                            .clickable { expandedLang = true }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(18.dp))
                        Text("${currentLang.flag} ${currentLang.label}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                    }
                    DropdownMenu(
                        expanded = expandedLang,
                        onDismissRequest = { expandedLang = false },
                        modifier = Modifier.background(Color(0xFF141A28))
                    ) {
                        languages.forEach { l ->
                            DropdownMenuItem(
                                text = { Text("${l.flag}  ${l.label}", color = if (l.code == lang) Color(0xFFFFC107) else Color.White) },
                                onClick = { viewModel.updateLanguage(l.code); expandedLang = false }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Logo / Title
            Text(
                text = "📈 MARGIN CALL",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFFFC107),
                letterSpacing = 2.sp
            )
            Text(
                text = Localizer.translate("google_login_title", lang),
                fontSize = 15.sp,
                color = Color(0xFFB0BEC5),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            AnimatedContent(targetState = mode, label = "auth_mode") { currentMode ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    when (currentMode) {
                        "choose" -> {
                            // Main buttons
                            Button(
                                onClick = { mode = "login"; errorMsg = null },
                                modifier = Modifier.fillMaxWidth().height(54.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.Login, contentDescription = null, tint = Color.White)
                                Spacer(Modifier.width(10.dp))
                                Text(Localizer.translate("login_btn", lang), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            if (accountCount < 2) {
                                OutlinedButton(
                                    onClick = { mode = "register"; errorMsg = null },
                                    modifier = Modifier.fillMaxWidth().height(54.dp),
                                    border = BorderStroke(1.5.dp, Color(0xFFFFC107)),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color(0xFFFFC107))
                                    Spacer(Modifier.width(10.dp))
                                    Text(Localizer.translate("register_btn", lang), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFC107))
                                }
                            } else {
                                // Max accounts reached
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2638)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = Localizer.translate("max_accounts_info", lang),
                                        color = Color(0xFFB0BEC5),
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }

                            Text(
                                text = "$accountCount/2 ${Localizer.translate("accounts_on_device", lang)}",
                                color = Color(0xFF546E7A),
                                fontSize = 11.sp
                            )
                        }

                        "login" -> {
                            Text(Localizer.translate("login_title", lang), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                            OutlinedTextField(
                                value = username,
                                onValueChange = { username = it; errorMsg = null },
                                label = { Text(Localizer.translate("username", lang), color = Color.Gray) },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFFC107),
                                    unfocusedBorderColor = Color(0xFF1E2638),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it; errorMsg = null },
                                label = { Text(Localizer.translate("password", lang), color = Color.Gray) },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray) },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, tint = Color.Gray)
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFFC107),
                                    unfocusedBorderColor = Color(0xFF1E2638),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            if (errorMsg != null) {
                                Text(errorMsg!!, color = Color(0xFFFF5252), fontSize = 13.sp, textAlign = TextAlign.Center)
                            }

                            Button(
                                onClick = {
                                    if (username.isBlank() || password.isBlank()) { errorMsg = "Kullanıcı adı ve şifre gerekli"; return@Button }
                                    isLoading = true
                                    viewModel.login(username.trim(), password) { err ->
                                        isLoading = false
                                        if (err != null) errorMsg = err
                                    }
                                },
                                enabled = !isLoading,
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                else Text(Localizer.translate("login_btn", lang), fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            TextButton(onClick = { mode = "choose"; errorMsg = null }) {
                                Text("← ${Localizer.translate("back", lang)}", color = Color(0xFFB0BEC5))
                            }
                        }

                        "register" -> {
                            Text(Localizer.translate("register_title", lang), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                            OutlinedTextField(
                                value = displayName,
                                onValueChange = { displayName = it; errorMsg = null },
                                label = { Text(Localizer.translate("display_name", lang), color = Color.Gray) },
                                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = Color.Gray) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFFC107),
                                    unfocusedBorderColor = Color(0xFF1E2638),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = username,
                                onValueChange = { username = it; errorMsg = null },
                                label = { Text(Localizer.translate("username", lang), color = Color.Gray) },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFFC107),
                                    unfocusedBorderColor = Color(0xFF1E2638),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it; errorMsg = null },
                                label = { Text(Localizer.translate("password", lang), color = Color.Gray) },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray) },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, tint = Color.Gray)
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFFC107),
                                    unfocusedBorderColor = Color(0xFF1E2638),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it; errorMsg = null },
                                label = { Text(Localizer.translate("confirm_password", lang), color = Color.Gray) },
                                leadingIcon = { Icon(Icons.Default.LockOpen, contentDescription = null, tint = Color.Gray) },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFFC107),
                                    unfocusedBorderColor = Color(0xFF1E2638),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            if (errorMsg != null) {
                                Text(errorMsg!!, color = Color(0xFFFF5252), fontSize = 13.sp, textAlign = TextAlign.Center)
                            }

                            Button(
                                onClick = {
                                    if (password != confirmPassword) { errorMsg = "Şifreler uyuşmuyor"; return@Button }
                                    if (username.isBlank()) { errorMsg = "Kullanıcı adı gerekli"; return@Button }
                                    if (password.length < 4) { errorMsg = "Şifre en az 4 karakter olmalı"; return@Button }
                                    isLoading = true
                                    viewModel.register(username.trim(), password, displayName) { err ->
                                        isLoading = false
                                        if (err != null) { errorMsg = err }
                                        else { mode = "login"; errorMsg = null; username = ""; password = "" }
                                    }
                                },
                                enabled = !isLoading,
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                else Text(Localizer.translate("register_btn", lang), fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            TextButton(onClick = { mode = "choose"; errorMsg = null }) {
                                Text("← ${Localizer.translate("back", lang)}", color = Color(0xFFB0BEC5))
                            }
                        }
                    }
                }
            }
        }
    }
}
