package com.astralw.core.data.repository

import com.astralw.core.data.model.AccountInfo
import com.astralw.core.data.model.AuthUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mock 认证数据源
 *
 * 模拟登录/注册流程，用于前端开发
 */
@Singleton
class MockAuthRepository @Inject constructor() : AuthRepository {

    private val _currentUser = MutableStateFlow<AuthUser?>(null)

    override fun observeCurrentUser(): Flow<AuthUser?> = _currentUser.asStateFlow()

    override suspend fun login(email: String, password: String): Result<AuthUser> {
        // 模拟网络延迟
        delay(1200)

        // 模拟验证
        if (email.isBlank() || password.length < 6) {
            return Result.failure(IllegalArgumentException("Invalid credentials"))
        }

        val user = AuthUser(
            userId = "mock-user-001",
            email = email,
            displayName = email.substringBefore("@"),
            mt5Account = "5000001",
            accessToken = "mock-jwt-token-${System.currentTimeMillis()}",
            refreshToken = "mock-refresh-${System.currentTimeMillis()}",
        )
        _currentUser.value = user
        return Result.success(user)
    }

    override suspend fun register(
        email: String,
        password: String,
        displayName: String,
    ): Result<AuthUser> {
        delay(1500)

        if (email.isBlank() || password.length < 6 || displayName.isBlank()) {
            return Result.failure(IllegalArgumentException("Invalid registration data"))
        }

        val user = AuthUser(
            userId = "mock-user-${System.currentTimeMillis()}",
            email = email,
            displayName = displayName,
            mt5Account = "5000002",
            accessToken = "mock-jwt-token-${System.currentTimeMillis()}",
            refreshToken = "mock-refresh-${System.currentTimeMillis()}",
        )
        _currentUser.value = user
        return Result.success(user)
    }

    override suspend fun logout() {
        _currentUser.value = null
    }

    override suspend fun isLoggedIn(): Boolean = _currentUser.value != null

    override suspend fun getAccountInfo(): Result<AccountInfo> {
        delay(500)
        return Result.success(
            AccountInfo(
                login = 5000001,
                group = "demo\\demoUSD",
                balance = "10000.00",
                credit = "0",
                equity = "10000.00",
                margin = "0",
                freeMargin = "10000.00",
                marginLevel = "0",
                leverage = 100,
                currency = "USD",
                name = "Demo User",
            ),
        )
    }
}

