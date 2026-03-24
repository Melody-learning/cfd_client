package com.astralw.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.astralw.app.navigation.Routes
import com.astralw.core.ui.theme.DesignTokens

/**
 * 底部 Tab 项
 */
data class BottomTab(
    val route: String,
    val label: String,
    val icon: String,
)

val BOTTOM_TABS = listOf(
    BottomTab(Routes.MARKET, "Markets", "📊"),
    BottomTab(Routes.PORTFOLIO, "Portfolio", "💼"),
    BottomTab(Routes.ACCOUNT, "Account", "👤"),
)

/**
 * 底部导航栏 — Linear Pro 风格
 */
@Composable
fun BottomNavBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // 只在主页面显示底部栏
    val showBottomBar = currentRoute in listOf(Routes.MARKET, Routes.PORTFOLIO, Routes.ACCOUNT)
    if (!showBottomBar) return

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(DesignTokens.SemanticColors.Border)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DesignTokens.SemanticColors.Surface)
            .padding(vertical = DesignTokens.SpacingTokens.SM),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        BOTTOM_TABS.forEach { tab ->
            val selected = currentRoute == tab.route
            val color = if (selected) {
                DesignTokens.SemanticColors.Accent
            } else {
                DesignTokens.SemanticColors.TextTertiary
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable {
                        navController.navigate(tab.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                    .padding(horizontal = DesignTokens.SpacingTokens.LG),
            ) {
                Text(
                    text = tab.icon,
                    fontSize = 20.sp,
                )
                Text(
                    text = tab.label,
                    color = color,
                    fontSize = 10.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
        }
    }
}
