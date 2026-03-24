package com.astralw.core.data.model

/**
 * 行情报价 — UI 层模型
 *
 * 铁律: 全站禁止 Double，价格用 String
 */
data class Quote(
    val symbol: String,
    val displayName: String,
    val category: String,
    val bid: String,
    val ask: String,
    val high: String,
    val low: String,
    val change: String,
    val changePercent: String,
    val spread: String,
    val digits: Int,
    val timestamp: Long,
) {
    /** 是否涨 */
    val isUp: Boolean
        get() = change.isNotEmpty() && !change.startsWith("-")
}
