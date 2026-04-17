package com.astralw.feature.trading

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.astralw.core.data.model.OrderDirection
import com.astralw.core.data.model.OrderType
import com.astralw.core.data.repository.AuthRepository
import com.astralw.core.data.repository.MarketRepository
import com.astralw.core.data.repository.TradingRepository
import com.astralw.domain.math.MarginCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

/**
 * 下单 ViewModel — 支持市价单 + 挂单
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class TradingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val tradingRepository: TradingRepository,
    private val marketRepository: MarketRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val symbol: String = savedStateHandle["symbol"] ?: "EURUSD"
    private val displayName: String = java.net.URLDecoder.decode(
        savedStateHandle["displayName"] ?: "EUR/USD", "UTF-8"
    )

    private val _uiState = MutableStateFlow(
        TradingUiState(symbol = symbol, displayName = displayName)
    )
    val uiState: StateFlow<TradingUiState> = _uiState.asStateFlow()

    init {
        observeRealTimePrice()
        loadAccountInfo()
    }

    private fun loadAccountInfo() {
        viewModelScope.launch {
            val result = authRepository.getAccountInfo()
            result.onSuccess { info ->
                _uiState.update {
                    it.copy(
                        balance = info.balance,
                        freeMargin = info.freeMargin,
                    )
                }
            }
        }
    }

    // ─── 用户输入事件 ───

    /** 切换订单类型 */
    fun onOrderTypeChanged(type: OrderType) {
        _uiState.update { state ->
            val newPrice = if (type != OrderType.MARKET && state.pendingPrice.isBlank()) {
                // 预填价格: BUY 类型用 Ask, SELL 类型用 Bid
                when (type) {
                    OrderType.BUY_LIMIT, OrderType.BUY_STOP -> state.currentAsk
                    OrderType.SELL_LIMIT, OrderType.SELL_STOP -> state.currentBid
                    else -> ""
                }
            } else if (type == OrderType.MARKET) {
                ""
            } else {
                state.pendingPrice
            }
            state.copy(
                orderType = type,
                pendingPrice = newPrice,
                errorMessage = null,
            )
        }
    }

    /** 修改挂单价格 */
    fun onPendingPriceChanged(price: String) {
        _uiState.update { it.copy(pendingPrice = price, errorMessage = null) }
    }

    /** 修改到期类型 */
    fun onExpirationChanged(type: ExpirationType) {
        _uiState.update { it.copy(expirationType = type, errorMessage = null) }
    }

    fun onLotsChanged(lots: String) {
        _uiState.update { state ->
            val margin = calculateMargin(lots, state.currentBid)
            state.copy(lots = lots, estimatedMargin = margin, errorMessage = null)
        }
    }

    fun onStopLossChanged(sl: String) {
        _uiState.update { it.copy(stopLoss = sl, errorMessage = null) }
    }

    fun onTakeProfitChanged(tp: String) {
        _uiState.update { it.copy(takeProfit = tp, errorMessage = null) }
    }

    /** 下单 (市价 / 挂单) */
    fun placeOrder(direction: OrderDirection) {
        val state = _uiState.value
        if (state.isSubmitting) return

        // 挂单价格验证
        if (state.isPendingOrder) {
            val validationError = validatePendingPrice(state, direction)
            if (validationError != null) {
                _uiState.update { it.copy(errorMessage = validationError) }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }

            val expiration = calculateExpiration(state.expirationType)

            val result = tradingRepository.openOrder(
                symbol = state.symbol,
                displayName = state.displayName,
                direction = direction,
                lots = state.lots,
                stopLoss = state.stopLoss.ifBlank { null },
                takeProfit = state.takeProfit.ifBlank { null },
                orderType = state.orderType,
                price = if (state.isPendingOrder) state.pendingPrice else null,
                expiration = expiration,
            )

            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isSubmitting = false, orderPlaced = true) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = error.message ?: "下单失败",
                        )
                    }
                },
            )
        }
    }

    // ─── 挂单价格验证 ───

    /**
     * 验证挂单价格逻辑
     * - BUY_LIMIT: 价格 < 当前 Ask
     * - SELL_LIMIT: 价格 > 当前 Bid
     * - BUY_STOP: 价格 > 当前 Ask
     * - SELL_STOP: 价格 < 当前 Bid
     */
    private fun validatePendingPrice(state: TradingUiState, direction: OrderDirection): String? {
        val priceStr = state.pendingPrice
        if (priceStr.isBlank()) return "请输入挂单价格"

        val price = try { BigDecimal(priceStr) } catch (_: Exception) { return "价格格式无效" }
        val bid = try { BigDecimal(state.currentBid) } catch (_: Exception) { return "等待行情..." }
        val ask = try { BigDecimal(state.currentAsk) } catch (_: Exception) { return "等待行情..." }

        return when (state.orderType) {
            OrderType.BUY_LIMIT -> if (price >= ask) "限价买入价格须低于当前 Ask ($ask)" else null
            OrderType.SELL_LIMIT -> if (price <= bid) "限价卖出价格须高于当前 Bid ($bid)" else null
            OrderType.BUY_STOP -> if (price <= ask) "止损买入价格须高于当前 Ask ($ask)" else null
            OrderType.SELL_STOP -> if (price >= bid) "止损卖出价格须低于当前 Bid ($bid)" else null
            OrderType.MARKET -> null
        }
    }

    /** 根据到期类型计算 Unix 秒 */
    private fun calculateExpiration(type: ExpirationType): Long? {
        val now = System.currentTimeMillis()
        val cal = java.util.Calendar.getInstance()
        return when (type) {
            ExpirationType.GTC -> null
            ExpirationType.TODAY -> {
                cal.set(java.util.Calendar.HOUR_OF_DAY, 23)
                cal.set(java.util.Calendar.MINUTE, 59)
                cal.set(java.util.Calendar.SECOND, 59)
                cal.timeInMillis / 1000
            }
            ExpirationType.THIS_WEEK -> {
                cal.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.FRIDAY)
                cal.set(java.util.Calendar.HOUR_OF_DAY, 23)
                cal.set(java.util.Calendar.MINUTE, 59)
                cal.set(java.util.Calendar.SECOND, 59)
                if (cal.timeInMillis <= now) cal.add(java.util.Calendar.WEEK_OF_YEAR, 1)
                cal.timeInMillis / 1000
            }
        }
    }

    // ─── 实时价格 ───

    private fun observeRealTimePrice() {
        viewModelScope.launch {
            marketRepository.observeQuotes().sample(500).collect { quotes ->
                val quote = quotes.find { it.symbol == symbol } ?: return@collect
                _uiState.update { state ->
                    val margin = calculateMargin(state.lots, quote.bid)
                    state.copy(
                        currentBid = quote.bid,
                        currentAsk = quote.ask,
                        isUp = quote.isUp,
                        estimatedMargin = margin,
                    )
                }
            }
        }
    }

    private fun calculateMargin(lots: String, price: String): String {
        if (lots.isBlank() || price.isBlank()) return "0.00"
        return try {
            val contractSize = getContractSize(symbol)
            MarginCalculator.requiredMargin(
                lots = lots,
                contractSize = contractSize,
                price = price,
                leverage = "100",
            )
        } catch (_: Exception) {
            "0.00"
        }
    }

    private fun getContractSize(symbol: String): String = when (symbol) {
        "XAUUSD" -> "100"
        "XAGUSD" -> "5000"
        "US30" -> "1"
        "BTCUSD", "BTCEUR", "ETHUSD" -> "1"
        else -> "100000"
    }
}
