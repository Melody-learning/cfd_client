package com.astralw.feature.market

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.astralw.core.data.repository.MarketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 行情页 ViewModel
 *
 * 遵循 UDF: UI 观察 State → 发送 Event → ViewModel 处理 → 更新 State
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class MarketViewModel @Inject constructor(
    private val marketRepository: MarketRepository,
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow("all")

    /**
     * 行情 UI 状态
     *
     * 铁律: 行情 Flow 使用 .sample(500) 节流
     */
    val uiState = combine(
        marketRepository.observeQuotes().sample(500),
        _selectedCategory,
    ) { quotes, category ->
        val filtered = if (category == "all") {
            quotes
        } else {
            quotes.filter { it.category == category }
        }
        MarketUiState.Success(
            quotes = filtered,
            selectedCategory = category,
        ) as MarketUiState
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MarketUiState.Loading,
    )

    init {
        startStreaming()
    }

    /**
     * 切换品种分类
     */
    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    private fun startStreaming() {
        viewModelScope.launch {
            try {
                marketRepository.startStreaming()
            } catch (e: Exception) {
                // streaming 被取消是正常的
            }
        }
    }
}
