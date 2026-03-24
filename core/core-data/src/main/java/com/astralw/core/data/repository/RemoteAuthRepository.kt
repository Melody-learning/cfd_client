package com.astralw.core.data.repository

import com.astralw.core.data.model.AccountInfo
import com.astralw.core.data.model.AuthUser
import com.astralw.core.data.token.TokenManager
import com.astralw.core.network.api.AstralWApiService
import com.astralw.core.network.model.LoginRequestDto
import com.astralw.core.network.model.RegisterRequestDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 远程认证 Repository — 通过 Gateway API 登录/注册
 */
@Singleton
class RemoteAuthRepository @Inject constructor(
    private val api: AstralWApiService,
    private val tokenManager: TokenManager,
) : AuthRepository {

    private val currentUser = MutableStateFlow<AuthUser?>(null)

    override fun observeCurrentUser(): Flow<AuthUser?> = currentUser

    override suspend fun login(email: String, password: String): Result<AuthUser> {
        return try {
            val resp = api.login(LoginRequestDto(email, password))
            tokenManager.saveTokens(resp.accessToken, resp.refreshToken, resp.mt5Login)
            val user = AuthUser(
                userId = resp.mt5Login.toString(),
                email = email,
                displayName = email.substringBefore("@"),
                mt5Account = resp.mt5Login.toString(),
                accessToken = resp.accessToken,
                refreshToken = resp.refreshToken,
            )
            currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        displayName: String,
    ): Result<AuthUser> {
        return try {
            val resp = api.register(RegisterRequestDto(email, password, displayName))
            tokenManager.saveTokens(resp.accessToken, resp.refreshToken, resp.mt5Login)
            val user = AuthUser(
                userId = resp.mt5Login.toString(),
                email = email,
                displayName = displayName,
                mt5Account = resp.mt5Login.toString(),
                accessToken = resp.accessToken,
                refreshToken = resp.refreshToken,
            )
            currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        try {
            api.logout()
        } catch (_: Exception) {
            // 忽略网络错误，本地清除即可
        }
        tokenManager.clearTokens()
        currentUser.value = null
    }

    override suspend fun isLoggedIn(): Boolean = tokenManager.hasTokens()

    override suspend fun getAccountInfo(): Result<AccountInfo> {
        return try {
            val dto = api.getAccountInfo()
            Result.success(
                AccountInfo(
                    login = dto.login,
                    group = dto.group,
                    balance = dto.balance,
                    credit = dto.credit,
                    equity = dto.equity,
                    margin = dto.margin,
                    freeMargin = dto.freeMargin,
                    marginLevel = dto.marginLevel,
                    leverage = dto.leverage,
                    currency = dto.currency,
                    name = dto.name,
                ),
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

