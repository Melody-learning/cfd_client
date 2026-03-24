package com.astralw.app.navigation

import java.net.URLDecoder
import java.net.URLEncoder

/**
 * 导航路由定义
 */
object Routes {
    const val AUTH = "auth"
    const val MARKET = "market"
    const val PORTFOLIO = "portfolio"
    const val ACCOUNT = "account"
    const val CHART = "chart/{symbol}/{displayName}"
    const val TRADING = "trading/{symbol}/{displayName}"

    fun chart(symbol: String, displayName: String): String =
        "chart/$symbol/${URLEncoder.encode(displayName, "UTF-8")}"

    fun trading(symbol: String, displayName: String): String =
        "trading/$symbol/${URLEncoder.encode(displayName, "UTF-8")}"

    fun decodeDisplayName(encoded: String?): String =
        URLDecoder.decode(encoded ?: "", "UTF-8")
}
