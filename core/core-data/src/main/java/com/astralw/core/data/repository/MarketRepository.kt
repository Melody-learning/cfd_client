package com.astralw.core.data.repository

import com.astralw.core.data.model.Quote
import kotlinx.coroutines.flow.Flow

/**
 * 行情 Repository 接口
 *
 * 遵循 Clean Architecture: Data Layer 暴露接口，具体实现可切换
 */
interface MarketRepository {

    /**
     * 获取实时行情流
     *
     * 铁律: 调用方必须对此 Flow 使用 .sample(500) 节流
     */
    fun observeQuotes(): Flow<List<Quote>>

    /**
     * 获取品种列表（一次性）
     */
    suspend fun getSymbols(): List<Quote>

    /**
     * 开始行情订阅
     */
    suspend fun startStreaming()

    /**
     * 停止行情订阅
     */
    suspend fun stopStreaming()
}
