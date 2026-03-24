package com.astralw.domain.math

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PnLCalculatorTest {

    @Test
    fun `floatingPnL - BUY profit`() {
        val result = PnLCalculator.floatingPnL(
            direction = PnLCalculator.Direction.BUY,
            openPrice = "1.08000",
            currentPrice = "1.08550",
            lots = "1.00",
            contractSize = "100000"
        )
        assertThat(result).isEqualTo("550.00")
    }

    @Test
    fun `floatingPnL - BUY loss`() {
        val result = PnLCalculator.floatingPnL(
            direction = PnLCalculator.Direction.BUY,
            openPrice = "1.08550",
            currentPrice = "1.08000",
            lots = "1.00",
            contractSize = "100000"
        )
        assertThat(result).isEqualTo("-550.00")
    }

    @Test
    fun `floatingPnL - SELL profit`() {
        val result = PnLCalculator.floatingPnL(
            direction = PnLCalculator.Direction.SELL,
            openPrice = "1.08550",
            currentPrice = "1.08000",
            lots = "1.00",
            contractSize = "100000"
        )
        assertThat(result).isEqualTo("550.00")
    }

    @Test
    fun `floatingPnL - SELL loss`() {
        val result = PnLCalculator.floatingPnL(
            direction = PnLCalculator.Direction.SELL,
            openPrice = "1.08000",
            currentPrice = "1.08550",
            lots = "1.00",
            contractSize = "100000"
        )
        assertThat(result).isEqualTo("-550.00")
    }

    @Test
    fun `floatingPnL - mini lot`() {
        val result = PnLCalculator.floatingPnL(
            direction = PnLCalculator.Direction.BUY,
            openPrice = "1.08000",
            currentPrice = "1.08550",
            lots = "0.01",
            contractSize = "100000"
        )
        assertThat(result).isEqualTo("5.50")
    }

    @Test
    fun `equity - with positive PnL`() {
        val result = PnLCalculator.equity(
            balance = "10000.00",
            totalFloatingPnL = "550.00"
        )
        assertThat(result).isEqualTo("10550.00")
    }

    @Test
    fun `equity - with negative PnL`() {
        val result = PnLCalculator.equity(
            balance = "10000.00",
            totalFloatingPnL = "-550.00"
        )
        assertThat(result).isEqualTo("9450.00")
    }
}
