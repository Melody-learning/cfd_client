package com.astralw.feature.chart

import com.astralw.core.data.model.Candle

/**
 * K 线图 UI 状态
 */
sealed interface ChartUiState {
    data object Loading : ChartUiState
    data class Success(
        val symbol: String,
        val displayName: String,
        val candles: List<Candle>,
        val selectedTimeframe: String = "H1",
        val currentBid: String = "",
        val currentAsk: String = "",
        val change: String = "",
        val changePercent: String = "",
        val isUp: Boolean = true,
    ) : ChartUiState
    data class Error(val message: String) : ChartUiState
}

/** 可用时间周期 */
val TIMEFRAMES = listOf("M1", "M5", "M15", "H1", "H4", "D1")
