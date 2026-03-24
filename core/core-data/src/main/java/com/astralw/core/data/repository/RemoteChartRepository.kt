package com.astralw.core.data.repository

import android.util.Log
import com.astralw.core.data.model.Candle
import com.astralw.core.network.api.AstralWApiService
import com.astralw.core.network.model.CandleDto
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

// RemoteChartRepository: fetches candle history from Gateway
// Refresh interval adapts to timeframe (M1 → 5s, H1 → 30s, D1 → 60s)
@Singleton
class RemoteChartRepository @Inject constructor(
    private val api: AstralWApiService,
) : ChartRepository {

    override suspend fun getCandles(symbol: String, timeframe: String, count: Int): List<Candle> {
        return try {
            Log.d(TAG, "getCandles: $symbol $timeframe count=$count")
            val resp = api.getCandles(symbol, timeframe, count = count)
            Log.d(TAG, "getCandles: got ${resp.candles.size} candles")
            resp.candles.map { it.toCandle() }
        } catch (e: Exception) {
            Log.e(TAG, "getCandles failed", e)
            emptyList()
        }
    }

    override fun observeCandles(symbol: String, timeframe: String): Flow<List<Candle>> = flow {
        val interval = refreshInterval(timeframe)
        val count = candleCount(timeframe)
        while (true) {
            val candles = getCandles(symbol, timeframe, count).takeLast(count)
            emit(candles)
            delay(interval)
        }
    }

    private fun CandleDto.toCandle() = Candle(
        timestamp = timestamp * 1000, // seconds -> millis
        open = open,
        high = high,
        low = low,
        close = close,
        volume = volume,
    )

    companion object {
        private const val TAG = "RemoteChartRepo"
        private const val CANDLE_COUNT = 30

        /**
         * Refresh interval based on timeframe.
         * M1 updates frequently (current candle changes every tick),
         * higher timeframes update less often.
         */
        private fun refreshInterval(tf: String): Long = when (tf.uppercase()) {
            "M1" -> 3_000L        // 3 seconds — current candle changes frequently
            "M5" -> 5_000L        // 5 seconds
            "M15" -> 10_000L      // 10 seconds
            "M30" -> 15_000L      // 15 seconds
            "H1" -> 10_000L       // 10 seconds — still needs timely updates
            "H4" -> 30_000L       // 30 seconds
            "D1" -> 60_000L       // 1 minute
            else -> 10_000L
        }

        /** 不同 timeframe 显示不同数量的蜡烛 */
        private fun candleCount(tf: String): Int = when (tf.uppercase()) {
            "M1" -> 25     // 25 分钟
            "M5" -> 25     // ~2 小时
            "M15" -> 25    // ~6 小时
            "M30" -> 30    // ~15 小时
            "H1" -> 48     // 2 天
            "H4" -> 42     // 7 天
            "D1" -> 30     // 1 个月
            else -> 30
        }
    }
}
