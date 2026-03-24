package com.astralw.core.network.interceptor

import com.astralw.core.network.token.TokenProvider
import okhttp3.Interceptor
import okhttp3.Response

// OkHttp Interceptor: auto-inject Authorization Bearer token
class AuthInterceptor(
    private val tokenProvider: TokenProvider,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath

        if (path.contains("/auth/")) {
            return chain.proceed(request)
        }

        val token = tokenProvider.getAccessToken()
        if (token.isNullOrBlank()) {
            return chain.proceed(request)
        }

        val authedRequest = request.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(authedRequest)
    }
}
