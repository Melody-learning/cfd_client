package com.astralw.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

/**
 * MT5 风格底部 5-Tab — 行情 / 图表 / 交易 / 历史 / 更多
 */
val BOTTOM_TABS = listOf(
    BottomTab(Routes.QUOTES, "行情", Icons.AutoMirrored.Filled.FormatListBulleted, Icons.AutoMirrored.Outlined.FormatListBulleted),
    BottomTab(Routes.CHART_TAB, "图表", Icons.AutoMirrored.Filled.ShowChart, Icons.AutoMirrored.Outlined.ShowChart),
    BottomTab(Routes.TRADE, "交易", Icons.Filled.SwapVert, Icons.Outlined.SwapVert),
    BottomTab(Routes.HISTORY, "历史", Icons.Filled.History, Icons.Outlined.History),
    BottomTab(Routes.SETTINGS, "更多", Icons.Filled.MoreHoriz, Icons.Outlined.MoreHoriz),
)

/** 需要显示底部栏的路由集合 */
private val MAIN_ROUTES = setOf(
    Routes.QUOTES,
    Routes.CHART_TAB,
    Routes.TRADE,
    Routes.HISTORY,
    Routes.SETTINGS,
)

/**
 * 底部导航栏 — Linear Pro 风格
 *
 * 包含 navigationBarsPadding() 适配系统导航栏
 */
@Composable
fun BottomNavBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // 只在主页面显示底部栏
    if (currentRoute !in MAIN_ROUTES) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DesignTokens.SemanticColors.Surface)
            .navigationBarsPadding(),
    ) {
        // 顶部分割线
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(DesignTokens.SemanticColors.Border)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
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
                                popUpTo(Routes.QUOTES) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                        .padding(horizontal = DesignTokens.SpacingTokens.MD),
                ) {
                    Icon(
                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = tab.label,
                        tint = color,
                        modifier = Modifier.size(22.dp),
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
}
