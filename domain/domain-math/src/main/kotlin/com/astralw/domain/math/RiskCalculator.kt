package com.astralw.domain.math

import java.math.BigDecimal

/**
 * CFD 风险等级计算器
 */
object RiskCalculator {

    /**
     * 风险等级
     */
    enum class RiskLevel {
        /** 安全: Margin Level > 200% */
        SAFE,
        /** 警告: 100% < Margin Level <= 200% */
        WARNING,
        /** 危险: Margin Level <= 100% (即将爆仓) */
        DANGER,
    }

    /**
     * 根据保证金水平判断风险等级
     *
     * @param marginLevel 保证金水平百分比 (e.g., "150.00")，"∞" 表示无持仓
     * @return 风险等级
     */
    fun evaluate(marginLevel: String): RiskLevel {
        if (marginLevel == "∞") return RiskLevel.SAFE

        val level = BigDecimal(marginLevel)
        return when {
            level > BigDecimal("200") -> RiskLevel.SAFE
            level > BigDecimal("100") -> RiskLevel.WARNING
            else -> RiskLevel.DANGER
        }
    }
}
