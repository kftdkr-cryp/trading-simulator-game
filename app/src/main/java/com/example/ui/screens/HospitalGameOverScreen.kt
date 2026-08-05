package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.Localizer
import kotlinx.coroutines.delay

@Composable
fun HospitalGameOverScreen(
    lang: String,
    onReset: () -> Unit
) {
    var animationPhase by remember { mutableIntStateOf(0) } // 0 = Ill/Hospitalized, 1 = Flatline/Died, 2 = Play Again Button Shown

    LaunchedEffect(Unit) {
        delay(4000)
        animationPhase = 1
        delay(3000)
        animationPhase = 2
    }

    // ECG line pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "ecg")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090A0F)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(30.dp)
        ) {
            // Screen Title Banner
            Text(
                text = "HOSPITAL EMERGENCY DEPT",
                color = Color.Red.copy(alpha = 0.8f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier
                    .border(1.dp, Color.Red.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )

            // ECG Monitor Visualization Box
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF050B0F)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .border(2.dp, Color(0xFF1E2D3D), RoundedCornerShape(12.dp))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // Draw grid background
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val gridSpacing = 20.dp.toPx()
                        val cols = (size.width / gridSpacing).toInt()
                        val rows = (size.height / gridSpacing).toInt()

                        for (i in 0..cols) {
                            drawLine(
                                color = Color(0xFF102A30).copy(alpha = 0.3f),
                                start = Offset(i * gridSpacing, 0f),
                                end = Offset(i * gridSpacing, size.height),
                                strokeWidth = 1f
                            )
                        }
                        for (i in 0..rows) {
                            drawLine(
                                color = Color(0xFF102A30).copy(alpha = 0.3f),
                                start = Offset(0f, i * gridSpacing),
                                end = Offset(size.width, i * gridSpacing),
                                strokeWidth = 1f
                            )
                        }
                    }

                    // Draw dynamic ECG line
                    Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)) {
                        val path = Path()
                        val midY = size.height / 2f
                        val width = size.width

                        path.moveTo(0f, midY)

                        if (animationPhase == 0) {
                            // Phase 0: Weak heart pulse
                            for (x in 0..width.toInt() step 5) {
                                val fx = x.toFloat()
                                val relativeX = (fx - waveOffset) % 150f
                                val y = if (relativeX in 40f..50f) {
                                    midY - 60f // P-wave
                                } else if (relativeX in 60f..70f) {
                                    midY + 30f // Q-wave
                                } else if (relativeX in 70f..85f) {
                                    midY - 120f // R-wave peak
                                } else if (relativeX in 85f..95f) {
                                    midY + 40f // S-wave
                                } else if (relativeX in 110f..130f) {
                                    midY - 20f // T-wave
                                } else {
                                    midY
                                }
                                path.lineTo(fx, y)
                            }
                        } else {
                            // Phase 1 & 2: DEAD / FLATLINE
                            for (x in 0..width.toInt() step 10) {
                                val fx = x.toFloat()
                                // Subtle tremor for flatline realism
                                val noise = if (animationPhase == 1) (Math.sin(fx * 0.1 + waveOffset * 0.05) * 1.5).toFloat() else 0f
                                path.lineTo(fx, midY + noise)
                            }
                        }

                        drawPath(
                            path = path,
                            color = if (animationPhase == 0) Color(0xFF00FF66) else Color(0xFFFF1744),
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }

                    // Heart icon glowing or flat
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (animationPhase == 0) Color(0xFF1B5E20).copy(alpha = 0.5f)
                                else Color(0xFFB71C1C).copy(alpha = 0.5f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (animationPhase == 0) "♥ 42" else "☠ 0",
                            color = if (animationPhase == 0) Color(0xFF00FF66) else Color(0xFFFF1744),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Text sequence 1: "YOUR DEBTS LEFT YOU HUNGRY AND YOU FELL ILL..."
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + expandVertically()
            ) {
                Text(
                    text = Localizer.translate("hospital_illness_msg", lang),
                    color = Color.LightGray,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Text sequence 2: "YOU LOST YOUR LIFE" (Fades in after flatline phase starts)
            AnimatedVisibility(
                visible = animationPhase >= 1,
                enter = fadeIn(animationSpec = tween(1500)) + slideInVertically()
            ) {
                Text(
                    text = Localizer.translate("hospital_died_msg", lang),
                    color = Color(0xFFFF1744),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Button: "PLAY AGAIN" / "TEKRAR OYNA" (Fades in at the very end)
            AnimatedVisibility(
                visible = animationPhase >= 2,
                enter = fadeIn(animationSpec = tween(1000)) + scaleIn()
            ) {
                Button(
                    onClick = { onReset() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Restart",
                            tint = Color.White
                        )
                        Text(
                            text = Localizer.translate("play_again", lang),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}
