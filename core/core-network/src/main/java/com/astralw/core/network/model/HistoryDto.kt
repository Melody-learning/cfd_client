package com.astralw.core.network.model

import kotlinx.serialization.Serializable

// ─── History DTO ───

@Serializable
data class DealDto(
    val deal: Long = 0,
    val order: Long = 0,
    val symbol: String = "",
    val type: Int = 0,
    val direction: String = "",
    val volume: String = "0",
    val price: String = "0",
    val profit: String = "0",
    val commission: String = "0",
    val swap: String = "0",
    val time: Long = 0,
    val comment: String = "",
)

@Serializable
data class DealsResponseDto(
    val deals: List<DealDto> = emptyList(),
)
