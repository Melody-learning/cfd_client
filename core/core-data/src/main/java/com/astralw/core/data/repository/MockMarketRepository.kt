package com.astralw.core.data.repository

import com.astralw.core.data.model.Quote
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.coroutineContext
import kotlin.random.Random

/**
 * Mock 行情数据源
 *
 * 在后端 Gateway 集成 MT5 之前，使用模拟数据进行前端开发
 */
@Singleton
class MockMarketRepository @Inject constructor() : MarketRepository {

    private val _quotes = MutableStateFlow(generateInitialQuotes())

    override fun observeQuotes(): Flow<List<Quote>> = _quotes.asStateFlow()

    override suspend fun getSymbols(): List<Quote> = _quotes.value

    override suspend fun startStreaming() {
        while (coroutineContext.isActive) {
            delay(800L + Random.nextLong(400))
            _quotes.value = simulatePriceUpdate(_quotes.value)
        }
    }

    override suspend fun stopStreaming() {
        // Mock 实现中由协程取消控制
    }

    private fun generateInitialQuotes(): List<Quote> = listOf(
        mockQuote("EURUSD", "EUR/USD", "forex", "1.08550", "1.08562", "1.08800", "1.08200", 5),
        mockQuote("GBPUSD", "GBP/USD", "forex", "1.29340", "1.29355", "1.29600", "1.29100", 5),
        mockQuote("USDJPY", "USD/JPY", "forex", "149.850", "149.868", "150.200", "149.500", 3),
        mockQuote("AUDUSD", "AUD/USD", "forex", "0.63720", "0.63735", "0.63900", "0.63500", 5),
        mockQuote("USDCAD", "USD/CAD", "forex", "1.44250", "1.44268", "1.44500", "1.44000", 5),
        mockQuote("USDCHF", "USD/CHF", "forex", "0.88350", "0.88368", "0.88600", "0.88100", 5),
        mockQuote("NZDUSD", "NZD/USD", "forex", "0.56980", "0.56998", "0.57200", "0.56800", 5),
        mockQuote("XAUUSD", "Gold", "commodities", "2985.50", "2986.10", "2995.00", "2975.00", 2),
        mockQuote("XAGUSD", "Silver", "commodities", "33.450", "33.480", "33.800", "33.200", 3),
        mockQuote("US30", "Dow Jones", "indices", "41250.5", "41253.5", "41500.0", "41000.0", 1),
    )

    private fun mockQuote(
        symbol: String,
        displayName: String,
        category: String,
        bid: String,
        ask: String,
        high: String,
        low: String,
        digits: Int,
    ): Quote {
        val bidDec = BigDecimal(bid)
        val askDec = BigDecimal(ask)
        val spread = askDec.subtract(bidDec)
            .multiply(BigDecimal(10).pow(digits))
            .setScale(1, RoundingMode.HALF_UP)

        return Quote(
            symbol = symbol,
            displayName = displayName,
            category = category,
            bid = bid,
            ask = ask,
            high = high,
            low = low,
            change = "+0.00050",
            changePercent = "+0.05",
            spread = spread.toPlainString(),
            digits = digits,
            timestamp = System.currentTimeMillis(),
        )
    }

    /**
     * 模拟价格波动
     */
    private fun simulatePriceUpdate(quotes: List<Quote>): List<Quote> {
        return quotes.map { quote ->
            val bidDec = BigDecimal(quote.bid)
            val scale = quote.digits

            // 随机波动幅度 (±几个 pip)
            val pipSize = BigDecimal.ONE.movePointLeft(scale)
            val pips = BigDecimal(Random.nextInt(-5, 6))
            val delta = pipSize.multiply(pips)

            val newBid = bidDec.add(delta).setScale(scale, RoundingMode.HALF_UP)
            val spreadPips = BigDecimal(quote.spread)
            val newAsk = newBid.add(
                spreadPips.multiply(pipSize)
            ).setScale(scale, RoundingMode.HALF_UP)

            // 计算涨跌
            val change = delta.setScale(scale, RoundingMode.HALF_UP)
            val changePercent = if (bidDec.compareTo(BigDecimal.ZERO) != 0) {
                delta.divide(bidDec, 6, RoundingMode.HALF_UP)
                    .multiply(BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP)
            } else BigDecimal.ZERO

            val changeStr = if (change >= BigDecimal.ZERO) "+${change.toPlainString()}" else change.toPlainString()
            val changePctStr = if (changePercent >= BigDecimal.ZERO) "+${changePercent.toPlainString()}" else changePercent.toPlainString()

            quote.copy(
                bid = newBid.toPlainString(),
                ask = newAsk.toPlainString(),
                change = changeStr,
                changePercent = changePctStr,
                timestamp = System.currentTimeMillis(),
            )
        }
    }
}
