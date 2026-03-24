package com.astralw.domain.math

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/**
 * CFD 保证金计算器
 *
 * 铁律: 全站禁止 Double，强制使用 BigDecimal
 */
object MarginCalculator {

    private val MC = MathContext(10, RoundingMode.HALF_UP)

    /**
     * 计算所需保证金 (Required Margin)
     *
     * 公式: lots × contractSize × price / leverage
     *
     * @param lots 手数 (e.g., "0.01")
     * @param contractSize 合约大小 (e.g., "100000" for Forex)
     * @param price 开仓价格 (e.g., "1.08550")
     * @param leverage 杠杆倍数 (e.g., "100")
     * @return 所需保证金 (String)
     */
    fun requiredMargin(
        lots: String,
        contractSize: String,
        price: String,
        leverage: String,
    ): String {
        val lotsDecimal = BigDecimal(lots)
        val contractDecimal = BigDecimal(contractSize)
        val priceDecimal = BigDecimal(price)
        val leverageDecimal = BigDecimal(leverage)

        require(leverageDecimal > BigDecimal.ZERO) { "Leverage must be positive" }

        return lotsDecimal
            .multiply(contractDecimal, MC)
            .multiply(priceDecimal, MC)
            .divide(leverageDecimal, MC)
            .setScale(2, RoundingMode.HALF_UP)
            .toPlainString()
    }

    /**
     * 计算可用保证金 (Free Margin)
     *
     * 公式: equity - usedMargin
     *
     * @param equity 净值
     * @param usedMargin 已用保证金
     * @return 可用保证金 (String)
     */
    fun freeMargin(equity: String, usedMargin: String): String {
        return BigDecimal(equity)
            .subtract(BigDecimal(usedMargin))
            .setScale(2, RoundingMode.HALF_UP)
            .toPlainString()
    }

    /**
     * 计算保证金水平 (Margin Level %)
     *
     * 公式: (equity / usedMargin) × 100
     *
     * @return 保证金水平百分比 (String)，如 usedMargin 为 0 则返回 "∞"
     */
    fun marginLevel(equity: String, usedMargin: String): String {
        val usedDecimal = BigDecimal(usedMargin)
        if (usedDecimal.compareTo(BigDecimal.ZERO) == 0) return "∞"

        return BigDecimal(equity)
            .divide(usedDecimal, MC)
            .multiply(BigDecimal("100"), MC)
            .setScale(2, RoundingMode.HALF_UP)
            .toPlainString()
    }
}
