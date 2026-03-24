package com.astralw.feature.market

import com.astralw.core.data.model.Quote

/**
 * 行情页 UI 状态
 *
 * 铁律: 必须处理 Loading / Success / Error 三态
 */
sealed interface MarketUiState {
    data object Loading : MarketUiState
    data class Success(
        val quotes: List<Quote>,
        val selectedCategory: String = "all",
    ) : MarketUiState
    data class Error(val message: String) : MarketUiState
}

/**
 * 可用分类筛选
 */
val MARKET_CATEGORIES = listOf("all", "forex", "commodities", "indices")
