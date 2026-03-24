package com.astralw.core.data.model

/**
 * K 线蜡烛图数据
 *
 * 铁律: 全站禁止 Double，价格用 String
 */
data class Candle(
    /** 时间戳（毫秒） */
    val timestamp: Long,
    /** 开盘价 */
    val open: String,
    /** 最高价 */
    val high: String,
    /** 最低价 */
    val low: String,
    /** 收盘价 */
    val close: String,
    /** 成交量 */
    val volume: String,
) {
    /** 是否阳线（收盘 >= 开盘） */
    val isBullish: Boolean
        get() {
            val o = open.toBigDecimalOrNull() ?: return true
            val c = close.toBigDecimalOrNull() ?: return true
            return c >= o
        }
}
