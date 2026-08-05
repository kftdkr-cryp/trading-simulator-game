package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.GameViewModel
import kotlinx.coroutines.delay
import kotlin.random.Random

// Scene structure for custom cinematic playback
data class StoryScene(
    val id: Int,
    val textTR: String,
    val textEN: String,
    val durationMs: Long
)

@Composable
fun IntroStoryScreen(
    lang: String,
    onComplete: () -> Unit
) {
    var currentSceneIdx by remember { mutableIntStateOf(0) }
    val scenes = listOf(
        StoryScene(
            1,
            "Fakirliğim yüzünden ailem beni terk etti... Karım zengin bir adamla evlendi ve çocuklarımı elimden aldı.",
            "Because of my poverty, my family abandoned me... My wife married a wealthy man and took my kids.",
            5500
        ),
        StoryScene(
            2,
            "Eski, tozlu bilgisayarımın başında gece gündüz para kazanma yollarını aradım... Ve nihayet TRADING'i keşfettim.",
            "I spent days and nights at my old, dusty computer looking for a way to survive... And finally, I discovered TRADING.",
            5500
        ),
        StoryScene(
            3,
            "En sonunda zengin olmam lazım! Başka seçme şansım yok!",
            "Ultimately, I must become rich! I have no other choice!",
            4500
        ),
        StoryScene(
            4,
            "MARGIN CALL\nPROTRADER698 YAPIM SUNAR",
            "MARGIN CALL\nPRESENTED BY PROTRADER698",
            5000
        )
    )

    val currentScene = scenes[currentSceneIdx]

    // Animation progress for scene elements
    val transitionState = rememberInfiniteTransition(label = "particles")
    val heartbeat by transitionState.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heartbeat"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070A0F)),
        contentAlignment = Alignment.Center
    ) {
        // Visual illustration depending on active scene
        when (currentScene.id) {
            1 -> SlumRainIllustration()
            2 -> OldComputerIllustration()
            3 -> DesperationIllustration(heartbeat)
            4 -> TitleRevealIllustration()
        }

        // Subtitle bottom panel with semi-transparent blur style
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(24.dp)
                .background(Color(0xCD0A0E17), RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFF1E273A), RoundedCornerShape(16.dp))
                .clickable {
                    if (currentSceneIdx < scenes.lastIndex) {
                        currentSceneIdx++
                    } else {
                        onComplete()
                    }
                }
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (lang == "TR") currentScene.textTR else currentScene.textEN,
                    color = if (currentScene.id == 3) Color(0xFFFF1744) else Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Scene ${currentSceneIdx + 1}/${scenes.size}",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (lang == "TR") "GEÇ / SKIP" else "SKIP",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { onComplete() }
                                .padding(4.dp)
                        )

                        Text(
                            text = if (currentSceneIdx < scenes.lastIndex) {
                                if (lang == "TR") "SONRAKİ >>" else "NEXT >>"
                            } else {
                                if (lang == "TR") "BAŞLAT >>" else "START >>"
                            },
                            color = Color(0xFFFFC107),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier
                                .background(Color(0xFF1E273A), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OutroStoryScreen(
    lang: String,
    onReset: () -> Unit,
    onContinue: () -> Unit
) {
    var currentSceneIdx by remember { mutableIntStateOf(0) }
    val scenes = listOf(
        StoryScene(
            1,
            "Sonunda başardım... Milyonlarca dolarlık varlığım, lüks villam ve kapıda bekleyen süper arabam var.",
            "I finally made it... I have millions of dollars, a luxury villa, and a supercar waiting outside.",
            6000
        ),
        StoryScene(
            2,
            "Adalet bazen pahalıdır... Çocuklarımı geri almak için hakime en iyi avukatları ve bedelleri ödedim.",
            "Justice can be expensive... I paid top lawyers and costs to secure the custody of my children.",
            6000
        ),
        StoryScene(
            3,
            "Ve sonunda... Çocuklarıma kavuştum. Artık güvendeyiz ve bir aradayız.",
            "And finally... I am reunited with my children. We are safe and together at last.",
            6000
        ),
        StoryScene(
            4,
            "PROTRADER698 YAPIM SUNDU\n\nOYUNU EĞER BEĞENDİYSENİZ KRİPTO PARA İLE KÜÇÜK BAĞIŞLARDA BULUNABİLİRSİNİZ BÖYLECE YENİ OYUNLAR GELECEKTİR.",
            "PRESENTED BY PROTRADER698\n\nIF YOU LIKED THE GAME, YOU CAN MAKE SMALL CRYPTO DONATIONS SO THAT NEW GAMES WILL COME.",
            100000 // Keep last scene visible
        )
    )

    val currentScene = scenes[currentSceneIdx]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070A0F)),
        contentAlignment = Alignment.Center
    ) {
        // Visual illustrations
        when (currentScene.id) {
            1 -> VillaSunsetIllustration()
            2 -> CourtroomIllustration()
            3 -> HappyHuggingIllustration()
            4 -> CreditsIllustration(lang, onReset, onContinue)
        }

        if (currentSceneIdx < 3) {
            // Subtitle panel
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(24.dp)
                    .background(Color(0xCD0A0E17), RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFF1E273A), RoundedCornerShape(16.dp))
                    .clickable {
                        if (currentSceneIdx < 2) {
                            currentSceneIdx++
                        } else {
                            currentSceneIdx = 3
                        }
                    }
                    .padding(20.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (lang == "TR") currentScene.textTR else currentScene.textEN,
                        color = Color(0xFF00E676),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Scene ${currentSceneIdx + 1}/3",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (lang == "TR") "SONA GEÇ / SKIP" else "SKIP TO END",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { currentSceneIdx = 3 }
                                    .padding(4.dp)
                            )

                            Text(
                                text = if (lang == "TR") "SONRAKİ >>" else "NEXT >>",
                                color = Color(0xFFFFC107),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier
                                    .background(Color(0xFF1E273A), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ================= INTRO ILLUSTRATIONS =================

@Composable
fun SlumRainIllustration() {
    val transition = rememberInfiniteTransition(label = "rain")
    val rainAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing)),
        label = "rain"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Background dark gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF020408), Color(0xFF0C1322))
            )
        )

        // Draw cracked ground
        drawLine(
            color = Color(0xFF111726),
            start = Offset(0f, height * 0.75f),
            end = Offset(width, height * 0.75f),
            strokeWidth = 4f
        )

        // Draw broken bench
        drawRect(
            color = Color(0xFF1D1B18),
            topLeft = Offset(width * 0.15f, height * 0.68f),
            size = Size(width * 0.35f, height * 0.02f)
        )
        drawLine(
            color = Color(0xFF1D1B18),
            start = Offset(width * 0.2f, height * 0.7f),
            end = Offset(width * 0.2f, height * 0.75f),
            strokeWidth = 6f
        )
        drawLine(
            color = Color(0xFF1D1B18),
            start = Offset(width * 0.45f, height * 0.7f),
            end = Offset(width * 0.42f, height * 0.75f), // Broken angle!
            strokeWidth = 6f
        )

        // Draw weeping silhouette sitting on the bench
        drawCircle(
            color = Color(0xFF10141D),
            center = Offset(width * 0.28f, height * 0.62f),
            radius = 18f
        )
        // Slumped body
        drawPath(
            path = Path().apply {
                moveTo(width * 0.25f, height * 0.64f)
                lineTo(width * 0.32f, height * 0.64f)
                lineTo(width * 0.35f, height * 0.73f)
                lineTo(width * 0.23f, height * 0.73f)
                close()
            },
            color = Color(0xFF10141D)
        )

        // Draw wife and kid walking away into the warm mansion glow
        val glowCenter = Offset(width * 0.85f, height * 0.45f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xEEFFD54F), Color(0x33FFB300), Color.Transparent),
                center = glowCenter,
                radius = 140f
            ),
            center = glowCenter,
            radius = 140f
        )

        // Silhouettes of wife & child walking away
        drawCircle(
            color = Color(0xFF0F1219),
            center = Offset(width * 0.72f, height * 0.66f),
            radius = 12f
        )
        drawRect(
            color = Color(0xFF0F1219),
            topLeft = Offset(width * 0.70f, height * 0.68f),
            size = Size(16f, 32f)
        )

        drawCircle(
            color = Color(0xFF0F1219),
            center = Offset(width * 0.77f, height * 0.69f),
            radius = 8f
        )
        drawRect(
            color = Color(0xFF0F1219),
            topLeft = Offset(width * 0.76f, height * 0.70f),
            size = Size(10f, 20f)
        )

        // Falling Raindrops
        for (i in 0..60) {
            val rx = (Random(i).nextFloat() * width)
            val ry = ((Random(i).nextFloat() * height + rainAnim * height) % height)
            drawLine(
                color = Color(0x4081D4FA),
                start = Offset(rx, ry),
                end = Offset(rx - 8f, ry + 22f),
                strokeWidth = 2f
            )
        }
    }
}

