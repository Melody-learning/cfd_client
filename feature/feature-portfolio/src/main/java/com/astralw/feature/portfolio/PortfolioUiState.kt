package com.astralw.feature.portfolio

import com.astralw.core.data.model.Deal
import com.astralw.core.data.model.Order

/**
 * 持仓页 UI 状态
 */
sealed interface PortfolioUiState {
    data object Loading : PortfolioUiState
    data class Success(
        val openPositions: List<Order>,
        val historyDeals: List<Deal>,
        val balance: String,
        val equity: String,
        val freeMargin: String,
        val marginLevel: String,
        val totalPnL: String,
        val totalPnLIsPositive: Boolean,
    ) : PortfolioUiState
    data class Error(val message: String) : PortfolioUiState
}


