package com.astralw.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ─── Account / Position DTO ───

@Serializable
data class AccountInfoDto(
    val login: Int = 0,
    val group: String = "",
    val balance: String = "0",
    val credit: String = "0",
    val equity: String = "0",
    val margin: String = "0",
    @SerialName("free_margin") val freeMargin: String = "0",
    @SerialName("margin_level") val marginLevel: String = "0",
    val leverage: Int = 100,
    val currency: String = "USD",
    val name: String = "",
)

@Serializable
data class PositionsResponseDto(
    val positions: List<PositionDto> = emptyList(),
)

@Serializable
data class PositionDto(
    val position: Long = 0,
    val symbol: String = "",
    val direction: String = "BUY",
    val lots: String = "0",
    @SerialName("price_open") val priceOpen: String = "0",
    @SerialName("price_current") val priceCurrent: String = "0",
    @SerialName("stop_loss") val stopLoss: String = "0",
    @SerialName("take_profit") val takeProfit: String = "0",
    val profit: String = "0",
    @SerialName("time_create") val timeCreate: Long = 0,
)

@Serializable
data class CandleDto(
    val timestamp: Long,
    val open: String,
    val high: String,
    val low: String,
    val close: String,
    val volume: String = "0",
)

@Serializable
data class CandleResponseDto(
    val symbol: String,
    val timeframe: String,
    val candles: List<CandleDto>,
)
