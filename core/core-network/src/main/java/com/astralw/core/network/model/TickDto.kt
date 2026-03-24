package com.astralw.core.network.model

import kotlinx.serialization.Serializable

/**
 * 行情 Tick 数据
 *
 * 铁律: 全站禁 Double，价格用 String 传输
 */
@Serializable
data class TickDto(
    /** 交易品种 (e.g., "EURUSD") */
    val symbol: String,
    /** 买入价 */
    val bid: String,
    /** 卖出价 */
    val ask: String,
    /** 最高价 */
    val high: String,
    /** 最低价 */
    val low: String,
    /** 涨跌幅 (e.g., "+0.15") */
    val change: String,
    /** 涨跌百分比 (e.g., "+0.014") */
    val changePercent: String,
    /** 点差 (e.g., "1.2") */
    val spread: String,
    /** 时间戳 (毫秒) */
    val timestamp: Long,
)
