package com.astralw.feature.portfolio

import com.astralw.core.data.model.Deal
import com.astralw.core.data.model.Order
import com.astralw.core.data.model.PendingOrder

/**
 * 持仓页 UI 状态
 */
sealed interface PortfolioUiState {
    data object Loading : PortfolioUiState
    data class Success(
        val openPositions: List<Order>,
        val historyDeals: List<Deal>,
        val pendingOrders: List<PendingOrder> = emptyList(),
        val balance: String,
        val equity: String,
        val margin: String,
        val freeMargin: String,
        val marginLevel: String,
        val totalPnL: String,
        val totalPnLIsPositive: Boolean,
        val selectedTab: Int = 0,
    ) : PortfolioUiState
    data class Error(val message: String) : PortfolioUiState
}


