package com.astralw.core.data.repository

import android.util.Log
import com.astralw.core.data.model.Quote
import com.astralw.core.network.api.AstralWApiService
import com.astralw.core.network.api.MarketWebSocketService
import com.astralw.core.network.model.QuoteDto
import com.astralw.core.network.model.TickStatDto
import com.astralw.core.network.model.WsQuoteData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.coroutineContext

/**
 * RemoteMarketRepository: 行情数据仓库
 *
 * 策略: WebSocket 推送为主 → HTTP 轮询降级兜底
 * - WebSocket 接收单品种 tick，合并到 quotesMap 后发射完整列表
 * - WebSocket 断线时自动切换到 HTTP 轮询
 * - 自动重连 WebSocket
 *
 * Change% baseline: D1 candle open price (UTC 00:00)
 */
@Singleton
class RemoteMarketRepository @Inject constructor(
    private val api: AstralWApiService,
    private val wsService: MarketWebSocketService,
) : MarketRepository {

    private val quotesFlow = MutableStateFlow<List<Quote>>(emptyList())
    private var symbolList: List<String> = emptyList()

    /** 最新行情快照（symbol → Quote），WebSocket 增量更新用 */
    private val quotesMap = mutableMapOf<String, Quote>()

    // D1 open prices: symbol → open price (fetched once per session)
    private val dailyOpen = mutableMapOf<String, BigDecimal>()
    private var dailyOpenLoaded = false

    override fun observeQuotes(): Flow<List<Quote>> = quotesFlow.asStateFlow()

    override suspend fun getSymbols(): List<Quote> {
        return try {
            val resp = api.getSymbols()
            symbolList = resp.symbols.map { it.symbol }.take(MAX_SYMBOLS)
            Log.d(TAG, "getSymbols: ${symbolList.size} symbols")
            fetchQuotes()
        } catch (e: Exception) {
            Log.e(TAG, "getSymbols failed", e)
            emptyList()
        }
    }

    override suspend fun startStreaming() {
        if (symbolList.isEmpty()) {
            // 重试获取品种列表（应对 503 等临时故障）
            var attempts = 0
            while (symbolList.isEmpty() && attempts < MAX_RETRY_ATTEMPTS) {
                try {
                    val resp = api.getSymbols()
                    symbolList = resp.symbols.map { it.symbol }.take(MAX_SYMBOLS)
                    Log.d(TAG, "startStreaming: loaded ${symbolList.size} symbols")
                } catch (e: Exception) {
                    attempts++
                    Log.w(TAG, "startStreaming: getSymbols attempt $attempts failed: ${e.message}")
                    if (attempts < MAX_RETRY_ATTEMPTS) {
                        delay(RETRY_DELAY_MS)
                    }
                }
            }
            if (symbolList.isEmpty()) {
                Log.e(TAG, "startStreaming: failed to load symbols after $MAX_RETRY_ATTEMPTS attempts")
                return
            }
        }

        // 1. 立即发第一批报价（先让列表渲染出来）
        try {
            val firstQuotes = fetchQuotes()
            if (firstQuotes.isNotEmpty()) {
                firstQuotes.forEach { quotesMap[it.symbol] = it }
                quotesFlow.value = firstQuotes
                Log.d(TAG, "startStreaming: first quotes emitted (${firstQuotes.size})")
            }
        } catch (e: Exception) {
            Log.w(TAG, "startStreaming: first fetch failed: ${e.message}")
        }

        // 2. 异步加载 D1 开盘价（不阻塞行情流）
        if (!dailyOpenLoaded) {
            CoroutineScope(coroutineContext).launch {
                loadDailyOpenPrices()
            }
        }

        // 3. 启动 WebSocket 行情收集（异步）
        CoroutineScope(coroutineContext).launch {
            collectWebSocketQuotes()
        }

        // 4. WebSocket 连接 + HTTP 降级轮询
        wsService.connect()
        wsService.subscribe(symbolList)
        Log.d(TAG, "startStreaming: WebSocket connect requested")

        // 降级轮询：仅在 WebSocket 断线时生效
        while (coroutineContext.isActive) {
            if (!wsService.isConnected) {
                Log.d(TAG, "startStreaming: WS disconnected, HTTP poll fallback")
                try {
                    val quotes = fetchQuotes()
                    if (quotes.isNotEmpty()) {
                        quotes.forEach { quotesMap[it.symbol] = it }
                        quotesFlow.value = quotes
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "poll failed", e)
                }
                // 尝试重连 WebSocket
                wsService.connect()
                wsService.subscribe(symbolList)
            }
            delay(POLL_INTERVAL_MS)
        }
    }

    override suspend fun stopStreaming() {
        wsService.disconnect()
    }

    /**
     * 收集 WebSocket 推送的行情，合并到 quotesMap 后发射完整列表
     */
    private suspend fun collectWebSocketQuotes() {
        wsService.quoteFlow.collect { wsQuote ->
            val quote = wsQuote.toQuote()
            quotesMap[quote.symbol] = quote
            // 发射完整列表（ViewModel 侧 sample(500) 会节流）
            quotesFlow.value = quotesMap.values.toList()
        }
    }

    /**
     * Fetch D1 candle for today to get open price as change% baseline.
     */
    private suspend fun loadDailyOpenPrices() {
        Log.d(TAG, "loadDailyOpen: fetching D1 candles for ${symbolList.size} symbols")

        symbolList.chunked(BATCH_SIZE).forEach { batch ->
            batch.forEach { symbol ->
                try {
                    val resp = api.getCandles(symbol, "D1", count = 1)
                    val firstCandle = resp.candles.firstOrNull()
                    if (firstCandle != null) {
                        val openDec = firstCandle.open.toBigDecimalOrNull()
                        if (openDec != null && openDec > BigDecimal.ZERO) {
                            dailyOpen[symbol] = openDec
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "D1 open failed for $symbol: ${e.message}")
                }
            }
        }
        dailyOpenLoaded = true
        Log.d(TAG, "dailyOpen loaded: ${dailyOpen.size}/${symbolList.size}")
    }

    /**
     * HTTP 批量获取报价（并发请求）
     */
    private suspend fun fetchQuotes(): List<Quote> {
        if (symbolList.isEmpty()) return emptyList()

        return coroutineScope {
            symbolList.chunked(BATCH_SIZE).map { batch ->
                async {
                    val joined = batch.joinToString(",")
                    fetchBatchTickStat(joined) ?: fetchBatchQuotes(joined) ?: emptyList()
                }
            }.awaitAll().flatten()
        }
    }

    private suspend fun fetchBatchTickStat(symbols: String): List<Quote>? {
        return try {
            val resp = api.getTickStat(symbols)
            resp.stats.map { it.toQuote() }
        } catch (e: Exception) {
            Log.w(TAG, "tick_stat failed, fallback: ${e.message}")
            null
        }
    }

    private suspend fun fetchBatchQuotes(symbols: String): List<Quote>? {
        return try {
            val resp = api.getQuotes(symbols)
            resp.quotes.map { it.toQuote() }
        } catch (e: Exception) {
            Log.e(TAG, "quotes also failed: ${e.message}")
            null
        }
    }

    // ─── DTO → Quote 转换 ───

    private fun WsQuoteData.toQuote(): Quote {
        val bidDec = bid.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val askDec = ask.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val d = digits.coerceAtLeast(2)
        val spreadDec = (askDec - bidDec).abs().setScale(d, RoundingMode.HALF_UP)
        val (changeAbs, changePct) = calcChange(symbol, bidDec, d)

        return Quote(
            symbol = symbol,
            displayName = symbol.mapToDisplayName(),
            category = "forex",
            bid = bid,
            ask = ask,
            high = high,
            low = low,
            change = changeAbs,
            changePercent = changePct,
            spread = spreadDec.toPlainString(),
            digits = d,
            timestamp = datetime,
        )
    }

    private fun TickStatDto.toQuote(): Quote {
        val bidDec = bid.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val askDec = ask.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val d = bid.substringAfter(".", "").length.coerceAtLeast(2)
        val spreadDec = (askDec - bidDec).abs().setScale(d, RoundingMode.HALF_UP)
        val (changeAbs, changePct) = calcChange(symbol, bidDec, d)

        return Quote(
            symbol = symbol,
            displayName = symbol.mapToDisplayName(),
            category = "forex",
            bid = bid,
            ask = ask,
            high = high,
            low = low,
            change = changeAbs,
            changePercent = changePct,
            spread = spreadDec.toPlainString(),
            digits = d,
            timestamp = datetime,
        )
    }

    private fun QuoteDto.toQuote(): Quote {
        val bidDec = bid.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val askDec = ask.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val d = bid.substringAfter(".", "").length.coerceAtLeast(2)
        val spreadDec = (askDec - bidDec).abs().setScale(d, RoundingMode.HALF_UP)
        val (changeAbs, changePct) = calcChange(symbol, bidDec, d)

        return Quote(
            symbol = symbol,
            displayName = symbol.mapToDisplayName(),
            category = "forex",
            bid = bid,
            ask = ask,
            high = "0",
            low = "0",
            change = changeAbs,
            changePercent = changePct,
            spread = spreadDec.toPlainString(),
            digits = d,
            timestamp = datetime,
        )
    }

    // Calculate change from D1 open price
    private fun calcChange(symbol: String, currentBid: BigDecimal, digits: Int): Pair<String, String> {
        val open = dailyOpen[symbol]
        if (open == null || open.compareTo(BigDecimal.ZERO) == 0 || currentBid.compareTo(BigDecimal.ZERO) == 0) {
            return "0" to "0.00"
        }

        val diff = currentBid - open
        val pct = diff.divide(open, 4, RoundingMode.HALF_UP)
            .multiply(BD_100)
            .setScale(2, RoundingMode.HALF_UP)

        val absStr = diff.setScale(digits, RoundingMode.HALF_UP).toPlainString()
        val pctStr = (if (pct > BigDecimal.ZERO) "+" else "") + pct.toPlainString()

        return absStr to pctStr
    }

    companion object {
        private const val TAG = "RemoteMarketRepo"
        private const val POLL_INTERVAL_MS = 2000L  // 降级轮询间隔（仅 WS 断线时）
        private const val MAX_SYMBOLS = 50
        private const val BATCH_SIZE = 10
        private const val MAX_RETRY_ATTEMPTS = 5
        private const val RETRY_DELAY_MS = 3000L
        private val BD_100 = BigDecimal("100")

        private fun String.mapToDisplayName(): String {
            if (length == 6) {
                val base = substring(0, 3)
                val quote = substring(3, 6)
                if (base.all { it.isLetter() } && quote.all { it.isLetter() }) {
                    return "$base/$quote"
                }
            }
            return this
        }
    }
}
