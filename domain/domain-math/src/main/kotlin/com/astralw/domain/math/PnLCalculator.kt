package com.astralw.domain.math

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/**
 * CFD 盈亏计算器
 *
 * 铁律: 全站禁止 Double，强制使用 BigDecimal
 */
object PnLCalculator {

    private val MC = MathContext(10, RoundingMode.HALF_UP)

    /**
     * 交易方向
     */
    enum class Direction { BUY, SELL }

    /**
     * 计算浮动盈亏 (Floating P&L)
     *
     * BUY:  (currentPrice - openPrice) × lots × contractSize
     * SELL: (openPrice - currentPrice) × lots × contractSize
     *
     * @param direction 交易方向
     * @param openPrice 开仓价格
     * @param currentPrice 当前价格
     * @param lots 手数
     * @param contractSize 合约大小
     * @return 浮动盈亏 (String)
     */
    fun floatingPnL(
        direction: Direction,
        openPrice: String,
        currentPrice: String,
        lots: String,
        contractSize: String,
    ): String {
        val open = BigDecimal(openPrice)
        val current = BigDecimal(currentPrice)
        val lotsDecimal = BigDecimal(lots)
        val contract = BigDecimal(contractSize)

        val priceDiff = when (direction) {
            Direction.BUY -> current.subtract(open)
            Direction.SELL -> open.subtract(current)
        }

        return priceDiff
            .multiply(lotsDecimal, MC)
            .multiply(contract, MC)
            .setScale(2, RoundingMode.HALF_UP)
            .toPlainString()
    }

    /**
     * 计算净值 (Equity)
     *
     * 公式: balance + totalFloatingPnL
     */
    fun equity(balance: String, totalFloatingPnL: String): String {
        return BigDecimal(balance)
            .add(BigDecimal(totalFloatingPnL))
            .setScale(2, RoundingMode.HALF_UP)
            .toPlainString()
    }
}
