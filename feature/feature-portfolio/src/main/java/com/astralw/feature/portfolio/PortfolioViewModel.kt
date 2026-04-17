package com.astralw.feature.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.astralw.core.data.model.AccountInfo
import com.astralw.core.data.model.Deal
import com.astralw.core.data.model.Order
import com.astralw.core.data.model.PendingOrder
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
 * 高频轮询账户信息+持仓列表（2 秒），低频轮询历史成交（30 秒）。
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

    /** 缓存历史成交，低频刷新 */
    private var cachedDeals: List<Deal> = emptyList()
    /** 缓存挂单列表 */
    private var cachedPendingOrders: List<PendingOrder> = emptyList()

    init {
        loadData()
        startHighFreqPolling()
        startLowFreqPolling()
    }

    /** 首次加载 / 手动重试 */
    fun loadData() {
        viewModelScope.launch {
            _uiState.value = PortfolioUiState.Loading
            refreshDeals()
            refreshAccountAndPositions()
        }
    }

    /**
     * 高频轮询：账户信息 + 持仓列表（2 秒间隔）
     * P&L 实时性的关键路径
     */
    private fun startHighFreqPolling() {
        viewModelScope.launch {
            while (true) {
                delay(HIGH_FREQ_POLL_MS)
                refreshAccountAndPositions()
            }
        }
    }

    /**
     * 低频轮询：历史成交（30 秒间隔）
     * 历史成交变化不频繁，无需高频拉取
     */
    private fun startLowFreqPolling() {
        viewModelScope.launch {
            while (true) {
                delay(LOW_FREQ_POLL_MS)
                refreshDeals()
            }
        }
    }

    /**
     * 刷新账户信息 + 持仓列表，使用缓存的历史成交构建 UI
     */
    private suspend fun refreshAccountAndPositions() {
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

        // 3.5 拉取挂单列表
        cachedPendingOrders = tradingRepository.getPendingOrders().getOrDefault(emptyList())

        // 4. 构建 UI 状态（使用缓存的历史成交）
        buildSuccessState(info, orders, cachedDeals)
    }

    /**
     * 刷新历史成交（低频）
     */
    private suspend fun refreshDeals() {
        try {
            val now = System.currentTimeMillis() / 1000
            val thirtyDaysAgo = now - 30L * 24 * 3600
            cachedDeals = tradingRepository.getHistoryDeals(thirtyDaysAgo, now)
                .getOrDefault(emptyList())
        } catch (_: Exception) {
            // 保持缓存数据
        }
    }

    private fun buildSuccessState(info: AccountInfo, orders: List<Order>, deals: List<Deal>) {
        if (orders.isEmpty()) {
            _uiState.value = PortfolioUiState.Success(
                openPositions = emptyList(),
                historyDeals = deals,
                pendingOrders = cachedPendingOrders,
                balance = info.balance,
                equity = info.equity,
                margin = info.margin,
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
                pendingOrders = cachedPendingOrders,
                balance = info.balance,
                equity = info.equity,
                margin = info.margin,
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
            refreshAccountAndPositions()
        }
    }

    fun clearCloseResult() {
        _closeResult.value = null
    }

    /** 取消挂单 */
    fun cancelPendingOrder(ticket: Long) {
        viewModelScope.launch {
            val result = tradingRepository.cancelPendingOrder(ticket)
            result.onSuccess {
                _closeResult.value = "挂单已取消"
            }.onFailure { e ->
                _closeResult.value = "取消失败: ${e.message}"
            }
            delay(CLOSE_REFRESH_DELAY_MS)
            refreshAccountAndPositions()
        }
    }

    /** 切换持仓/挂单 Tab */
    fun selectTab(index: Int) {
        val current = _uiState.value
        if (current is PortfolioUiState.Success) {
            _uiState.value = current.copy(selectedTab = index)
        }
    }

    companion object {
        /** 高频轮询：账户+持仓（2 秒） */
        private const val HIGH_FREQ_POLL_MS = 2_000L
        /** 低频轮询：历史成交（30 秒） */
        private const val LOW_FREQ_POLL_MS = 30_000L
        private const val CLOSE_REFRESH_DELAY_MS = 300L
    }
}

