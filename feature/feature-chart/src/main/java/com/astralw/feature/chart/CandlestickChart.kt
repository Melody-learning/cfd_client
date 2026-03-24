package com.astralw.feature.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import com.astralw.core.data.model.Candle
import com.astralw.core.ui.theme.DesignTokens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Compose Canvas K 线蜡烛图
 *
 * 竞品风格：黑底，绿涨/红跌，右侧价格轴，底部时间轴，
 * 蜡烛宽大有间距，当前价格高亮标记。
 */
@Composable
fun CandlestickChart(
    candles: List<Candle>,
    modifier: Modifier = Modifier,
    currentBid: String = "",
) {
    if (candles.isEmpty()) return

    val bullishColor = DesignTokens.SemanticColors.PriceUp
    val bearishColor = DesignTokens.SemanticColors.PriceDown
    val gridColor = DesignTokens.SemanticColors.BorderSubtle
    val textColor = DesignTokens.SemanticColors.TextTertiary
    val currentBidVal = currentBid.toDoubleOrNull()

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(DesignTokens.SemanticColors.Background),
    ) {
        val priceAxisWidth = 70f  // 右侧价格轴宽度
        val timeAxisHeight = 28f  // 底部时间轴高度
        val chartWidth = size.width - priceAxisWidth
        val chartHeight = size.height - timeAxisHeight
        val candleCount = candles.size
        if (candleCount == 0 || chartWidth <= 0 || chartHeight <= 0) return@Canvas

        // 蜡烛宽度：占 60%，间距 40%
        val totalCandleWidth = chartWidth / candleCount
        val candleWidth = totalCandleWidth * 0.6f
        val gapWidth = totalCandleWidth * 0.4f

        // 计算价格范围（包含 currentBid 确保实时价格线始终可见）
        val prices = candles.flatMap { candle ->
            listOf(
                candle.high.toDoubleOrNull() ?: 0.0,
                candle.low.toDoubleOrNull() ?: 0.0,
            )
        }.let { list ->
            if (currentBidVal != null) list + currentBidVal else list
        }
        val maxPrice = prices.maxOrNull() ?: 1.0
        val minPrice = prices.minOrNull() ?: 0.0
        val priceRange = maxPrice - minPrice
        if (priceRange <= 0) return@Canvas

        val padding = priceRange * 0.08
        val adjustedMax = maxPrice + padding
        val adjustedMin = minPrice - padding
        val adjustedRange = adjustedMax - adjustedMin

        // 1. 绘制横向网格线 + 右侧价格标签
        drawPriceAxis(chartWidth, chartHeight, priceAxisWidth, adjustedMin, adjustedMax, adjustedRange, gridColor, textColor)

        // 2. 绘制底部时间轴标签
        drawTimeAxis(candles, chartWidth, chartHeight, timeAxisHeight, totalCandleWidth, gapWidth, candleWidth, textColor)

        // 3. 绘制蜡烛
        candles.forEachIndexed { index, candle ->
            val open = candle.open.toDoubleOrNull() ?: return@forEachIndexed
            val close = candle.close.toDoubleOrNull() ?: return@forEachIndexed
            val high = candle.high.toDoubleOrNull() ?: return@forEachIndexed
            val low = candle.low.toDoubleOrNull() ?: return@forEachIndexed

            val x = index * totalCandleWidth + gapWidth / 2
            val centerX = x + candleWidth / 2

            val openY = ((adjustedMax - open) / adjustedRange * chartHeight).toFloat()
            val closeY = ((adjustedMax - close) / adjustedRange * chartHeight).toFloat()
            val highY = ((adjustedMax - high) / adjustedRange * chartHeight).toFloat()
            val lowY = ((adjustedMax - low) / adjustedRange * chartHeight).toFloat()

            val isBullish = close >= open
            val color = if (isBullish) bullishColor else bearishColor
            val bodyTop = minOf(openY, closeY)
            val bodyBottom = maxOf(openY, closeY)
            val bodyHeight = maxOf(bodyBottom - bodyTop, 2f)

            // 上影线
            drawLine(
                color = color,
                start = Offset(centerX, highY),
                end = Offset(centerX, bodyTop),
                strokeWidth = 1.5f,
            )

            // 下影线
            drawLine(
                color = color,
                start = Offset(centerX, bodyBottom),
                end = Offset(centerX, lowY),
                strokeWidth = 1.5f,
            )

            // 实体
            drawRect(
                color = color,
                topLeft = Offset(x, bodyTop),
                size = Size(candleWidth, bodyHeight),
            )
        }

        // 4. 当前实时 bid 价格线（每 500ms 随 tick 跳动）
        val bidForLine = currentBidVal ?: candles.last().close.toDoubleOrNull()
        if (bidForLine != null) {
            val bidY = ((adjustedMax - bidForLine) / adjustedRange * chartHeight).toFloat()
            val lastCandle = candles.last()
            val isUp = lastCandle.open.toDoubleOrNull()?.let { bidForLine >= it } ?: true
            val lineColor = if (isUp) bullishColor else bearishColor

            drawLine(
                color = lineColor.copy(alpha = 0.6f),
                start = Offset(0f, bidY),
                end = Offset(chartWidth, bidY),
                strokeWidth = 0.8f,
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                    floatArrayOf(8f, 4f),
                ),
            )
            // 右侧价格标签高亮
            drawRect(
                color = lineColor,
                topLeft = Offset(chartWidth + 2f, bidY - 10f),
                size = Size(priceAxisWidth - 4f, 20f),
            )
            drawContext.canvas.nativeCanvas.drawText(
                formatPrice(bidForLine, priceRange),
                chartWidth + 6f,
                bidY + 4f,
                android.graphics.Paint().apply {
                    this.color = android.graphics.Color.BLACK
                    textSize = 22f
                    isAntiAlias = true
                    typeface = android.graphics.Typeface.create(
                        android.graphics.Typeface.DEFAULT,
                        android.graphics.Typeface.BOLD,
                    )
                },
            )
        }
    }
}

