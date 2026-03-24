package com.astralw.core.data.repository

import com.astralw.core.data.model.Candle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Mock K 线数据源
 *
 * 生成逼真的随机蜡烛图数据用于前端开发
 */
@Singleton
class MockChartRepository @Inject constructor() : ChartRepository {

    private val candleCache = mutableMapOf<String, MutableStateFlow<List<Candle>>>()

    override suspend fun getCandles(symbol: String, timeframe: String, count: Int): List<Candle> {
        return generateCandles(symbol, timeframe, count)
    }

    override fun observeCandles(symbol: String, timeframe: String): Flow<List<Candle>> {
        val key = "$symbol-$timeframe"
        return candleCache.getOrPut(key) {
            MutableStateFlow(generateCandles(symbol, timeframe, 80))
        }.asStateFlow().onStart {
            // 初始数据已在 getOrPut 时生成
        }
    }

    private fun generateCandles(symbol: String, timeframe: String, count: Int): List<Candle> {
        val basePrice = getBasePrice(symbol)
        val volatility = getVolatility(symbol)
        val digits = getDigits(symbol)
        val intervalMs = getIntervalMs(timeframe)

        val candles = mutableListOf<Candle>()
        var currentPrice = basePrice
        val now = System.currentTimeMillis()

        for (i in count - 1 downTo 0) {
            val timestamp = now - (i.toLong() * intervalMs)
            val open = currentPrice

            // 随机生成价格波动
            val range = volatility * (0.5 + Random.nextDouble())
            val direction = if (Random.nextBoolean()) BigDecimal.ONE else BigDecimal.ONE.negate()
            val moveAmount = BigDecimal(range).multiply(direction)

            val close = open.add(moveAmount).setScale(digits, RoundingMode.HALF_UP)
            val bodyHigh = open.max(close)
            val bodyLow = open.min(close)

            val wickUp = BigDecimal(range * Random.nextDouble(0.2, 0.8))
            val wickDown = BigDecimal(range * Random.nextDouble(0.2, 0.8))

            val high = bodyHigh.add(wickUp).setScale(digits, RoundingMode.HALF_UP)
            val low = bodyLow.subtract(wickDown).setScale(digits, RoundingMode.HALF_UP)

            val volume = BigDecimal(Random.nextInt(100, 5000))

            candles.add(
                Candle(
                    timestamp = timestamp,
                    open = open.toPlainString(),
                    high = high.toPlainString(),
                    low = low.toPlainString(),
                    close = close.toPlainString(),
                    volume = volume.toPlainString(),
                )
            )

            currentPrice = close
        }

        return candles
    }

    private fun getBasePrice(symbol: String): BigDecimal = when (symbol) {
        "EURUSD" -> BigDecimal("1.08550")
        "GBPUSD" -> BigDecimal("1.29340")
        "USDJPY" -> BigDecimal("149.850")
        "AUDUSD" -> BigDecimal("0.63720")
        "USDCAD" -> BigDecimal("1.44250")
        "USDCHF" -> BigDecimal("0.88350")
        "NZDUSD" -> BigDecimal("0.56980")
        "XAUUSD" -> BigDecimal("2985.50")
        "XAGUSD" -> BigDecimal("33.450")
        "US30" -> BigDecimal("41250.5")
        else -> BigDecimal("1.00000")
    }

    private fun getVolatility(symbol: String): Double = when (symbol) {
        "XAUUSD" -> 5.0
        "XAGUSD" -> 0.15
        "US30" -> 50.0
        "USDJPY" -> 0.15
        else -> 0.0015
    }

    private fun getDigits(symbol: String): Int = when (symbol) {
        "USDJPY" -> 3
        "XAUUSD" -> 2
        "XAGUSD" -> 3
        "US30" -> 1
        else -> 5
    }

    private fun getIntervalMs(timeframe: String): Long = when (timeframe) {
        "M1" -> 60_000L
        "M5" -> 300_000L
        "M15" -> 900_000L
        "H1" -> 3_600_000L
        "H4" -> 14_400_000L
        "D1" -> 86_400_000L
        "W1" -> 604_800_000L
        else -> 60_000L
    }
}
