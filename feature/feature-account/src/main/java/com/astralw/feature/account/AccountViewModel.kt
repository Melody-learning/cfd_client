package com.astralw.feature.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.astralw.core.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 账户 ViewModel — 加载真实账户信息 + 登出
 */
@HiltViewModel
class AccountViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AccountUiState>(AccountUiState.Loading)
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    init {
        loadAccountInfo()
    }

    /** 从后端加载 MT5 账户信息 */
    fun loadAccountInfo() {
        viewModelScope.launch {
            _uiState.value = AccountUiState.Loading
            authRepository.getAccountInfo().fold(
                onSuccess = { info ->
                    _uiState.value = AccountUiState.Success(
                        login = info.login.toString(),
                        group = info.group,
                        balance = info.balance,
                        equity = info.equity,
                        margin = info.margin,
                        freeMargin = info.freeMargin,
                        marginLevel = info.marginLevel,
                        leverage = "1:${info.leverage}",
                        currency = info.currency,
                        name = info.name,
                    )
                },
                onFailure = { error ->
                    _uiState.value = AccountUiState.Error(
                        message = error.message ?: "Failed to load account info",
                    )
                },
            )
        }
    }

    /** 登出 */
    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onComplete()
        }
    }
}
