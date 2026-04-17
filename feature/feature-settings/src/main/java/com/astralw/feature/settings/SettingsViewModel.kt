package com.astralw.feature.settings

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
 * 设置/更多页面 ViewModel
 *
 * 加载账户信息并提供登出功能。
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadAccountInfo()
    }

    /**
     * 加载账户信息
     */
    fun loadAccountInfo() {
        viewModelScope.launch {
            _uiState.value = SettingsUiState.Loading

            authRepository.getAccountInfo()
                .onSuccess { info ->
                    _uiState.value = SettingsUiState.Success(
                        name = info.name,
                        login = info.login.toString(),
                        balance = info.balance,
                        equity = info.equity,
                        freeMargin = info.freeMargin,
                    )
                }
                .onFailure { error ->
                    _uiState.value = SettingsUiState.Error(
                        message = error.message ?: "加载账户信息失败",
                    )
                }
        }
    }

    /**
     * 登出
     */
    fun logout(onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onLogoutComplete()
        }
    }
}
