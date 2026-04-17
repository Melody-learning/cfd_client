package com.astralw.feature.chart

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.astralw.core.data.model.Candle
import com.astralw.core.data.repository.ChartRepository
import com.astralw.core.data.repository.MarketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.stateIn
import java.math.BigDecimal
import java.net.URLDecoder
import javax.inject.Inject

/**
 * K 线图 ViewModel
 *
 * 简洁架构：
 * - 后端蜡烛 = 绝对权威数据源（MT5 已返回包含当前正在形成的蜡烛）
 * - WebSocket tick = 只微调最后一根蜡烛的 close/high/low（视觉实时性）
 * - 不创建新蜡烛，不维护前端状态，不与后端数据打架
 */
@OptIn(FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class ChartViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val chartRepository: ChartRepository,
    private val marketRepository: MarketRepository,
) : ViewModel() {

    private val symbol: String = savedStateHandle["symbol"] ?: "EURUSD"
    private val displayName: String = URLDecoder.decode(
        savedStateHandle["displayName"] ?: "EUR/USD", "UTF-8"
    )

    private val _timeframe = MutableStateFlow("H1")

    val uiState = combine(
        _timeframe.flatMapLatest { tf ->
            chartRepository.observeCandles(symbol, tf)
        },
        // 图表页单品种：比行情列表(500ms)更快的刷新率，提升实时感
        marketRepository.observeQuotes().sample(150),
        _timeframe,
    ) { candles, quotes, timeframe ->
        val currentQuote = quotes.find { it.symbol == symbol }

        // 用实时 tick 微调最后一根蜡烛的 close/high/low
        val liveCandles = injectTickIntoLastCandle(
            candles = candles,
            currentBid = currentQuote?.bid,
            timeframe = timeframe,
        )

        ChartUiState.Success(
            symbol = symbol,
            displayName = displayName,
            candles = liveCandles,
            selectedTimeframe = timeframe,
            currentBid = currentQuote?.bid ?: "",
            currentAsk = currentQuote?.ask ?: "",
            change = currentQuote?.change ?: "",
            changePercent = currentQuote?.changePercent ?: "",
            isUp = currentQuote?.isUp ?: true,
        ) as ChartUiState
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ChartUiState.Loading,
    )

    fun selectTimeframe(timeframe: String) {
        _timeframe.value = timeframe
    }

    /**
     * 用实时 bid 价格微调最后一根蜡烛的 close / high / low
     *
     * 安全检查：
     * - 只在 tick 时间仍属于最后一根蜡烛的时间窗口时才注入
     * - 超出窗口则不注入（等后端下次同步带来新蜡烛）
     * - 防止跨分钟修改旧蜡烛导致"巨型蜡烛"
     */
    private fun injectTickIntoLastCandle(
        candles: List<Candle>,
        currentBid: String?,
        timeframe: String,
    ): List<Candle> {
        if (candles.isEmpty() || currentBid.isNullOrBlank()) return candles

        val bidDec = currentBid.toBigDecimalOrNull() ?: return candles
        if (bidDec.compareTo(BigDecimal.ZERO) == 0) return candles

        val last = candles.last()
        val high = last.high.toBigDecimalOrNull() ?: return candles
        val low = last.low.toBigDecimalOrNull() ?: return candles

        // 时间窗口检查：tick 是否仍在最后一根蜡烛的时间范围内
        val periodMs = timeframePeriodMs(timeframe)
        val lastCandleEnd = last.timestamp + periodMs
        val now = System.currentTimeMillis()
        if (now >= lastCandleEnd) {
            // tick 已超出最后一根蜡烛的窗口 → 不注入，等后端同步
            return candles
        }

        val updatedLast = last.copy(
            close = currentBid,
            high = if (bidDec > high) currentBid else last.high,
            low = if (bidDec < low) currentBid else last.low,
        )

        return candles.dropLast(1) + updatedLast
    }

    companion object {
        /** 每种 timeframe 的毫秒周期 */
        private fun timeframePeriodMs(tf: String): Long = when (tf.uppercase()) {
            "M1" -> 60_000L
            "M5" -> 5 * 60_000L
            "M15" -> 15 * 60_000L
            "M30" -> 30 * 60_000L
            "H1" -> 60 * 60_000L
            "H4" -> 4 * 60 * 60_000L
            "D1" -> 24 * 60 * 60_000L
            else -> 60_000L
        }
    }
}
