package com.astralw.core.network.model

import kotlinx.serialization.Serializable

/**
 * 通用 API 响应封装
 */
@Serializable
data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T? = null,
)