@Composable
fun OldComputerIllustration() {
    val transition = rememberInfiniteTransition(label = "computer")
    val bulbSwing by transition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bulb"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        drawRect(Color(0xFF080B11))

        // Draw hanging wire and swinging lightbulb
        val pivot = Offset(width * 0.5f, 0f)
        val bulbY = height * 0.3f
        val bulbAngleRad = Math.toRadians(bulbSwing.toDouble())
        val bx = (pivot.x + Math.sin(bulbAngleRad) * bulbY).toFloat()
        val by = (pivot.y + Math.cos(bulbAngleRad) * bulbY).toFloat()

        drawLine(Color.DarkGray, pivot, Offset(bx, by), strokeWidth = 3f)
        drawCircle(Color(0xFFFFFFC107), center = Offset(bx, by), radius = 22f)
        // Light cone
        drawPath(
            path = Path().apply {
                moveTo(bx, by)
                lineTo(bx - 180f, height * 0.85f)
                lineTo(bx + 180f, height * 0.85f)
                close()
            },
            brush = Brush.verticalGradient(
                colors = listOf(Color(0x3BFFC107), Color.Transparent),
                startY = by,
                endY = height * 0.85f
            )
        )

        // Retro computer desk & monitor
        val monitorCenter = Offset(width * 0.5f, height * 0.6f)
        drawRect(
            color = Color(0xFF1B2333),
            topLeft = Offset(monitorCenter.x - 90f, monitorCenter.y - 70f),
            size = Size(180f, 130f),
            style = Stroke(4f)
        )
        // Screen glowing interior with green grid
        drawRect(
            color = Color(0xFF0D141F),
            topLeft = Offset(monitorCenter.x - 84f, monitorCenter.y - 64f),
            size = Size(168f, 118f)
        )

        // Draw fluorescent green stock chart on the retro computer
        drawPath(
            path = Path().apply {
                moveTo(monitorCenter.x - 80f, monitorCenter.y + 30f)
                lineTo(monitorCenter.x - 50f, monitorCenter.y + 10f)
                lineTo(monitorCenter.x - 20f, monitorCenter.y + 40f)
                lineTo(monitorCenter.x + 10f, monitorCenter.y - 20f)
                lineTo(monitorCenter.x + 40f, monitorCenter.y + 10f)
                lineTo(monitorCenter.x + 70f, monitorCenter.y - 45f)
            },
            color = Color(0xFF00E676),
            style = Stroke(4f)
        )

        // Dusty table
        drawLine(
            color = Color(0xFF2A3447),
            start = Offset(width * 0.15f, height * 0.72f),
            end = Offset(width * 0.85f, height * 0.72f),
            strokeWidth = 6f
        )
    }
}

