package com.astralw.feature.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.astralw.core.data.model.AccountInfo
import com.astralw.core.data.model.Deal
import com.astralw.core.data.model.Order
import com.astralw.core.data.repository.AuthRepository
import com.astralw.core.data.repository.TradingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

/**
 * 持仓 ViewModel
 *
 * 定时轮询账户信息和持仓列表，保持 P&L 和 balance 实时更新。
 * 轮询仅在 ViewModel 存活期间运行（用户离开页面时自动停止）。
 */
@HiltViewModel
class PortfolioViewModel @Inject constructor(
    private val tradingRepository: TradingRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<PortfolioUiState>(PortfolioUiState.Loading)
    val uiState: StateFlow<PortfolioUiState> = _uiState.asStateFlow()

    private val _closeResult = MutableStateFlow<String?>(null)
    val closeResult: StateFlow<String?> = _closeResult.asStateFlow()

    init {
        loadData()
        startPolling()
    }

    /** 首次加载 / 手动重试 */
    fun loadData() {
        viewModelScope.launch {
            _uiState.value = PortfolioUiState.Loading
            refreshAll()
        }
    }

    /**
     * 定时刷新（5 秒间隔）
     */
    private fun startPolling() {
        viewModelScope.launch {
            while (true) {
                delay(POLL_INTERVAL_MS)
                refreshAll()
            }
        }
    }

    /**
     * 同时刷新账户信息 + 持仓列表，更新 UI 状态
     */
    private suspend fun refreshAll() {
        // 1. 获取账户信息
        val info = authRepository.getAccountInfo().getOrElse { error ->
            if (_uiState.value is PortfolioUiState.Loading) {
                _uiState.value = PortfolioUiState.Error(
                    error.message ?: "Failed to load account info",
                )
            }
            return
        }

        // 2. 刷新持仓（从后端拉最新数据到 StateFlow）
        tradingRepository.refreshPositions()

        // 3. 取最新快照
        val orders = tradingRepository.observeOpenOrders().first()

        // 4. 获取最近 30 天历史成交
        val now = System.currentTimeMillis() / 1000
        val thirtyDaysAgo = now - 30L * 24 * 3600
        val deals = tradingRepository.getHistoryDeals(thirtyDaysAgo, now)
            .getOrDefault(emptyList())

        // 5. 构建 UI 状态
        buildSuccessState(info, orders, deals)
    }

    private fun buildSuccessState(info: AccountInfo, orders: List<Order>, deals: List<Deal>) {
        if (orders.isEmpty()) {
            _uiState.value = PortfolioUiState.Success(
                openPositions = emptyList(),
                historyDeals = deals,
                balance = info.balance,
                equity = info.equity,
                freeMargin = info.freeMargin,
                marginLevel = if (info.marginLevel == "0") "∞" else "${info.marginLevel}%",
                totalPnL = "0.00",
                totalPnLIsPositive = true,
            )
        } else {
            var totalPnL = BigDecimal.ZERO

            orders.forEach { order ->
                totalPnL = totalPnL.add(order.floatingPnL.toBigDecimalOrNull() ?: BigDecimal.ZERO)
            }

            _uiState.value = PortfolioUiState.Success(
                openPositions = orders,
                historyDeals = deals,
                balance = info.balance,
                equity = info.equity,
                freeMargin = info.freeMargin,
                marginLevel = if (info.marginLevel == "0") "∞" else "${info.marginLevel}%",
                totalPnL = totalPnL.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                totalPnLIsPositive = totalPnL >= BigDecimal.ZERO,
            )
        }
    }

    /** 平仓后立即刷新 */
    fun closePosition(orderId: String) {
        viewModelScope.launch {
            val result = tradingRepository.closeOrder(orderId)
            result.onSuccess {
                _closeResult.value = "Position closed successfully"
            }.onFailure { e ->
                _closeResult.value = "Close failed: ${e.message}"
            }
            delay(CLOSE_REFRESH_DELAY_MS)
            refreshAll()
        }
    }

    fun clearCloseResult() {
        _closeResult.value = null
    }

    companion object {
        private const val POLL_INTERVAL_MS = 5_000L
        private const val CLOSE_REFRESH_DELAY_MS = 300L
    }
}
