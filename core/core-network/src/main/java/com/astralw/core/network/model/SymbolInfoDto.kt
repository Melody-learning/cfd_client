package com.astralw.core.network.model

import kotlinx.serialization.Serializable

/**
 * 交易品种信息
 */
@Serializable
data class SymbolInfoDto(
    /** 品种代码 (e.g., "EURUSD") */
    val symbol: String,
    /** 品种描述 (e.g., "Euro vs US Dollar") */
    val description: String,
    /** 品种分类 (e.g., "forex", "indices", "commodities") */
    val category: String,
    /** 合约大小 */
    val contractSize: String,
    /** 小数位数 */
    val digits: Int,
    /** 最小手数 */
    val minLot: String,
    /** 最大手数 */
    val maxLot: String,
    /** 步进手数 */
    val lotStep: String,
)
