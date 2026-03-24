package com.astralw.core.data.repository

import com.astralw.core.data.model.Deal
import com.astralw.core.data.model.Order
import com.astralw.core.data.model.OrderDirection
import kotlinx.coroutines.flow.Flow

/**
 * 交易 Repository 接口
 */
interface TradingRepository {

    /** 观察当前持仓 */
    fun observeOpenOrders(): Flow<List<Order>>

    /** 刷新持仓列表（从后端重新获取） */
    suspend fun refreshPositions()

    /** 观察历史订单 */
    fun observeClosedOrders(): Flow<List<Order>>

    /** 开仓 */
    suspend fun openOrder(
        symbol: String,
        displayName: String,
        direction: OrderDirection,
        lots: String,
        stopLoss: String?,
        takeProfit: String?,
    ): Result<Order>

    /** 平仓 */
    suspend fun closeOrder(orderId: String): Result<Order>

    /** 修改止损止盈 */
    suspend fun modifyOrder(
        orderId: String,
        stopLoss: String?,
        takeProfit: String?,
    ): Result<Order>

    /** 获取历史成交记录 */
    suspend fun getHistoryDeals(fromSec: Long, toSec: Long): Result<List<Deal>>
}

