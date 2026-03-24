package com.astralw.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ─── Market DTO ───

@Serializable
data class SymbolEntryDto(
    val symbol: String,
)

@Serializable
data class SymbolsResponseDto(
    val symbols: List<SymbolEntryDto> = emptyList(),
)

@Serializable
data class QuoteDto(
    val symbol: String,
    val bid: String = "0",
    val ask: String = "0",
    val last: String = "0",
    val volume: String = "0",
    val datetime: Long = 0,
)

@Serializable
data class QuotesResponseDto(
    val quotes: List<QuoteDto> = emptyList(),
)

@Serializable
data class TickStatResponseDto(
    val stats: List<TickStatDto> = emptyList(),
)

@Serializable
data class TickStatDto(
    val symbol: String,
    val bid: String = "0",
    val ask: String = "0",
    val last: String = "0",
    val high: String = "0",
    val low: String = "0",
    val volume: String = "0",
    @SerialName("vol_buy") val volBuy: String = "0",
    @SerialName("vol_sell") val volSell: String = "0",
    @SerialName("trade_buy") val tradeBuy: String = "0",
    @SerialName("trade_sell") val tradeSell: String = "0",
    @SerialName("price_open") val priceOpen: String = "0",
    @SerialName("price_close") val priceClose: String = "0",
    @SerialName("price_change") val priceChange: String = "0",
    val spread: String = "0",
    val digits: Int = 5,
    val datetime: Long = 0,
)
