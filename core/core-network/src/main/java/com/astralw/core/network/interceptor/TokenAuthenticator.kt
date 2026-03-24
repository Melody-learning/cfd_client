package com.astralw.core.network.interceptor

import android.util.Log
import com.astralw.core.network.model.RefreshRequestDto
import com.astralw.core.network.token.TokenProvider
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route

/**
 * OkHttp Authenticator：收到 401 时自动用 refresh_token 获取新 access_token
 *
 * 与 AuthInterceptor 配合使用：
 * - AuthInterceptor: 每次请求注入 access_token
 * - TokenAuthenticator: 401 时自动刷新 token 并重试
 */
class TokenAuthenticator(
    private val tokenProvider: TokenProvider,
    private val baseUrl: String,
    private val json: Json,
) : Authenticator {

    companion object {
        private const val TAG = "TokenAuthenticator"
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        // 防止无限重试
        if (response.request.header("X-Retry-Auth") != null) {
            Log.w(TAG, "Already retried, giving up")
            return null
        }

        val refreshToken = tokenProvider.getRefreshTokenSync() ?: run {
            Log.w(TAG, "No refresh token available")
            return null
        }

        // 同步调用 refresh 接口
        val refreshBody = json.encodeToString(
            RefreshRequestDto.serializer(),
            RefreshRequestDto(refreshToken = refreshToken),
        )

        val refreshRequest = Request.Builder()
            .url("${baseUrl}api/v1/auth/refresh")
            .post(refreshBody.toRequestBody("application/json".toMediaType()))
            .build()

        return try {
            // 使用独立的 OkHttpClient 避免拦截器循环
            val client = OkHttpClient.Builder().build()
            val refreshResponse = client.newCall(refreshRequest).execute()

            if (refreshResponse.isSuccessful) {
                val body = refreshResponse.body?.string() ?: return null
                val result = json.decodeFromString<Map<String, kotlinx.serialization.json.JsonElement>>(body)
                val newAccessToken = result["access_token"]?.toString()?.removeSurrounding("\"") ?: return null

                tokenProvider.updateAccessTokenSync(newAccessToken)
                Log.i(TAG, "Token refreshed successfully")

                // 用新 token 重试原请求
                response.request.newBuilder()
                    .header("Authorization", "Bearer $newAccessToken")
                    .header("X-Retry-Auth", "true")
                    .build()
            } else {
                Log.w(TAG, "Refresh failed: ${refreshResponse.code}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Refresh error", e)
            null
        }
    }
}
