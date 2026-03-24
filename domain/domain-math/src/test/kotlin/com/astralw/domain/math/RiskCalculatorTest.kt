package com.astralw.domain.math

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RiskCalculatorTest {

    @Test
    fun `evaluate - safe when margin level above 200`() {
        assertThat(RiskCalculator.evaluate("500.00")).isEqualTo(RiskCalculator.RiskLevel.SAFE)
        assertThat(RiskCalculator.evaluate("200.01")).isEqualTo(RiskCalculator.RiskLevel.SAFE)
    }

    @Test
    fun `evaluate - warning when margin level between 100 and 200`() {
        assertThat(RiskCalculator.evaluate("200.00")).isEqualTo(RiskCalculator.RiskLevel.WARNING)
        assertThat(RiskCalculator.evaluate("150.00")).isEqualTo(RiskCalculator.RiskLevel.WARNING)
        assertThat(RiskCalculator.evaluate("100.01")).isEqualTo(RiskCalculator.RiskLevel.WARNING)
    }

    @Test
    fun `evaluate - danger when margin level at or below 100`() {
        assertThat(RiskCalculator.evaluate("100.00")).isEqualTo(RiskCalculator.RiskLevel.DANGER)
        assertThat(RiskCalculator.evaluate("50.00")).isEqualTo(RiskCalculator.RiskLevel.DANGER)
        assertThat(RiskCalculator.evaluate("0.00")).isEqualTo(RiskCalculator.RiskLevel.DANGER)
    }

    @Test
    fun `evaluate - infinity is safe`() {
        assertThat(RiskCalculator.evaluate("∞")).isEqualTo(RiskCalculator.RiskLevel.SAFE)
    }
}
