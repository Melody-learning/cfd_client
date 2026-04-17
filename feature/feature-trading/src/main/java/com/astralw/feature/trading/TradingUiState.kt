package com.astralw.feature.trading

import com.astralw.core.data.model.OrderType

/**
 * 到期时间类型
 */
enum class ExpirationType(val label: String) {
    /** 不限期 (Good Till Cancelled) */
    GTC("不限期"),
    /** 今天结束 */
    TODAY("今天"),
    /** 本周结束 */
    THIS_WEEK("本周"),
}

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
    // ─── 挂单相关 ───
    /** 当前选择的订单类型 */
    val orderType: OrderType = OrderType.MARKET,
    /** 挂单触发价格 */
    val pendingPrice: String = "",
    /** 到期时间类型 */
    val expirationType: ExpirationType = ExpirationType.GTC,
) {
    /** 是否为挂单模式 */
    val isPendingOrder: Boolean get() = orderType != OrderType.MARKET

    /** 订单类型中文标签 */
    val orderTypeLabel: String get() = orderType.label
}
