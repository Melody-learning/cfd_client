package com.astralw.feature.chart

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import java.net.URLDecoder
import javax.inject.Inject

/**
 * K 线图 ViewModel
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
        marketRepository.observeQuotes().sample(500),
        _timeframe,
    ) { candles, quotes, timeframe ->
        val currentQuote = quotes.find { it.symbol == symbol }

        // 蜡烛数据完全由后端轮询提供（每 3s），不做前端 tick 注入
        // 实时跳动感通过 currentBid 价格线在 Canvas 上实现（每 500ms 刷新）
        ChartUiState.Success(
            symbol = symbol,
            displayName = displayName,
            candles = candles,
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
