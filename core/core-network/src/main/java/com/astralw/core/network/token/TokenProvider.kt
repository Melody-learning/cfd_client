package com.astralw.core.network.token

/**
 * Token 提供接口 — 供 AuthInterceptor / TokenAuthenticator 获取和刷新 Token
 *
 * 实现方在 core-data 的 TokenManager 中
 */
interface TokenProvider {
    fun getAccessToken(): String?
    fun getRefreshTokenSync(): String?
    fun updateAccessTokenSync(newToken: String)
}
