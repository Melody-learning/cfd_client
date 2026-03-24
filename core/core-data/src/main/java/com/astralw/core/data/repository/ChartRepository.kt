package com.astralw.core.data.repository

import com.astralw.core.data.model.Candle
import kotlinx.coroutines.flow.Flow

/**
 * K 线图数据 Repository 接口
 */
interface ChartRepository {

    /**
     * 获取历史 K 线数据
     *
     * @param symbol 品种代码
     * @param timeframe 时间周期 (e.g., "M1", "M5", "H1", "D1")
     * @param count 数量
     */
    suspend fun getCandles(symbol: String, timeframe: String, count: Int): List<Candle>

    /**
     * 观察实时 K 线更新
     */
    fun observeCandles(symbol: String, timeframe: String): Flow<List<Candle>>
}
