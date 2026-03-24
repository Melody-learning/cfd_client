package com.astralw.core.data.model

/**
 * 历史成交记录领域模型
 */
data class Deal(
    /** MT5 成交单号 */
    val dealId: Long,
    /** MT5 订单号 */
    val orderId: Long,
    /** 品种 */
    val symbol: String,
    /** 显示名 */
    val displayName: String,
    /** 方向: BUY / SELL */
    val direction: String,
    /** 手数 (已换算) */
    val lots: String,
    /** 成交价格 */
    val price: String,
    /** 盈亏 */
    val profit: String,
    /** 手续费 */
    val commission: String,
    /** 隔夜利息 */
    val swap: String,
    /** 成交时间 (毫秒) */
    val timeMs: Long,
    /** 备注 */
    val comment: String,
)
