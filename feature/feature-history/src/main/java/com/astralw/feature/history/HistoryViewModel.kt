package com.astralw.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.astralw.core.data.repository.TradingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 历史成交 ViewModel
 *
 * 从 TradingRepository 获取历史成交记录，支持按时间范围筛选。
 */
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val tradingRepository: TradingRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    /** 时间筛选选项 (毫秒) */
    private val filterDaysMs = longArrayOf(
        1L * 24 * 60 * 60 * 1000,   // 今天
        7L * 24 * 60 * 60 * 1000,   // 最近 7 天
        30L * 24 * 60 * 60 * 1000,  // 最近 30 天
        0L,                          // 全部 (0 表示无限制)
    )

    init {
        loadDeals(selectedFilterIndex = 2) // 默认 30 天
    }

    /**
     * 切换时间筛选
     */
    fun selectFilter(index: Int) {
        loadDeals(selectedFilterIndex = index)
    }

    /**
     * 重试加载
     */
    fun retry() {
        val currentIndex = (_uiState.value as? HistoryUiState.Success)?.selectedFilterIndex ?: 2
        loadDeals(selectedFilterIndex = currentIndex)
    }

    private fun loadDeals(selectedFilterIndex: Int) {
        viewModelScope.launch {
            _uiState.value = HistoryUiState.Loading

            val now = System.currentTimeMillis()
            val daysMs = filterDaysMs[selectedFilterIndex]
            val fromMs = if (daysMs == 0L) 0L else now - daysMs
            // API 使用秒级时间戳
            val fromSec = fromMs / 1000
            val toSec = now / 1000

            tradingRepository.getHistoryDeals(fromSec, toSec)
                .onSuccess { deals ->
                    _uiState.value = HistoryUiState.Success(
                        deals = deals,
                        selectedFilterIndex = selectedFilterIndex,
                    )
                }
                .onFailure { error ->
                    _uiState.value = HistoryUiState.Error(
                        message = error.message ?: "加载历史记录失败",
                    )
                }
        }
    }
}
