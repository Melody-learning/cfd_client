package com.astralw.feature.settings

/**
 * 更多/设置页面 UI 状态
 */
sealed interface SettingsUiState {
    /** 加载中 */
    data object Loading : SettingsUiState

    /** 加载失败 */
    data class Error(val message: String) : SettingsUiState

    /** 加载成功 */
    data class Success(
        /** 账户名称 */
        val name: String = "",
        /** MT5 登录 ID */
        val login: String = "",
        /** 余额 (String, BigDecimal 传输) */
        val balance: String = "0.00",
        /** 权益 (String, BigDecimal 传输) */
        val equity: String = "0.00",
        /** 可用保证金 (String, BigDecimal 传输) */
        val freeMargin: String = "0.00",
    ) : SettingsUiState
}
