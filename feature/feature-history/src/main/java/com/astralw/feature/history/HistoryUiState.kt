package com.astralw.feature.history

import com.astralw.core.data.model.Deal

/**
 * 历史成交页面 UI 状态
 */
sealed interface HistoryUiState {
    /** 加载中 */
    data object Loading : HistoryUiState

    /** 加载失败 */
    data class Error(val message: String) : HistoryUiState

    /** 加载成功 */
    data class Success(
        /** 历史成交记录 */
        val deals: List<Deal> = emptyList(),
        /** 当前选中的时间筛选索引 (0=今天, 1=7天, 2=30天, 3=全部) */
        val selectedFilterIndex: Int = 2,
    ) : HistoryUiState
}
