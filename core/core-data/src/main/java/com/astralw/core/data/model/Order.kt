package com.astralw.core.data.model

/**
 * 交易订单
 *
 * 铁律: 全站禁止 Double，金额/价格用 String
 */
data class Order(
    val orderId: String,
    val symbol: String,
    val displayName: String,
    val direction: OrderDirection,
    val lots: String,
    val openPrice: String,
    val currentPrice: String,
    val stopLoss: String?,
    val takeProfit: String?,
    val floatingPnL: String,
    val margin: String,
    val openTime: Long,
    val status: OrderStatus,
)

enum class OrderDirection { BUY, SELL }

enum class OrderStatus {
    /** 持仓中 */
    OPEN,
    /** 挂单 */
    PENDING,
    /** 已平仓 */
    CLOSED,
}
