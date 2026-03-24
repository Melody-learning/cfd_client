package com.astralw.core.data.di

import com.astralw.core.data.repository.AuthRepository
import com.astralw.core.data.repository.ChartRepository
import com.astralw.core.data.repository.MarketRepository
import com.astralw.core.data.repository.RemoteAuthRepository
import com.astralw.core.data.repository.RemoteChartRepository
import com.astralw.core.data.repository.RemoteMarketRepository
import com.astralw.core.data.repository.RemoteTradingRepository
import com.astralw.core.data.repository.TradingRepository
import com.astralw.core.data.token.TokenManager
import com.astralw.core.network.api.AstralWApiService
import com.astralw.core.network.interceptor.AuthInterceptor
import com.astralw.core.network.interceptor.TokenAuthenticator
import com.astralw.core.network.token.TokenProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds @Singleton
    abstract fun bindMarketRepository(impl: RemoteMarketRepository): MarketRepository

    @Binds @Singleton
    abstract fun bindAuthRepository(impl: RemoteAuthRepository): AuthRepository

    @Binds @Singleton
    abstract fun bindChartRepository(impl: RemoteChartRepository): ChartRepository

    @Binds @Singleton
    abstract fun bindTradingRepository(impl: RemoteTradingRepository): TradingRepository

    @Binds @Singleton
    abstract fun bindTokenProvider(impl: TokenManager): TokenProvider

    companion object {
        /** 真机 WiFi 调试: 电脑局域网 IP */
        private const val BASE_URL = "http://10.0.0.44:8000/"

        @Provides
        @Singleton
        fun provideJson(): Json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            isLenient = true
        }

        @Provides
        @Singleton
        fun provideOkHttpClient(tokenProvider: TokenProvider, json: Json): OkHttpClient {
            val authInterceptor = AuthInterceptor(tokenProvider)
            val tokenAuthenticator = TokenAuthenticator(tokenProvider, BASE_URL, json)
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            return OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(logging)
                .authenticator(tokenAuthenticator)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
        }

        @Provides
        @Singleton
        fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit {
            val contentType = "application/json".toMediaType()
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(json.asConverterFactory(contentType))
                .build()
        }

        @Provides
        @Singleton
        fun provideApiService(retrofit: Retrofit): AstralWApiService {
            return retrofit.create(AstralWApiService::class.java)
        }
    }
}
