package com.astralw.core.data.model

/**
 * 挂单领域模型
 *
 * 与持仓 [Order] 独立，字段差异大：
 * 挂单有 triggerPrice/expiration，无 floatingPnL/margin
 *
 * 铁律: 全站禁止 Double，金额/价格用 String
 */
data class PendingOrder(
    /** MT5 订单 ticket */
    val ticket: Long,
    /** 品种代码 */
    val symbol: String,
    /** 显示名称 */
    val displayName: String,
    /** 订单类型 */
    val type: OrderType,
    /** 手数 (已换算) */
    val lots: String,
    /** 挂单触发价格 */
    val triggerPrice: String,
    /** 止损 */
    val stopLoss: String?,
    /** 止盈 */
    val takeProfit: String?,
    /** 当前市场价 */
    val currentPrice: String,
    /** 创建时间 (毫秒) */
    val setupTime: Long,
    /** 到期时间 (Unix 秒, 0=不限期) */
    val expiration: Long,
    /** 备注 */
    val comment: String = "",
)
