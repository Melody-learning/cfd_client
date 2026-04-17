package com.astralw.app.navigation

import java.net.URLDecoder
import java.net.URLEncoder

/**
 * MT5 风格导航路由定义
 *
 * 5-Tab: 行情 / 图表 / 交易 / 历史 / 更多
 */
object Routes {
    // ─── 认证 ───
    const val AUTH = "auth"

    // ─── 5 大主 Tab ───
    const val QUOTES = "quotes"
    const val CHART_TAB = "chart_tab"
    const val TRADE = "trade"
    const val HISTORY = "history"
    const val SETTINGS = "settings"

    // ─── 二级页面 ───
    const val CHART = "chart/{symbol}/{displayName}"
    const val TRADING = "trading/{symbol}/{displayName}"

    /** 构建 K 线图路由 */
    fun chart(symbol: String, displayName: String): String =
        "chart/$symbol/${URLEncoder.encode(displayName, "UTF-8")}"

    /** 构建下单页路由 */
    fun trading(symbol: String, displayName: String): String =
        "trading/$symbol/${URLEncoder.encode(displayName, "UTF-8")}"

    /** 解码显示名称 */
    fun decodeDisplayName(encoded: String?): String =
        URLDecoder.decode(encoded ?: "", "UTF-8")
}
