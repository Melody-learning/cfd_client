package com.astralw.feature.account

/**
 * 账户页 UI 状态
 */
sealed interface AccountUiState {

    /** 加载中 */
    data object Loading : AccountUiState

    /** 加载成功 */
    data class Success(
        /** MT5 登录号 */
        val login: String,
        /** 账户组 (e.g., "demo\demoUSD") */
        val group: String,
        /** 余额 */
        val balance: String,
        /** 净值 */
        val equity: String,
        /** 已用保证金 */
        val margin: String,
        /** 可用保证金 */
        val freeMargin: String,
        /** 保证金水平 */
        val marginLevel: String,
        /** 杠杆 */
        val leverage: String,
        /** 货币 */
        val currency: String,
        /** 账户名 */
        val name: String,
    ) : AccountUiState

    /** 加载失败 */
    data class Error(val message: String) : AccountUiState
}
