package com.astralw.domain.math

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MarginCalculatorTest {

    @Test
    fun `requiredMargin - standard forex lot EURUSD`() {
        // 1 standard lot of EURUSD at 1.08550, leverage 100
        val result = MarginCalculator.requiredMargin(
            lots = "1.00",
            contractSize = "100000",
            price = "1.08550",
            leverage = "100"
        )
        assertThat(result).isEqualTo("1085.50")
    }

    @Test
    fun `requiredMargin - mini lot`() {
        // 0.01 lot of EURUSD at 1.08550, leverage 100
        val result = MarginCalculator.requiredMargin(
            lots = "0.01",
            contractSize = "100000",
            price = "1.08550",
            leverage = "100"
        )
        assertThat(result).isEqualTo("10.86")
    }

    @Test
    fun `requiredMargin - high leverage`() {
        // 1 lot at 1.08550, leverage 500
        val result = MarginCalculator.requiredMargin(
            lots = "1.00",
            contractSize = "100000",
            price = "1.08550",
            leverage = "500"
        )
        assertThat(result).isEqualTo("217.10")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `requiredMargin - zero leverage throws`() {
        MarginCalculator.requiredMargin(
            lots = "1.00",
            contractSize = "100000",
            price = "1.08550",
            leverage = "0"
        )
    }

    @Test
    fun `freeMargin - positive`() {
        val result = MarginCalculator.freeMargin(
            equity = "10000.00",
            usedMargin = "1085.50"
        )
        assertThat(result).isEqualTo("8914.50")
    }

    @Test
    fun `freeMargin - negative when underwater`() {
        val result = MarginCalculator.freeMargin(
            equity = "800.00",
            usedMargin = "1085.50"
        )
        assertThat(result).isEqualTo("-285.50")
    }

    @Test
    fun `marginLevel - normal case`() {
        val result = MarginCalculator.marginLevel(
            equity = "10000.00",
            usedMargin = "1085.50"
        )
        assertThat(result).isEqualTo("921.23")
    }

    @Test
    fun `marginLevel - zero used margin returns infinity`() {
        val result = MarginCalculator.marginLevel(
            equity = "10000.00",
            usedMargin = "0"
        )
        assertThat(result).isEqualTo("∞")
    }

    @Test
    fun `marginLevel - at 100 percent threshold`() {
        val result = MarginCalculator.marginLevel(
            equity = "1085.50",
            usedMargin = "1085.50"
        )
        assertThat(result).isEqualTo("100.00")
    }
}
