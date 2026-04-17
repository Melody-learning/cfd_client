package com.astralw.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 挂单 DTO — 对应 GET /api/v1/orders 响应中的单条记录
 */
@Serializable
data class PendingOrderDto(
    /** MT5 订单 ticket */
    val ticket: Long,
    /** 品种 */
    val symbol: String,
    /** 订单类型: BUY_LIMIT / SELL_LIMIT / BUY_STOP / SELL_STOP */
    val type: String,
    /** MT5 内部手数 (100 = 0.01 手) */
    val volume: String,
    /** 挂单触发价格 */
    @SerialName("price_open") val priceOpen: String,
    /** 止损 */
    @SerialName("stop_loss") val stopLoss: String = "0",
    /** 止盈 */
    @SerialName("take_profit") val takeProfit: String = "0",
    /** 当前市场价 */
    @SerialName("price_current") val priceCurrent: String = "",
    /** 挂单创建时间 (Unix 秒) */
    @SerialName("time_setup") val timeSetup: Long = 0,
    /** 到期时间 (Unix 秒, 0=不限期) */
    val expiration: Long = 0,
    /** 备注 */
    val comment: String = "",
)

/**
 * 挂单列表响应 DTO
 */
@Serializable
data class OrdersResponseDto(
    val orders: List<PendingOrderDto> = emptyList(),
)
