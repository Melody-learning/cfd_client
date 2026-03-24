package com.astralw.feature.auth

/**
 * 认证页 UI 状态
 *
 * 铁律: 必须处理 Loading / Success / Error 三态
 */
data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val isRegisterMode: Boolean = false,
    val errorMessage: String? = null,
)
