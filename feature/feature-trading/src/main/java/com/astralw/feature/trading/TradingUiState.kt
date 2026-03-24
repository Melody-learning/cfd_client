package com.astralw.feature.trading

import com.astralw.core.data.model.OrderDirection

/**
 * 下单页 UI 状态
 */
data class TradingUiState(
    val symbol: String = "",
    val displayName: String = "",
    val currentBid: String = "",
    val currentAsk: String = "",
    val isUp: Boolean = true,
    val lots: String = "0.01",
    val stopLoss: String = "",
    val takeProfit: String = "",
    val estimatedMargin: String = "0.00",
    val balance: String = "--",
    val freeMargin: String = "--",
    val isSubmitting: Boolean = false,
    val orderPlaced: Boolean = false,
    val errorMessage: String? = null,
)
