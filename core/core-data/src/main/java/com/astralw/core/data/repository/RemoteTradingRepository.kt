package com.astralw.core.data.repository

import com.astralw.core.data.model.Deal
import com.astralw.core.data.model.Order
import com.astralw.core.data.model.OrderDirection
import com.astralw.core.data.model.OrderStatus
import com.astralw.core.network.api.AstralWApiService
import com.astralw.core.network.model.PositionDto
import com.astralw.core.network.model.TradeCloseRequestDto
import com.astralw.core.network.model.TradeModifyRequestDto
import com.astralw.core.network.model.TradeOpenRequestDto
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 远程交易 Repository — 通过 Gateway 执行交易操作
 */
@Singleton
class RemoteTradingRepository @Inject constructor(
    private val api: AstralWApiService,
) : TradingRepository {

    private val openOrders = MutableStateFlow<List<Order>>(emptyList())
    private val closedOrders = MutableStateFlow<List<Order>>(emptyList())

    override fun observeOpenOrders(): Flow<List<Order>> = openOrders.asStateFlow()

    override fun observeClosedOrders(): Flow<List<Order>> = closedOrders.asStateFlow()

    override suspend fun openOrder(
        symbol: String,
        displayName: String,
        direction: OrderDirection,
        lots: String,
        stopLoss: String?,
        takeProfit: String?,
    ): Result<Order> {
        return try {
            // 记录当前持仓 ID，用于检测新增持仓
            val existingIds = openOrders.value.map { it.orderId }.toSet()

            val result = api.tradeOpen(
                TradeOpenRequestDto(
                    symbol = symbol,
                    direction = if (direction == OrderDirection.BUY) "BUY" else "SELL",
                    lots = lots,
                    stopLoss = stopLoss?.takeIf { it.isNotBlank() && it != "0" },
                    takeProfit = takeProfit?.takeIf { it.isNotBlank() && it != "0" },
                ),
            )

            when (result.retcode) {
                // 10009 = Done(成交成功), 10008 = 挂单已挂出
                RETCODE_DONE, RETCODE_PLACED -> {
                    // 同步返回成交结果，刷新持仓并构建 Order
                    refreshPositions()
                    val order = Order(
                        orderId = result.order.toString(),
                        symbol = symbol,
                        displayName = displayName,
                        direction = direction,
                        lots = mt5VolToLots(result.volume),
                        openPrice = result.price,
                        currentPrice = result.price,
                        stopLoss = stopLoss,
                        takeProfit = takeProfit,
                        floatingPnL = "0.00",
                        margin = "0.00",
                        openTime = System.currentTimeMillis(),
                        status = OrderStatus.OPEN,
                    )
                    Result.success(order)
                }

                // 0 = 已提交但无法即时确认，轮询持仓
                RETCODE_SUBMITTED -> {
                    var newOrder: Order? = null
                    repeat(MAX_POLL_ATTEMPTS) {
                        delay(POLL_DELAY_MS)
                        refreshPositions()
                        newOrder = openOrders.value.find { it.orderId !in existingIds }
                        if (newOrder != null) return@repeat
                    }

                    if (newOrder != null) {
                        Result.success(newOrder!!)
                    } else {
                        // 轮询超时但请求已提交，返回临时 Order
                        refreshPositions()
                        val pendingOrder = Order(
                            orderId = "PENDING-${System.currentTimeMillis()}",
                            symbol = symbol,
                            displayName = displayName,
                            direction = direction,
                            lots = lots,
                            openPrice = "",
                            currentPrice = "",
                            stopLoss = stopLoss,
                            takeProfit = takeProfit,
                            floatingPnL = "0.00",
                            margin = "0.00",
                            openTime = System.currentTimeMillis(),
                            status = OrderStatus.OPEN,
                        )
                        Result.success(pendingOrder)
                    }
                }

                // 其他 retcode = 失败
                else -> Result.failure(Exception(result.message))
            }
        } catch (e: retrofit2.HttpException) {
            val friendlyMsg = parseTradeError(e)
            Result.failure(Exception(friendlyMsg))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun closeOrder(orderId: String): Result<Order> {
        return try {
            api.tradeClose(TradeCloseRequestDto(position = orderId.toLong()))
            // 刷新持仓
            delay(200)
            refreshPositions()

            val closed = Order(
                orderId = orderId,
                symbol = "",
                displayName = "",
                direction = OrderDirection.BUY,
                lots = "0",
                openPrice = "0",
                currentPrice = "0",
                stopLoss = null,
                takeProfit = null,
                floatingPnL = "0",
                margin = "0",
                openTime = 0,
                status = OrderStatus.CLOSED,
            )
            Result.success(closed)
        } catch (e: retrofit2.HttpException) {
            Result.failure(Exception(parseTradeError(e)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun modifyOrder(
        orderId: String,
        stopLoss: String?,
        takeProfit: String?,
    ): Result<Order> {
        return try {
            api.tradeModify(
                TradeModifyRequestDto(
                    position = orderId.toLong(),
                    stopLoss = stopLoss ?: "0",
                    takeProfit = takeProfit ?: "0",
                ),
            )
            refreshPositions()
            // 返回当前列表中匹配的持仓
            val current = openOrders.value.find { it.orderId == orderId }
            Result.success(current ?: error("Position not found after modify"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 从后端刷新持仓列表
     */
    override suspend fun refreshPositions() {
        try {
            val resp = api.getPositions()
            openOrders.value = resp.positions.map { it.toOrder() }
        } catch (_: Exception) {
            // 保持上次数据
        }
    }

    private fun PositionDto.toOrder(): Order {
        val dir = if (direction.equals("BUY", ignoreCase = true)) OrderDirection.BUY else OrderDirection.SELL
        return Order(
            orderId = position.toString(),
            symbol = symbol,
            displayName = symbol.mapToDisplayName(),
            direction = dir,
            lots = lots,
            openPrice = priceOpen,
            currentPrice = priceCurrent,
            stopLoss = stopLoss.takeIf { it != "0" && it != "0.0" && it != "0.00000" && it.isNotEmpty() },
            takeProfit = takeProfit.takeIf { it != "0" && it != "0.0" && it != "0.00000" && it.isNotEmpty() },
            floatingPnL = profit,
            margin = "0.00",
            openTime = timeCreate * 1000,
            status = OrderStatus.OPEN,
        )
    }

    override suspend fun getHistoryDeals(fromSec: Long, toSec: Long): Result<List<Deal>> {
        return try {
            val resp = api.getHistoryDeals(from = fromSec, to = toSec)
            val deals = resp.deals.map { dto ->
                Deal(
                    dealId = dto.deal,
                    orderId = dto.order,
                    symbol = dto.symbol,
                    displayName = dto.symbol.mapToDisplayName(),
                    direction = dto.direction,
                    lots = mt5VolToLots(dto.volume),
                    price = dto.price,
                    profit = dto.profit,
                    commission = dto.commission,
                    swap = dto.swap,
                    timeMs = dto.time * 1000,
                    comment = dto.comment,
                )
            }
            Result.success(deals)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        /** MT5 retcode: 成交成功 (Done) */
        private const val RETCODE_DONE = "10009"
        /** MT5 retcode: 挂单已挂出 (Placed) */
        private const val RETCODE_PLACED = "10008"
        /** MT5 retcode: 已提交但未即时确认 */
        private const val RETCODE_SUBMITTED = "0"

        /** 轮询间隔 (毫秒) */
        private const val POLL_DELAY_MS = 500L
        /** 最大轮询次数 (500ms × 10 = 5s 超时) */
        private const val MAX_POLL_ATTEMPTS = 10

        /** MT5 Volume 单位 */
        private const val MT5_VOL_DIVISOR = 10_000L

        /**
         * 解析 HTTP 错误响应中的交易错误信息
         *
         * 后端返回格式:
         * {"detail":{"error":{"code":"TRADE_FAILED","message":"...","mt5_retcode":"10019"}}}
         */
        private fun parseTradeError(e: retrofit2.HttpException): String {
            return try {
                val body = e.response()?.errorBody()?.string() ?: return "Trade failed (${e.code()})"
                // 简单解析 mt5_retcode
                val retcodeMatch = Regex("\"mt5_retcode\"\\s*:\\s*\"(\\d+)\"").find(body)
                val retcode = retcodeMatch?.groupValues?.get(1)
                if (retcode != null) {
                    mt5RetcodeToMessage(retcode)
                } else {
                    // 尝试解析 message
                    val msgMatch = Regex("\"message\"\\s*:\\s*\"([^\"]+)\"").find(body)
                    msgMatch?.groupValues?.get(1) ?: "Trade failed (${e.code()})"
                }
            } catch (_: Exception) {
                "Trade failed (${e.code()})"
            }
        }

        /**
         * MT5 retcode 映射为用户友好的提示
         */
        private fun mt5RetcodeToMessage(retcode: String): String = when (retcode) {
            "10004" -> "Requote - price has changed, please try again"
            "10006" -> "Request rejected by server"
            "10007" -> "Request cancelled by trader"
            "10010" -> "Only partial order was filled"
            "10011" -> "Request handling error"
            "10012" -> "Request cancelled due to timeout"
            "10013" -> "Invalid trade request"
            "10014" -> "Invalid volume"
            "10015" -> "Invalid price"
            "10016" -> "Invalid stop loss or take profit"
            "10017" -> "Trading disabled"
            "10018" -> "Market is closed"
            "10019" -> "Not enough money"
            "10020" -> "Price has changed, please try again"
            "10021" -> "No quotes available"
            "10022" -> "Invalid order expiration"
            "10024" -> "Too frequent requests, please wait"
            "10025" -> "No changes in request"
            "10026" -> "Auto-trading disabled on server"
            "10027" -> "Auto-trading disabled on client"
            "10030" -> "Invalid fill type"
            "10031" -> "No connection to trading server"
            "10032" -> "Operation only for live accounts"
            "10033" -> "Orders limit reached"
            "10034" -> "Volume limit for this symbol reached"
            "10036" -> "Trading disabled"
            "10038" -> "Position already closed"
            else -> "Trade failed (MT5 code: $retcode)"
        }


        /**
         * MT5 内部 Volume 转前端手数
         *
         * MT5 格式: 100 = 0.01 手, 10000 = 1.0 手
         * 公式: 手数 = Volume ÷ 10000
         */
        private fun mt5VolToLots(mt5Volume: String): String {
            val vol = mt5Volume.toLongOrNull() ?: return mt5Volume
            if (vol <= 0) return mt5Volume
            val lotsBd = java.math.BigDecimal(vol)
                .divide(java.math.BigDecimal(MT5_VOL_DIVISOR), 2, java.math.RoundingMode.HALF_UP)
            return lotsBd.stripTrailingZeros().toPlainString()
        }

        private fun String.mapToDisplayName(): String {
            if (length == 6) {
                return "${substring(0, 3)}/${substring(3, 6)}"
            }
            return this
        }
    }
}
