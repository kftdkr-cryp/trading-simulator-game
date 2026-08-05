package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MarketCandle
import kotlin.math.max

data class LineDrawing(
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val type: String = "LINE"
)

@Composable
fun CandleChart(
    candles: List<MarketCandle>,
    sma10: List<Float?>,
    sma20: List<Float?>,
    rsi: List<Float?>,
    showSma10: Boolean,
    showSma20: Boolean,
    showRsi: Boolean,
    drawings: List<LineDrawing>,
    drawingModeEnabled: Boolean,
    selectedDrawingTool: String, // "LINE", "HORIZONTAL", "RAY"
    onDrawLineAdded: (LineDrawing) -> Unit,
    modifier: Modifier = Modifier
) {
    if (candles.isEmpty()) {
        Box(
            modifier = modifier.background(Color(0xFF121620)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Grafik verisi yükleniyor...",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
        return
    }

    // Determine viewport size (limit to last 35 candles to keep them wide and readable)
    val maxVisibleCandles = 35
    val visibleCandles = if (candles.size > maxVisibleCandles) {
        candles.takeLast(maxVisibleCandles)
    } else {
        candles
    }

    val visibleSma10 = if (sma10.size == candles.size) sma10.takeLast(visibleCandles.size) else List(visibleCandles.size) { null }
    val visibleSma20 = if (sma20.size == candles.size) sma20.takeLast(visibleCandles.size) else List(visibleCandles.size) { null }
    val visibleRsi = if (rsi.size == candles.size) rsi.takeLast(visibleCandles.size) else List(visibleCandles.size) { null }

    // Calculate Min and Max values for scale
    val minPrice = visibleCandles.minOf { it.low }
    val maxPrice = visibleCandles.maxOf { it.high }
    
    // SMA adjusted min/max
    var chartMin = minPrice * 0.99
    var chartMax = maxPrice * 1.01

    // Include SMAs in min/max boundary calculation if shown
    if (showSma10) {
        visibleSma10.filterNotNull().forEach {
            chartMin = minOf(chartMin, it.toDouble())
            chartMax = maxOf(chartMax, it.toDouble())
        }
    }
    if (showSma20) {
        visibleSma20.filterNotNull().forEach {
            chartMin = minOf(chartMin, it.toDouble())
            chartMax = maxOf(chartMax, it.toDouble())
        }
    }

    val maxVolume = visibleCandles.maxOfOrNull { it.volume } ?: 1.0

    // Colors
    val greenBull = Color(0xFF00E676)
    val redBear = Color(0xFFFF1744)
    val darkBg = Color(0xFF0B0E14)
    val gridColor = Color(0xFF1E2638)
    val axisColor = Color(0xFF37474F)
    val sma10Color = Color(0xFFFFC107) // Amber
    val sma20Color = Color(0xFF00B0FF) // Blue
    val rsiLineColor = Color(0xFFE040FB) // Magenta
    val drawingColor = Color(0xFFE040FB) // Magenta drawing line

    // Drag-Drawing State
    var dragStartOffset by remember { mutableStateOf<Offset?>(null) }
    var dragCurrentOffset by remember { mutableStateOf<Offset?>(null) }

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(darkBg)
                .pointerInput(drawingModeEnabled, selectedDrawingTool) {
                    if (drawingModeEnabled) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                dragStartOffset = offset
                                dragCurrentOffset = offset
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                dragCurrentOffset = change.position
                            },
                            onDragEnd = {
                                val start = dragStartOffset
                                val end = dragCurrentOffset
                                if (start != null && end != null) {
                                    val finalEnd = when (selectedDrawingTool) {
                                        "HORIZONTAL" -> Offset(end.x, start.y)
                                        else -> end
                                    }
                                    onDrawLineAdded(
                                        LineDrawing(
                                            startX = start.x,
                                            startY = start.y,
                                            endX = finalEnd.x,
                                            endY = finalEnd.y,
                                            type = selectedDrawingTool
                                        )
                                    )
                                }
                                dragStartOffset = null
                                dragCurrentOffset = null
                            },
                            onDragCancel = {
                                dragStartOffset = null
                                dragCurrentOffset = null
                            }
                        )
                    }
                }
        ) {
            val width = size.width
            val height = size.height

            val usableWidth = width * 0.88f
            val labelWidth = width * 0.12f
            val candleCount = visibleCandles.size
            val candleWidth = usableWidth / candleCount
            val priceRange = chartMax - chartMin

            // Helper lambda to get Y pixel position for a price
            val getPriceY = { price: Double ->
                (height * (1.0 - (price - chartMin) / priceRange)).toFloat()
            }

            // Draw horizontal Grid lines
            val gridLinesCount = 5
            for (i in 0 until gridLinesCount) {
                val ratio = i.toFloat() / (gridLinesCount - 1)
                val y = height * ratio
                val gridPrice = chartMax - (ratio * priceRange)

                // Dotted horizontal grid line
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(usableWidth, y),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )

                // Draw price labels on the right axis
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 28f
                        isAntiAlias = true
                    }
                    drawText(
                        String.format("$%.2f", gridPrice),
                        usableWidth + 10f,
                        y + 10f,
                        paint
                    )
                }
            }

            // Draw Y-axis border
            drawLine(
                color = axisColor,
                start = Offset(usableWidth, 0f),
                end = Offset(usableWidth, height),
                strokeWidth = 2f
            )

            // Draw Candles & Volume Bars
            for (i in visibleCandles.indices) {
                val candle = visibleCandles[i]
                val xLeft = i * candleWidth
                val xCenter = xLeft + candleWidth / 2f
                val spacing = candleWidth * 0.15f // 15% padding between candles

                val isBullish = candle.close >= candle.open
                val color = if (isBullish) greenBull else redBear

                val openY = getPriceY(candle.open)
                val closeY = getPriceY(candle.close)
                val highY = getPriceY(candle.high)
                val lowY = getPriceY(candle.low)

                // 1. Draw High-Low wick
                drawLine(
                    color = color,
                    start = Offset(xCenter, highY),
                    end = Offset(xCenter, lowY),
                    strokeWidth = 2.dp.toPx()
                )

                // 2. Draw Body
                val bodyTop = minOf(openY, closeY)
                val bodyBottom = maxOf(openY, closeY)
                val bodyHeight = maxOf(bodyBottom - bodyTop, 1f)

                drawRect(
                    color = color,
                    topLeft = Offset(xLeft + spacing, bodyTop),
                    size = Size(candleWidth - (spacing * 2f), bodyHeight)
                )

                // 3. Draw Volume Bar at the bottom
                val volumeHeight = (height * 0.18f * (candle.volume / maxVolume)).toFloat()
                drawRect(
                    color = color.copy(alpha = 0.22f),
                    topLeft = Offset(xLeft + spacing, height - volumeHeight),
                    size = Size(candleWidth - (spacing * 2f), volumeHeight)
                )
            }

            // Draw SMA 10 (Gold)
            if (showSma10) {
                for (i in 1 until visibleSma10.size) {
                    val prevVal = visibleSma10[i - 1]
                    val currVal = visibleSma10[i]
                    if (prevVal != null && currVal != null) {
                        drawLine(
                            color = sma10Color,
                            start = Offset((i - 1) * candleWidth + candleWidth / 2f, getPriceY(prevVal.toDouble())),
                            end = Offset(i * candleWidth + candleWidth / 2f, getPriceY(currVal.toDouble())),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                }
            }

            // Draw SMA 20 (Blue)
            if (showSma20) {
                for (i in 1 until visibleSma20.size) {
                    val prevVal = visibleSma20[i - 1]
                    val currVal = visibleSma20[i]
                    if (prevVal != null && currVal != null) {
                        drawLine(
                            color = sma20Color,
                            start = Offset((i - 1) * candleWidth + candleWidth / 2f, getPriceY(prevVal.toDouble())),
                            end = Offset(i * candleWidth + candleWidth / 2f, getPriceY(currVal.toDouble())),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                }
            }

            // Render persistent user drawing lines!
            drawings.forEach { drawing ->
                val drawY = drawing.startY
                when (drawing.type) {
                    "HORIZONTAL" -> {
                        // Horizontal line across the entire usable width of the chart
                        drawLine(
                            color = Color.Yellow,
                            start = Offset(0f, drawY),
                            end = Offset(usableWidth, drawY),
                            strokeWidth = 3f
                        )
                    }
                    "RAY" -> {
                        // Ray starts at startX and extends to the right edge
                        drawLine(
                            color = Color.Yellow,
                            start = Offset(drawing.startX, drawY),
                            end = Offset(usableWidth, drawing.endY),
                            strokeWidth = 3f
                        )
                    }
                    else -> {
                        // Normal Line
                        drawLine(
                            color = Color.Yellow,
                            start = Offset(drawing.startX, drawing.startY),
                            end = Offset(drawing.endX, drawing.endY),
                            strokeWidth = 3f
                        )
                    }
                }
            }

            // Render current drawing line preview
            val currentStart = dragStartOffset
            val currentEnd = dragCurrentOffset
            if (currentStart != null && currentEnd != null) {
                val previewEnd = when (selectedDrawingTool) {
                    "HORIZONTAL" -> Offset(currentEnd.x, currentStart.y)
                    else -> currentEnd
                }
                
                when (selectedDrawingTool) {
                    "HORIZONTAL" -> {
                        drawLine(
                            color = Color.White,
                            start = Offset(0f, currentStart.y),
                            end = Offset(usableWidth, currentStart.y),
                            strokeWidth = 2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
                        )
                    }
                    "RAY" -> {
                        drawLine(
                            color = Color.White,
                            start = Offset(currentStart.x, currentStart.y),
                            end = Offset(usableWidth, previewEnd.y),
                            strokeWidth = 2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
                        )
                    }
                    else -> {
                        drawLine(
                            color = Color.White,
                            start = currentStart,
                            end = previewEnd,
                            strokeWidth = 2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
                        )
                    }
                }
            }
        }

        // Dedicated RSI Chart Composable below
        if (showRsi) {
            Spacer(modifier = Modifier.height(4.dp))
            Canvas(
                modifier = Modifier
                    .height(65.dp)
                    .fillMaxWidth()
                    .background(darkBg)
            ) {
                val width = size.width
                val height = size.height

                val usableWidth = width * 0.88f
                val candleCount = visibleCandles.size
                val candleWidth = usableWidth / candleCount

                val rsiY = { rsiVal: Float ->
                    (height * (1f - rsiVal / 100f))
                }

                // Shaded oversold/overbought zone (30 to 70)
                val top30 = rsiY(30f)
                val top70 = rsiY(70f)
                drawRect(
                    color = Color(0xFF4A148C).copy(alpha = 0.15f),
                    topLeft = Offset(0f, top70),
                    size = Size(usableWidth, top30 - top70)
                )

                // Dash lines for 30 and 70 thresholds
                drawLine(
                    color = Color.Gray.copy(alpha = 0.5f),
                    start = Offset(0f, top30),
                    end = Offset(usableWidth, top30),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
                )
                drawLine(
                    color = Color.Gray.copy(alpha = 0.5f),
                    start = Offset(0f, top70),
                    end = Offset(usableWidth, top70),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
                )

                // Dotted horizontal line at 50 (neutral)
                drawLine(
                    color = Color.DarkGray.copy(alpha = 0.4f),
                    start = Offset(0f, rsiY(50f)),
                    end = Offset(usableWidth, rsiY(50f)),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )

                // Y-axis border
                drawLine(
                    color = axisColor,
                    start = Offset(usableWidth, 0f),
                    end = Offset(usableWidth, height),
                    strokeWidth = 2f
                )

                // Draw RSI labels
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 24f
                        isAntiAlias = true
                    }
                    drawText("70", usableWidth + 10f, top70 + 8f, paint)
                    drawText("30", usableWidth + 10f, top30 + 8f, paint)
                }

                // Draw the actual RSI line
                for (i in 1 until visibleRsi.size) {
                    val prevVal = visibleRsi[i - 1]
                    val currVal = visibleRsi[i]
                    if (prevVal != null && currVal != null) {
                        drawLine(
                            color = rsiLineColor,
                            start = Offset((i - 1) * candleWidth + candleWidth / 2f, rsiY(prevVal)),
                            end = Offset(i * candleWidth + candleWidth / 2f, rsiY(currVal)),
                            strokeWidth = 1.5.dp.toPx()
                        )
                    }
                }
            }
        }
    }
}
