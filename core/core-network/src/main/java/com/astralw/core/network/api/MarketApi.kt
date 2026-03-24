package com.astralw.core.network.api

import com.astralw.core.network.model.ApiResponse
import com.astralw.core.network.model.SymbolInfoDto
import com.astralw.core.network.model.TickDto
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * 行情 API 接口
 */
interface MarketApi {

    /**
     * 获取所有可用品种列表
     */
    @GET("symbols")
    suspend fun getSymbols(): ApiResponse<List<SymbolInfoDto>>

    /**
     * 获取指定品种的最新报价
     */
    @GET("quotes/{symbol}")
    suspend fun getQuote(@Path("symbol") symbol: String): ApiResponse<TickDto>

    /**
     * 获取所有品种的最新报价
     */
    @GET("quotes")
    suspend fun getAllQuotes(): ApiResponse<List<TickDto>>
}
