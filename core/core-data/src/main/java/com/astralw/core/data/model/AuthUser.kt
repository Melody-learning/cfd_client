package com.astralw.core.data.model

/**
 * 用户认证信息
 */
data class AuthUser(
    val userId: String,
    val email: String,
    val displayName: String,
    val mt5Account: String,
    val accessToken: String,
    val refreshToken: String,
)
