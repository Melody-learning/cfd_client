package com.astralw.core.data.model

/**
 * MT5 订单类型
 *
 * 市价单 + 四种挂单类型
 */
enum class OrderType(
    /** API 传输值 */
    val apiValue: String,
    /** 中文标签 */
    val label: String,
) {
    /** 市价单 */
    MARKET("MARKET", "市价执行"),
    /** 限价买入 — 当 Ask 下跌到 ≤ 触发价时买入 */
    BUY_LIMIT("BUY_LIMIT", "限价买入"),
    /** 限价卖出 — 当 Bid 上涨到 ≥ 触发价时卖出 */
    SELL_LIMIT("SELL_LIMIT", "限价卖出"),
    /** 止损买入 — 当 Ask 上涨到 ≥ 触发价时买入 */
    BUY_STOP("BUY_STOP", "止损买入"),
    /** 止损卖出 — 当 Bid 下跌到 ≤ 触发价时卖出 */
    SELL_STOP("SELL_STOP", "止损卖出");

    companion object {
        /** 从 API 值解析 OrderType, 未知则默认 MARKET */
        fun fromApiValue(value: String): OrderType =
            entries.find { it.apiValue.equals(value, ignoreCase = true) } ?: MARKET

        /** 是否为挂单类型 */
        fun OrderType.isPending(): Boolean = this != MARKET

        /** 是否为买入方向 */
        fun OrderType.isBuy(): Boolean = this == MARKET || this == BUY_LIMIT || this == BUY_STOP
    }
}
