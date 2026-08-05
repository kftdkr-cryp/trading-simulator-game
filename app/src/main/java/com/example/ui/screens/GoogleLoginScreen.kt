package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.GameViewModel
import com.example.ui.Localizer

data class LangItem(val code: String, val flag: String, val name: String)

fun getRegisteredAccounts(context: Context): List<String> {
    val prefs = context.getSharedPreferences("user_accounts", Context.MODE_PRIVATE)
    val accountsStr = prefs.getString("accounts_list", "") ?: ""
    return if (accountsStr.isEmpty()) emptyList() else accountsStr.split(",")
}

fun registerAccount(context: Context, username: String, pass: String): String? {
    val cleanedUsername = username.trim()
    if (cleanedUsername.length < 3) {
        return "Username too short"
    }
    val accounts = getRegisteredAccounts(context).toMutableList()
    if (accounts.size >= 2) {
        return "Limit reached"
    }
    if (accounts.any { it.equals(cleanedUsername, ignoreCase = true) }) {
        return "Already exists"
    }
    accounts.add(cleanedUsername)
    val prefs = context.getSharedPreferences("user_accounts", Context.MODE_PRIVATE)
    prefs.edit()
        .putString("accounts_list", accounts.joinToString(","))
        .putString("password_$cleanedUsername", pass)
        .apply()
    return null
}

