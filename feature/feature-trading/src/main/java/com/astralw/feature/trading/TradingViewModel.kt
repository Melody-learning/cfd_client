package com.astralw.feature.trading

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.astralw.core.data.model.OrderDirection
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
import javax.inject.Inject

/**
 * 下单 ViewModel
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

    fun placeOrder(direction: OrderDirection) {
        val state = _uiState.value
        if (state.isSubmitting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }

            val result = tradingRepository.openOrder(
                symbol = state.symbol,
                displayName = state.displayName,
                direction = direction,
                lots = state.lots,
                stopLoss = state.stopLoss.ifBlank { null },
                takeProfit = state.takeProfit.ifBlank { null },
            )

            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isSubmitting = false, orderPlaced = true) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = error.message ?: "Order failed",
                        )
                    }
                },
            )
        }
    }

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