private fun DrawScope.drawPriceAxis(
    chartWidth: Float,
    chartHeight: Float,
    priceAxisWidth: Float,
    minPrice: Double,
    maxPrice: Double,
    priceRange: Double,
    gridColor: Color,
    textColor: Color,
) {
    val gridCount = 5
    val paint = android.graphics.Paint().apply {
        color = android.graphics.Color.argb(120, 160, 160, 160)
        textSize = 20f
        isAntiAlias = true
    }
    for (i in 0..gridCount) {
        val y = chartHeight * i / gridCount
        // 网格线
        drawLine(
            color = gridColor,
            start = Offset(0f, y),
            end = Offset(chartWidth, y),
            strokeWidth = 0.5f,
        )
        // 价格标签
        val price = maxPrice - (priceRange * i / gridCount)
        drawContext.canvas.nativeCanvas.drawText(
            formatPrice(price, priceRange),
            chartWidth + 6f,
            y + 6f,
            paint,
        )
    }
}

private fun DrawScope.drawTimeAxis(
    candles: List<Candle>,
    chartWidth: Float,
    chartHeight: Float,
    timeAxisHeight: Float,
    totalCandleWidth: Float,
    gapWidth: Float,
    candleWidth: Float,
    textColor: Color,
) {
    val paint = android.graphics.Paint().apply {
        color = android.graphics.Color.argb(120, 160, 160, 160)
        textSize = 18f
        isAntiAlias = true
        textAlign = android.graphics.Paint.Align.CENTER
    }
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault()).apply {
        timeZone = TimeZone.getDefault()
    }

    // 每隔 5-6 根蜡烛显示一个时间标签
    val labelInterval = maxOf(candles.size / 5, 1)
    candles.forEachIndexed { index, candle ->
        if (index % labelInterval == 0) {
            val x = index * totalCandleWidth + gapWidth / 2 + candleWidth / 2
            val timeStr = sdf.format(Date(candle.timestamp))
            drawContext.canvas.nativeCanvas.drawText(
                timeStr,
                x,
                chartHeight + timeAxisHeight - 4f,
                paint,
            )
        }
    }
}

/** 根据价格范围自动选择小数位数 */
private fun formatPrice(price: Double, priceRange: Double): String {
    val decimals = when {
        priceRange < 0.001 -> 5
        priceRange < 0.01 -> 5
        priceRange < 0.1 -> 4
        priceRange < 1.0 -> 3
        priceRange < 10.0 -> 2
        else -> 1
    }
    return String.format(Locale.US, "%.${decimals}f", price)
}
