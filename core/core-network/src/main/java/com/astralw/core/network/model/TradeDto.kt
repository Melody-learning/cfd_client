package com.astralw.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ─── Trade DTO ───

@Serializable
data class TradeOpenRequestDto(
    val symbol: String,
    val direction: String,
    val lots: String,
    @SerialName("stop_loss") val stopLoss: String? = null,
    @SerialName("take_profit") val takeProfit: String? = null,
)

@Serializable
data class TradeCloseRequestDto(
    val position: Long,
    val lots: String = "",
)

@Serializable
data class TradeModifyRequestDto(
    val position: Long,
    @SerialName("stop_loss") val stopLoss: String = "0",
    @SerialName("take_profit") val takeProfit: String = "0",
)

@Serializable
data class TradeResultDto(
    val order: Long = 0,
    val deal: Long = 0,
    val price: String = "",
    val volume: String = "0",
    val retcode: String = "",
    val message: String = "",
)

// ─── Margin Check / Profit Calc DTO ───

@Serializable
data class MarginCheckResponseDto(
    val margin: String = "0",
    @SerialName("free_margin") val freeMargin: String = "0",
    @SerialName("margin_level") val marginLevel: String = "0",
)

@Serializable
data class ProfitCalcResponseDto(
    val profit: String = "0",
    @SerialName("profit_rate") val profitRate: String = "0",
)

