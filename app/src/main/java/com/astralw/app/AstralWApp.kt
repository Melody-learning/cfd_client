package com.astralw.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.astralw.app.navigation.Routes
import com.astralw.feature.account.AccountScreen
import com.astralw.feature.auth.AuthScreen
import com.astralw.feature.chart.ChartScreen
import com.astralw.feature.market.MarketScreen
import com.astralw.feature.portfolio.PortfolioScreen
import com.astralw.feature.trading.TradingScreen

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
            modifier = Modifier.weight(1f),
        ) {
            composable(Routes.AUTH) {
                AuthScreen(
                    onLoginSuccess = {
                        navController.navigate(Routes.MARKET) {
                            popUpTo(Routes.AUTH) { inclusive = true }
                        }
                    },
                )
            }
            composable(Routes.MARKET) {
                MarketScreen(
                    onSymbolClick = { symbol, displayName ->
                        navController.navigate(Routes.chart(symbol, displayName))
                    },
                )
            }
            composable(Routes.PORTFOLIO) {
                PortfolioScreen()
            }
            composable(Routes.ACCOUNT) {
                AccountScreen(
                    onLogout = {
                        navController.navigate(Routes.AUTH) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                )
            }
            composable(route = Routes.CHART, arguments = symbolArgs) {
                ChartScreen(
                    onBack = { navController.popBackStack() },
                    onTrade = { symbol, displayName ->
                        navController.navigate(Routes.trading(symbol, displayName))
                    },
                )
            }
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
