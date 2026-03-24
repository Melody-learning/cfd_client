package com.astralw.core.data.repository

import com.astralw.core.data.model.AccountInfo
import com.astralw.core.data.model.AuthUser
import kotlinx.coroutines.flow.Flow

/**
 * 认证 Repository 接口
 */
interface AuthRepository {

    /** 当前登录用户状态（null 表示未登录） */
    fun observeCurrentUser(): Flow<AuthUser?>

    /** 登录 */
    suspend fun login(email: String, password: String): Result<AuthUser>

    /** 注册 */
    suspend fun register(email: String, password: String, displayName: String): Result<AuthUser>

    /** 登出 */
    suspend fun logout()

    /** 是否已登录 */
    suspend fun isLoggedIn(): Boolean

    /** 获取 MT5 账户信息 */
    suspend fun getAccountInfo(): Result<AccountInfo>
}