fun checkLogin(context: Context, username: String, pass: String): Boolean {
    val cleanedUsername = username.trim()
    val prefs = context.getSharedPreferences("user_accounts", Context.MODE_PRIVATE)
    val savedPass = prefs.getString("password_$cleanedUsername", null)
    return savedPass != null && savedPass == pass
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleLoginScreen(viewModel: GameViewModel) {
    val settings by viewModel.settingsState.collectAsState()
    val lang = settings?.selectedLanguage ?: "TR"

    val context = LocalContext.current
    
    var isRegisterMode by remember { mutableStateOf(false) }
    var usernameInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var successMsg by remember { mutableStateOf<String?>(null) }
    
    var expandedLangDropdown by remember { mutableStateOf(false) }

    val languages = listOf(
        LangItem("TR", "🇹🇷", "Türkçe"),
        LangItem("EN", "🇺🇸", "English"),
        LangItem("HI", "🇮🇳", "हिन्दी"),
        LangItem("ZH", "🇨🇳", "中文"),
        LangItem("FR", "🇫🇷", "Français"),
        LangItem("RU", "🇷🇺", "Русский"),
        LangItem("AZ", "🇦🇿", "Azərbaycanca"),
        LangItem("ES", "🇪🇸", "Español"),
        LangItem("TH", "🇹🇭", "ไทย"),
        LangItem("DE", "🇩🇪", "Deutsch")
    )

    val currentLangItem = languages.firstOrNull { it.code == lang } ?: languages[0]

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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Language Selection dropdown at the very top of the login screen
            Box(modifier = Modifier.wrapContentSize()) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF141A28))
                        .border(1.dp, Color(0xFF1E273A), RoundedCornerShape(20.dp))
                        .clickable { expandedLangDropdown = true }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Language",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${currentLangItem.flag} ${currentLangItem.name}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }

                DropdownMenu(
                    expanded = expandedLangDropdown,
                    onDismissRequest = { expandedLangDropdown = false },
                    modifier = Modifier.background(Color(0xFF141A28))
                ) {
                    languages.forEach { item ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "${item.flag}  ${item.name}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            onClick = {
                                viewModel.updateLanguage(item.code)
                                expandedLangDropdown = false
                            },
                            modifier = Modifier.background(
                                if (item.code == lang) Color.White.copy(alpha = 0.05f) else Color.Transparent
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Glowing Brand Logo
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
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
                        text = "₮",
                        color = Color.Black,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Titles
            Text(
                text = Localizer.translate("app_title", lang),
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            val welcomeText = if (lang == "TR") {
                "200 yapay zeka rakip ile rekabet etmeye hazır mısın? Giriş yap veya yeni hesap oluştur."
            } else {
                "Ready to compete against 200 AI rivals? Log in or create a new account."
            }
            Text(
                text = welcomeText,
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Tab Buttons: Giriş Yap / Kayıt Ol
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF141A28), RoundedCornerShape(10.dp))
                    .padding(4.dp)
            ) {
                Button(
                    onClick = { 
                        isRegisterMode = false 
                        errorMsg = null
                        successMsg = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isRegisterMode) MaterialTheme.colorScheme.primary else Color.Transparent
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (lang == "TR") "Giriş Yap" else "Log In",
                        color = if (!isRegisterMode) Color.Black else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Button(
                    onClick = { 
                        isRegisterMode = true 
                        errorMsg = null
                        successMsg = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRegisterMode) MaterialTheme.colorScheme.primary else Color.Transparent
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (lang == "TR") "Kayıt Ol" else "Register",
                        color = if (isRegisterMode) Color.Black else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            // Text Fields Card container
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141A28)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1E273A), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Username input
                    OutlinedTextField(
                        value = usernameInput,
                        onValueChange = { 
                            usernameInput = it 
                            errorMsg = null
                            successMsg = null
                        },
                        label = { Text(if (lang == "TR") "Kullanıcı Adı" else "Username") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = "User") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color(0xFF1E273A)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Password input
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { 
                            passwordInput = it 
                            errorMsg = null
                            successMsg = null
                        },
                        label = { Text(if (lang == "TR") "Şifre" else "Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle Visibility"
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color(0xFF1E273A)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Error or Success messages
                    errorMsg?.let { msg ->
                        Text(
                            text = "❌ $msg",
                            color = Color(0xFFFF1744),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    successMsg?.let { msg ->
                        Text(
                            text = "✅ $msg",
                            color = Color(0xFF00E676),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    // Main Action Button
                    Button(
                        onClick = {
                            val u = usernameInput.trim()
                            val p = passwordInput
                            if (u.isEmpty() || p.isEmpty()) {
                                errorMsg = if (lang == "TR") "Lütfen tüm alanları doldurun." else "Please fill in all fields."
                                return@Button
                            }

                            if (isRegisterMode) {
                                val err = registerAccount(context, u, p)
                                if (err != null) {
                                    errorMsg = when (err) {
                                        "Limit reached" -> if (lang == "TR") "Cihaz başına maksimum 2 hesap açılabilir!" else "Maximum of 2 accounts can be registered per device!"
                                        "Already exists" -> if (lang == "TR") "Bu kullanıcı adı zaten alınmış!" else "Username is already taken!"
                                        "Username too short" -> if (lang == "TR") "Kullanıcı adı en az 3 karakter olmalıdır!" else "Username must be at least 3 characters!"
                                        else -> err
                                    }
                                } else {
                                    successMsg = if (lang == "TR") "Kayıt Başarılı! Şimdi giriş yapabilirsiniz." else "Registration Successful! You can now log in."
                                    isRegisterMode = false
                                    passwordInput = ""
                                }
                            } else {
                                val success = checkLogin(context, u, p)
                                if (success) {
                                    viewModel.syncGoogleProfile(email = u, name = u, avatarUrl = "")
                                } else {
                                    errorMsg = if (lang == "TR") "Hatalı kullanıcı adı veya şifre!" else "Incorrect username or password!"
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = if (isRegisterMode) {
                                if (lang == "TR") "Kayıt Ol" else "Register"
                            } else {
                                if (lang == "TR") "Giriş Yap" else "Log In"
                            },
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            // Devices Stats indicator
            val registeredCount = getRegisteredAccounts(context).size
            Text(
                text = if (lang == "TR") "Cihazdaki kayıtlı hesaplar: $registeredCount / 2" else "Registered accounts on device: $registeredCount / 2",
                color = Color.Gray,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