@Composable
fun DesperationIllustration(heartbeat: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF2E080F), Color(0xFF080003)),
                center = Offset(width * 0.5f, height * 0.5f),
                radius = width * 0.8f
            )
        )

        // Draw pulsing high-voltage trading line
        val midY = height * 0.5f
        drawPath(
            path = Path().apply {
                moveTo(0f, midY)
                lineTo(width * 0.25f, midY)
                lineTo(width * 0.35f, midY - 140f * heartbeat)
                lineTo(width * 0.45f, midY + 160f * heartbeat)
                lineTo(width * 0.55f, midY - 80f * heartbeat)
                lineTo(width * 0.65f, midY + 90f * heartbeat)
                lineTo(width * 0.75f, midY)
                lineTo(width, midY)
            },
            color = Color(0xFFFF1744),
            style = Stroke(6f)
        )

        // Glowing text indicator
        drawCircle(
            color = Color(0xFFFF1744),
            center = Offset(width * 0.45f, midY + 160f * heartbeat),
            radius = 16f * heartbeat
        )
    }
}

@Composable
fun TitleRevealIllustration() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutBack),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF04060A)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF141A28))
                    .border(4.dp, Color(0xFFFFC107), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📈",
                    fontSize = 58.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "MARGIN CALL",
                color = Color(0xFFFFC107),
                fontSize = 38.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "PROTRADER698 YAPIM",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}


