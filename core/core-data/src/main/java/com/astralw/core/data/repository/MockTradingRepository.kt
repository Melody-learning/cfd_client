package com.astralw.core.data.repository

import com.astralw.core.data.model.Deal
import com.astralw.core.data.model.Order
import com.astralw.core.data.model.OrderDirection
import com.astralw.core.data.model.OrderStatus
import com.astralw.core.data.model.OrderType
import com.astralw.core.data.model.PendingOrder
import com.astralw.domain.math.MarginCalculator
import com.astralw.domain.math.PnLCalculator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mock 交易数据源
 */
@Singleton
class MockTradingRepository @Inject constructor() : TradingRepository {

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    private var orderCounter = 0

    override fun observeOpenOrders(): Flow<List<Order>> =
        _orders.asStateFlow().map { orders ->
            orders.filter { it.status == OrderStatus.OPEN }
        }

    override fun observeClosedOrders(): Flow<List<Order>> =
        _orders.asStateFlow().map { orders ->
            orders.filter { it.status == OrderStatus.CLOSED }
        }

    override suspend fun refreshPositions() {
        // Mock: 无需刷新
    }

    override suspend fun openOrder(
        symbol: String,
        displayName: String,
        direction: OrderDirection,
        lots: String,
        stopLoss: String?,
        takeProfit: String?,
        orderType: OrderType,
        price: String?,
        expiration: Long?,
    ): Result<Order> {
        delay(800)

        val price = getMockPrice(symbol)
        val contractSize = getContractSize(symbol)

        val margin = MarginCalculator.requiredMargin(
            lots = lots,
            contractSize = contractSize,
            price = price,
            leverage = "100",
        )

        val order = Order(
            orderId = "ORD-${++orderCounter}",
            symbol = symbol,
            displayName = displayName,
            direction = direction,
            lots = lots,
            openPrice = price,
            currentPrice = price,
            stopLoss = stopLoss,
            takeProfit = takeProfit,
            floatingPnL = "0.00",
            margin = margin,
            openTime = System.currentTimeMillis(),
            status = OrderStatus.OPEN,
        )

        _orders.value = _orders.value + order
        return Result.success(order)
    }

    override suspend fun closeOrder(orderId: String): Result<Order> {
        delay(500)

        val index = _orders.value.indexOfFirst { it.orderId == orderId }
        if (index == -1) return Result.failure(IllegalArgumentException("Order not found"))

        val updated = _orders.value.toMutableList()
        updated[index] = updated[index].copy(status = OrderStatus.CLOSED)
        _orders.value = updated

        return Result.success(updated[index])
    }

    override suspend fun modifyOrder(
        orderId: String,
        stopLoss: String?,
        takeProfit: String?,
    ): Result<Order> {
        delay(500)

        val index = _orders.value.indexOfFirst { it.orderId == orderId }
        if (index == -1) return Result.failure(IllegalArgumentException("Order not found"))

        val updated = _orders.value.toMutableList()
        updated[index] = updated[index].copy(
            stopLoss = stopLoss,
            takeProfit = takeProfit,
        )
        _orders.value = updated

        return Result.success(updated[index])
    }

    override suspend fun getHistoryDeals(fromSec: Long, toSec: Long): Result<List<Deal>> {
        delay(300)
        return Result.success(emptyList())
    }

    override suspend fun getPendingOrders(): Result<List<PendingOrder>> {
        delay(300)
        return Result.success(emptyList())
    }

    override suspend fun cancelPendingOrder(ticket: Long): Result<Unit> {
        delay(300)
        return Result.success(Unit)
    }

    private fun getMockPrice(symbol: String): String = when (symbol) {
        "EURUSD" -> "1.08550"
        "GBPUSD" -> "1.29340"
        "USDJPY" -> "149.850"
        "AUDUSD" -> "0.63720"
        "USDCAD" -> "1.44250"
        "USDCHF" -> "0.88350"
        "NZDUSD" -> "0.56980"
        "XAUUSD" -> "2985.50"
        "XAGUSD" -> "33.450"
        "US30" -> "41250.5"
        "BTCUSD" -> "87500.00"
        "ETHUSD" -> "2050.00"
        else -> "1.00000"
    }

    private fun getContractSize(symbol: String): String = when (symbol) {
        "XAUUSD" -> "100"
        "XAGUSD" -> "5000"
        "US30" -> "1"
        "BTCUSD", "BTCEUR", "ETHUSD" -> "1"
        else -> "100000"
    }
}

