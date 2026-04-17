package com.astralw.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.astralw.app.navigation.Routes
import com.astralw.feature.auth.AuthScreen
import com.astralw.feature.chart.ChartScreen
import com.astralw.feature.history.HistoryScreen
import com.astralw.feature.market.MarketScreen
import com.astralw.feature.portfolio.PortfolioScreen
import com.astralw.feature.settings.SettingsScreen
import com.astralw.feature.trading.TradingScreen
import java.net.URLEncoder

/**
 * AstralW 顶层 App Composable — MT5 风格
 *
 * 5-Tab: 行情 / 图表 / 交易 / 历史 / 更多
 */
@Composable
fun AstralWApp() {
    val navController = rememberNavController()

    val symbolArgs = listOf(
        navArgument("symbol") { type = NavType.StringType },
        navArgument("displayName") { type = NavType.StringType },
    )

    Column(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Routes.AUTH,
            modifier = Modifier
                .weight(1f)
                .statusBarsPadding(),
        ) {
            // ─── 认证 ───
            composable(Routes.AUTH) {
                AuthScreen(
                    onLoginSuccess = {
                        navController.navigate(Routes.QUOTES) {
                            popUpTo(Routes.AUTH) { inclusive = true }
                        }
                    },
                )
            }

            // ─── Tab 1: 行情 ───
            composable(Routes.QUOTES) {
                MarketScreen(
                    onSymbolClick = { symbol, displayName ->
                        navController.navigate(Routes.chart(symbol, displayName))
                    },
                )
            }

            // ─── Tab 2: 图表 (独立 Tab, 默认 EURUSD) ───
            composable(Routes.CHART_TAB) {
                val defaultSymbol = "EURUSD"
                val defaultName = URLEncoder.encode("EUR/USD", "UTF-8")
                // 导航到 chart 页面，显示默认品种
                ChartScreen(
                    onBack = {
                        navController.navigate(Routes.QUOTES) {
                            popUpTo(Routes.QUOTES) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onTrade = { symbol, displayName ->
                        navController.navigate(Routes.trading(symbol, displayName))
                    },
                )
            }

            // ─── Tab 3: 交易 (持仓列表) ───
            composable(Routes.TRADE) {
                PortfolioScreen()
            }

            // ─── Tab 4: 历史 ───
            composable(Routes.HISTORY) {
                HistoryScreen()
            }

            // ─── Tab 5: 更多 ───
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onLogout = {
                        navController.navigate(Routes.AUTH) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                )
            }

            // ─── 二级页: K 线图 (从行情点击进入) ───
            composable(route = Routes.CHART, arguments = symbolArgs) {
                ChartScreen(
                    onBack = { navController.popBackStack() },
                    onTrade = { symbol, displayName ->
                        navController.navigate(Routes.trading(symbol, displayName))
                    },
                )
            }

            // ─── 二级页: 下单 ───
            composable(route = Routes.TRADING, arguments = symbolArgs) {
                TradingScreen(
                    onBack = { navController.popBackStack() },
                    onOrderPlaced = { navController.popBackStack() },
                )
            }
        }

        BottomNavBar(navController = navController)
    }
}