// ================= OUTRO ILLUSTRATIONS =================

@Composable
fun VillaSunsetIllustration() {
    val transition = rememberInfiniteTransition(label = "sunset")
    val cloudX by transition.animateFloat(
        initialValue = 0f,
        targetValue = 400f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cloud"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Sunset sky background
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF311B92), Color(0xFFD81B60), Color(0xFFFFB300))
            )
        )

        // Draw rolling clouds
        drawCircle(Color(0x33FFFFFF), center = Offset((cloudX - 100f) % (width + 300f) - 100f, height * 0.2f), radius = 60f)
        drawCircle(Color(0x33FFFFFF), center = Offset((cloudX + 150f) % (width + 300f) - 100f, height * 0.25f), radius = 90f)

        // Big golden sun
        drawCircle(
            color = Color(0xFFFFEB3B),
            center = Offset(width * 0.5f, height * 0.55f),
            radius = 80f
        )

        // Ground outline (luxury resort paving)
        drawRect(
            color = Color(0xFF0F111A),
            topLeft = Offset(0f, height * 0.65f),
            size = Size(width, height * 0.35f)
        )

        // Modern villa silhouette
        drawRect(
            color = Color(0xFF1C1E2B),
            topLeft = Offset(width * 0.1f, height * 0.45f),
            size = Size(width * 0.5f, height * 0.2f)
        )
        // Glowing gold windows
        drawRect(Color(0xFFFFD54F), topLeft = Offset(width * 0.15f, height * 0.48f), size = Size(50f, 30f))
        drawRect(Color(0xFFFFD54F), topLeft = Offset(width * 0.3f, height * 0.48f), size = Size(80f, 30f))
        drawRect(Color(0xFFFFD54F), topLeft = Offset(width * 0.45f, height * 0.52f), size = Size(40f, 40f))

        // Luxury sports car silhouette
        drawPath(
            path = Path().apply {
                moveTo(width * 0.68f, height * 0.65f)
                lineTo(width * 0.72f, height * 0.59f)
                lineTo(width * 0.85f, height * 0.59f)
                lineTo(width * 0.92f, height * 0.65f)
                close()
            },
            color = Color(0xFFFF1744) // Shiny Red Supercar!
        )
        drawCircle(Color.Black, center = Offset(width * 0.74f, height * 0.65f), radius = 12f)
        drawCircle(Color.Black, center = Offset(width * 0.86f, height * 0.65f), radius = 12f)
    }
}

