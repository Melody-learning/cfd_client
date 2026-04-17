package com.astralw.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ─── WebSocket Message DTO ───

/**
 * WebSocket 发送消息（subscribe / unsubscribe）
 */
@Serializable
data class WsActionMessage(
    val action: String,
    val symbols: List<String>,
)

/**
 * WebSocket 接收消息
 *
 * 服务端推送格式: { "type": "quote", "data": { ... } }
 */
@Serializable
data class WsIncomingMessage(
    val type: String = "",
    val data: WsQuoteData? = null,
)

/**
 * WebSocket 行情数据
 *
 * 铁律: 全站禁 Double，价格用 String 传输
 */
@Serializable
data class WsQuoteData(
    val symbol: String = "",
    val bid: String = "0",
    val ask: String = "0",
    val last: String = "0",
    val high: String = "0",
    val low: String = "0",
    val volume: String = "0",
    @SerialName("price_open") val priceOpen: String = "0",
    @SerialName("price_close") val priceClose: String = "0",
    @SerialName("price_change") val priceChange: String = "0",
    val spread: String = "0",
    val digits: Int = 5,
    val datetime: Long = 0,
)
