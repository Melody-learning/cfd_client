package com.astralw.core.network.api

import com.astralw.core.network.model.AccountInfoDto
import com.astralw.core.network.model.CandleResponseDto
import com.astralw.core.network.model.DealsResponseDto
import com.astralw.core.network.model.LoginRequestDto
import com.astralw.core.network.model.LoginResponseDto
import com.astralw.core.network.model.OrdersResponseDto
import com.astralw.core.network.model.PositionsResponseDto
import com.astralw.core.network.model.QuotesResponseDto
import com.astralw.core.network.model.RefreshRequestDto
import com.astralw.core.network.model.RefreshResponseDto
import com.astralw.core.network.model.RegisterRequestDto
import com.astralw.core.network.model.SymbolsResponseDto
import com.astralw.core.network.model.TickStatResponseDto
import com.astralw.core.network.model.TradeCloseRequestDto
import com.astralw.core.network.model.TradeModifyRequestDto
import com.astralw.core.network.model.TradeOpenRequestDto
import com.astralw.core.network.model.TradeResultDto
import com.astralw.core.network.model.MarginCheckResponseDto
import com.astralw.core.network.model.ProfitCalcResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

/**
 * AstralW Gateway REST API
 */
interface AstralWApiService {

    // ─── Auth ───

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequestDto): LoginResponseDto

    @POST("api/v1/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshRequestDto): RefreshResponseDto

    @POST("api/v1/auth/logout")
    suspend fun logout()

    // ─── Market ───

    @GET("api/v1/market/symbols")
    suspend fun getSymbols(): SymbolsResponseDto

    @GET("api/v1/market/quotes")
    suspend fun getQuotes(@Query("symbols") symbols: String): QuotesResponseDto

    @GET("api/v1/market/tick_stat")
    suspend fun getTickStat(@Query("symbols") symbols: String): TickStatResponseDto

    // ─── Chart ───

    @GET("api/v1/chart/candles")
    suspend fun getCandles(
        @Query("symbol") symbol: String,
        @Query("timeframe") timeframe: String = "M1",
        @Query("from") from: Long? = null,
        @Query("to") to: Long? = null,
        @Query("count") count: Int? = null,
    ): CandleResponseDto

    // ─── Trade ───

    @POST("api/v1/trade/open")
    suspend fun tradeOpen(@Body request: TradeOpenRequestDto): TradeResultDto

    @POST("api/v1/trade/close")
    suspend fun tradeClose(@Body request: TradeCloseRequestDto): TradeResultDto

    @PUT("api/v1/trade/modify")
    suspend fun tradeModify(@Body request: TradeModifyRequestDto): TradeResultDto

    @GET("api/v1/trade/check_margin")
    suspend fun checkMargin(
        @Query("symbol") symbol: String,
        @Query("direction") direction: String,
        @Query("lots") lots: String,
    ): MarginCheckResponseDto

    @GET("api/v1/trade/calc_profit")
    suspend fun calcProfit(
        @Query("symbol") symbol: String,
        @Query("direction") direction: String,
        @Query("lots") lots: String,
        @Query("price_open") priceOpen: String,
        @Query("price_close") priceClose: String,
    ): ProfitCalcResponseDto

    // ─── Account ───

    @GET("api/v1/account/info")
    suspend fun getAccountInfo(): AccountInfoDto

    // ─── Positions ───

    @GET("api/v1/positions")
    suspend fun getPositions(): PositionsResponseDto

    // ─── Pending Orders ───

    @GET("api/v1/orders")
    suspend fun getOrders(): OrdersResponseDto

    @retrofit2.http.DELETE("api/v1/orders/{ticket}")
    suspend fun cancelOrder(@retrofit2.http.Path("ticket") ticket: Long): TradeResultDto

    // ─── History ───

    @GET("api/v1/history/deals")
    suspend fun getHistoryDeals(
        @Query("from") from: Long,
        @Query("to") to: Long,
    ): DealsResponseDto
}