@Composable
fun CourtroomIllustration() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Dark rich oak panel gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF1E140F), Color(0xFF0F0906))
            )
        )

        // Scales of justice
        val scaleX = width * 0.5f
        val scaleY = height * 0.4f
        drawLine(Color(0xFFFFD54F), Offset(scaleX, scaleY - 60f), Offset(scaleX, scaleY + 80f), strokeWidth = 8f)
        // Balance beam tipped (tipped in our favor!)
        drawLine(Color(0xFFFFD54F), Offset(scaleX - 110f, scaleY - 30f), Offset(scaleX + 110f, scaleY - 10f), strokeWidth = 6f)

        // Briefcase full of gold stacks
        drawRect(
            color = Color(0xFF3E2723),
            topLeft = Offset(width * 0.25f, height * 0.62f),
            size = Size(width * 0.5f, height * 0.08f)
        )
        // Glowing gold coins inside briefcase
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFF176), Color.Transparent),
                center = Offset(width * 0.5f, height * 0.62f),
                radius = 60f
            ),
            center = Offset(width * 0.5f, height * 0.62f),
            radius = 60f
        )
    }
}

@Composable
fun HappyHuggingIllustration() {
    val infiniteTransition = rememberInfiniteTransition(label = "hearts")
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing)),
        label = "heart"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Rich bright emerald-green / sunset sky representing success and emotional healing
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF1B5E20), Color(0xFF0C2411)),
                center = Offset(width * 0.5f, height * 0.5f),
                radius = width * 0.8f
            )
        )

        // Big golden sun of hope
        drawCircle(Color(0xFFFFF9C4), center = Offset(width * 0.5f, height * 0.45f), radius = 100f)

        // Drawing silhouettes of dad hugging children
        val cx = width * 0.5f
        val cy = height * 0.58f

        // Dad
        drawCircle(Color(0xFF0F1B11), center = Offset(cx, cy - 60f), radius = 24f)
        drawRect(Color(0xFF0F1B11), topLeft = Offset(cx - 24f, cy - 36f), size = Size(48f, 100f))

        // Child 1 (Left) hugging
        drawCircle(Color(0xFF0F1B11), center = Offset(cx - 32f, cy - 20f), radius = 14f)
        drawRect(Color(0xFF0F1B11), topLeft = Offset(cx - 42f, cy - 6f), size = Size(20f, 60f))

        // Child 2 (Right) hugging
        drawCircle(Color(0xFF0F1B11), center = Offset(cx + 32f, cy - 10f), radius = 12f)
        drawRect(Color(0xFF0F1B11), topLeft = Offset(cx + 24f, cy + 2f), size = Size(16f, 50f))

        // Floating hearts
        for (i in 0..5) {
            val hx = cx + Math.sin(i * 45.0).toFloat() * 140f
            val hy = (cy - 100f - (floatAnim * 250f) + i * 30f) % height
            if (hy < cy) {
                drawCircle(Color(0xFFFF1744), center = Offset(hx, hy), radius = 8f)
            }
        }
    }
}

@Composable
fun CreditsIllustration(
    lang: String,
    onReset: () -> Unit,
    onContinue: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF05070B))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2E7D32))
                    .border(3.dp, Color(0xFFFFC107), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🏆", fontSize = 48.sp)
            }

            Text(
                text = if (lang == "TR") "TEBRİKLER! OYUNU BİTİRDİNİZ!" else "CONGRATULATIONS! YOU BEAT THE GAME!",
                color = Color(0xFFFFC107),
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )

            Text(
                text = "PROTRADER698 YAPIM SUNDU\n\nOYUNU EĞER BEĞENDİYSENİZ KRİPTO PARA İLE KÜÇÜK BAĞIŞLARDA BULUNABİLİRSİNİZ BÖYLECE YENİ OYUNLAR GELECEKTİR.",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onContinue,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = if (lang == "TR") "LÜKS HAYATA SANDBOX DEVAM ET 🏰" else "CONTINUE PLAYING LUXURY SANDBOX 🏰",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                OutlinedButton(
                    onClick = onReset,
                    border = BorderStroke(1.dp, Color(0xFFFF1744)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF1744)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = if (lang == "TR") "OYUNU SIFIRLA VE TEKRAR OYNA 🔄" else "RESET GAME & PLAY AGAIN 🔄",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
