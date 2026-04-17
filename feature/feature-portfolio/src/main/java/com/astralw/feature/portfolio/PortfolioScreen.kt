package com.astralw.feature.portfolio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.astralw.core.data.model.Deal
import com.astralw.core.data.model.Order
import com.astralw.core.data.model.OrderDirection
import com.astralw.core.ui.theme.DesignTokens
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 持仓管理页面 — 按原型图重构
 */
@Composable
fun PortfolioScreen(
    viewModel: PortfolioViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val closeResult by viewModel.closeResult.collectAsStateWithLifecycle()

    val context = androidx.compose.ui.platform.LocalContext.current
    androidx.compose.runtime.LaunchedEffect(closeResult) {
        closeResult?.let { msg ->
            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearCloseResult()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DesignTokens.SemanticColors.Background)
    ) {
        // ─── 居中标题 ───
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = DesignTokens.SpacingTokens.MD),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "持仓管理",
                color = DesignTokens.SemanticColors.TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        // 分割线
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(DesignTokens.SemanticColors.Border)
        )

        when (val state = uiState) {
            is PortfolioUiState.Loading -> {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = DesignTokens.SemanticColors.Accent)
                }
            }
            is PortfolioUiState.Error -> {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.message,
                            color = DesignTokens.SemanticColors.RiskDanger,
                            fontSize = 14.sp,
                        )
                        Spacer(Modifier.height(DesignTokens.SpacingTokens.MD))
                        Box(
                            modifier = Modifier
                                .background(
                                    DesignTokens.SemanticColors.SurfaceElevated,
                                    RoundedCornerShape(DesignTokens.RadiusTokens.MD),
                                )
                                .clickable { viewModel.loadData() }
                                .padding(
                                    horizontal = DesignTokens.SpacingTokens.LG,
                                    vertical = DesignTokens.SpacingTokens.SM,
                                ),
                        ) {
                            Text(
                                text = "重试",
                                color = DesignTokens.SemanticColors.Accent,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }
            is PortfolioUiState.Success -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    // ─── 账户摘要卡片 ───
                    item { AccountSummaryCard(state) }

                    item { Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.MD)) }

                    // ─── Tab 切换 ───
                    item {
                        val pendingLabel = if (state.pendingOrders.isNotEmpty()) {
                            "挂单 (${state.pendingOrders.size})"
                        } else {
                            "挂单"
                        }
                        TabRow(
                            tabs = listOf("当前持仓", pendingLabel, "历史记录"),
                            selectedIndex = state.selectedTab,
                            onTabSelected = { viewModel.selectTab(it) },
                        )

                        Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.SM))

                        when (state.selectedTab) {
                            0 -> PositionsContent(state, viewModel)
                            1 -> PendingOrdersContent(state, viewModel)
                            2 -> HistoryContent(state)
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// 账户摘要卡片 — 原型风格
// ═══════════════════════════════════════════════════════

@Composable
private fun AccountSummaryCard(state: PortfolioUiState.Success) {
    val pnlColor = if (state.totalPnLIsPositive) {
        DesignTokens.SemanticColors.PriceUp
    } else {
        DesignTokens.SemanticColors.PriceDown
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.SpacingTokens.LG)
            .padding(top = DesignTokens.SpacingTokens.MD)
            .background(
                DesignTokens.SemanticColors.SurfaceCard,
                RoundedCornerShape(DesignTokens.RadiusTokens.LG),
            )
            .border(
                DesignTokens.BorderTokens.Thin,
                DesignTokens.SemanticColors.Border,
                RoundedCornerShape(DesignTokens.RadiusTokens.LG),
            )
            .padding(DesignTokens.SpacingTokens.LG),
    ) {
        // 第一区：总盈亏 + 全部平仓按钮
        Text(
            text = "当前持仓总获利（浮动盈亏）",
            color = DesignTokens.SemanticColors.TextTertiary,
            fontSize = 12.sp,
        )
        Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.SM))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.Bottom,
            ) {
                val sign = if (state.totalPnLIsPositive) "+" else ""
                Text(
                    text = "$sign${state.totalPnL}",
                    color = pnlColor,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.width(DesignTokens.SpacingTokens.XS))
                Text(
                    text = "$",
                    color = DesignTokens.SemanticColors.TextTertiary,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }

            // 全部平仓按钮
            Box(
                modifier = Modifier
                    .background(
                        DesignTokens.SemanticColors.TextPrimary,
                        RoundedCornerShape(DesignTokens.RadiusTokens.MD),
                    )
                    .clickable { /* TODO: 全部平仓 */ }
                    .padding(
                        horizontal = DesignTokens.SpacingTokens.MD,
                        vertical = DesignTokens.SpacingTokens.SM,
                    ),
            ) {
                Text(
                    text = "全部平仓",
                    color = DesignTokens.SemanticColors.Background,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.XS))

        // 浮盈提示 + 余额
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            val pnlBd = state.totalPnL.toBigDecimalOrNull() ?: BigDecimal.ZERO
            val hintText = if (pnlBd >= BigDecimal.ZERO) "已浮盈" else "已浮亏"
            Text(
                text = "$hintText  请及时平仓",
                color = pnlColor,
                fontSize = 12.sp,
            )
            Text(
                text = "余额：${state.balance} $",
                color = DesignTokens.SemanticColors.TextTertiary,
                fontSize = 12.sp,
            )
        }

        Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.LG))

        // 分割线
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(DesignTokens.SemanticColors.Border)
        )

        Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.LG))

        // 第二区：占用保证金 + 保证金比例
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = "占用保证金",
                    color = DesignTokens.SemanticColors.TextTertiary,
                    fontSize = 11.sp,
                )
                Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.XXS))
                Text(
                    text = "$${state.margin}",
                    color = DesignTokens.SemanticColors.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "保证金比例",
                    color = DesignTokens.SemanticColors.TextTertiary,
                    fontSize = 11.sp,
                )
                Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.XXS))
                Text(
                    text = state.marginLevel,
                    color = DesignTokens.SemanticColors.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.SM))

        // 保证金进度条
        val marginBd = state.margin.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val equityBd = state.equity.toBigDecimalOrNull() ?: BigDecimal.ONE
        val marginRatio = if (equityBd > BigDecimal.ZERO) {
            marginBd.divide(equityBd, 4, java.math.RoundingMode.HALF_UP)
                .toFloat().coerceIn(0f, 1f)
        } else {
            0f
        }

        LinearProgressIndicator(
            progress = { marginRatio },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = DesignTokens.SemanticColors.TextPrimary,
            trackColor = DesignTokens.SemanticColors.SurfaceElevated,
        )

        Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.SM))

        // 强制平仓水位 + 风险度
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row {
                Text(
                    text = "强制平仓水位",
                    color = DesignTokens.SemanticColors.TextTertiary,
                    fontSize = 11.sp,
                )
            }

            // 风险度指示
            Row(verticalAlignment = Alignment.CenterVertically) {
                val riskColor = when {
                    marginRatio < 0.3f -> DesignTokens.SemanticColors.PriceUp
                    marginRatio < 0.7f -> DesignTokens.SemanticColors.Accent
                    else -> DesignTokens.SemanticColors.PriceDown
                }
                val riskText = when {
                    marginRatio < 0.3f -> "健康"
                    marginRatio < 0.7f -> "一般"
                    else -> "危险"
                }

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(riskColor),
                )
                Spacer(modifier = Modifier.width(DesignTokens.SpacingTokens.XS))
                Text(
                    text = "风险度：$riskText",
                    color = DesignTokens.SemanticColors.TextSecondary,
                    fontSize = 11.sp,
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// Tab 栏
// ═══════════════════════════════════════════════════════

@Composable
private fun TabRow(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.SpacingTokens.LG),
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.SpacingTokens.SM),
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (isSelected) DesignTokens.SemanticColors.SurfaceElevated
                        else DesignTokens.SemanticColors.SurfaceCard,
                        RoundedCornerShape(DesignTokens.RadiusTokens.MD),
                    )
                    .border(
                        DesignTokens.BorderTokens.Thin,
                        if (isSelected) DesignTokens.SemanticColors.Accent
                        else DesignTokens.SemanticColors.Border,
                        RoundedCornerShape(DesignTokens.RadiusTokens.MD),
                    )
                    .clickable { onTabSelected(index) }
                    .padding(vertical = DesignTokens.SpacingTokens.SM),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = title,
                    color = if (isSelected) DesignTokens.SemanticColors.Accent
                    else DesignTokens.SemanticColors.TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// 当前持仓内容
// ═══════════════════════════════════════════════════════

@Composable
private fun PositionsContent(state: PortfolioUiState.Success, viewModel: PortfolioViewModel) {
    if (state.openPositions.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.SpacingTokens.XXXL),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "暂无持仓",
                color = DesignTokens.SemanticColors.TextTertiary,
                fontSize = 14.sp,
            )
        }
    } else {
        Column(modifier = Modifier.padding(horizontal = DesignTokens.SpacingTokens.LG)) {
            state.openPositions.forEach { order ->
                PositionItem(
                    order = order,
                    onClose = { viewModel.closePosition(order.orderId) },
                )
                Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.SM))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// 历史记录内容
// ═══════════════════════════════════════════════════════

@Composable
private fun HistoryContent(state: PortfolioUiState.Success) {
    if (state.historyDeals.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.SpacingTokens.XXXL),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "暂无历史记录",
                color = DesignTokens.SemanticColors.TextTertiary,
                fontSize = 14.sp,
            )
        }
    } else {
        Column(modifier = Modifier.padding(horizontal = DesignTokens.SpacingTokens.LG)) {
            state.historyDeals.forEach { deal ->
                DealItem(deal)
                Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.SM))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// 持仓项 — 品种图标 + 信息 + 盈亏
// ═══════════════════════════════════════════════════════

@Composable
private fun PositionItem(order: Order, onClose: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var showCloseDialog by remember { mutableStateOf(false) }

    val dirColor = if (order.direction == OrderDirection.BUY) {
        DesignTokens.SemanticColors.PriceUp
    } else {
        DesignTokens.SemanticColors.PriceDown
    }

    val pnlDecimal = order.floatingPnL.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val pnlColor = if (pnlDecimal >= BigDecimal.ZERO) {
        DesignTokens.SemanticColors.PriceUp
    } else {
        DesignTokens.SemanticColors.PriceDown
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                DesignTokens.SemanticColors.SurfaceCard,
                RoundedCornerShape(DesignTokens.RadiusTokens.MD),
            )
            .border(
                DesignTokens.BorderTokens.Thin,
                DesignTokens.SemanticColors.Border,
                RoundedCornerShape(DesignTokens.RadiusTokens.MD),
            )
            .clickable { expanded = !expanded }
            .padding(DesignTokens.SpacingTokens.MD),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 品种图标
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(DesignTokens.SemanticColors.SurfaceElevated),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = order.displayName.take(1).uppercase(),
                color = DesignTokens.SemanticColors.Accent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.width(DesignTokens.SpacingTokens.SM))

        // 品种信息
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = order.displayName,
                color = DesignTokens.SemanticColors.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Row {
                val dirText = if (order.direction == OrderDirection.BUY) "买入" else "卖出"
                Text(
                    text = dirText,
                    color = dirColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = " ${order.lots}手",
                    color = DesignTokens.SemanticColors.TextTertiary,
                    fontSize = 12.sp,
                )
            }
        }

        // 盈亏
        Column(horizontalAlignment = Alignment.End) {
            val sign = if (pnlDecimal >= BigDecimal.ZERO) "+ " else ""
            Text(
                text = "$sign$${order.floatingPnL}",
                color = pnlColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }

    // 展开详情
    if (expanded) {
        val dateStr = remember(order.openTime) {
            SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US).format(Date(order.openTime))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DesignTokens.SpacingTokens.LG)
                .background(
                    DesignTokens.SemanticColors.SurfaceCard,
                    RoundedCornerShape(
                        bottomStart = DesignTokens.RadiusTokens.MD,
                        bottomEnd = DesignTokens.RadiusTokens.MD,
                    ),
                )
                .padding(DesignTokens.SpacingTokens.MD),
        ) {
            DetailRow("开仓价", order.openPrice)
            DetailRow("现价", order.currentPrice)
            order.stopLoss?.let { DetailRow("止损", it) }
            order.takeProfit?.let { DetailRow("止盈", it) }
            DetailRow("开仓时间", dateStr)

            Spacer(Modifier.height(DesignTokens.SpacingTokens.MD))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        DesignTokens.SemanticColors.PriceDown,
                        RoundedCornerShape(DesignTokens.RadiusTokens.MD),
                    )
                    .clickable { showCloseDialog = true }
                    .padding(vertical = DesignTokens.SpacingTokens.SM),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "平仓",
                    color = DesignTokens.Palette.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }

    // 平仓确认弹窗
    if (showCloseDialog) {
        AlertDialog(
            onDismissRequest = { showCloseDialog = false },
            containerColor = DesignTokens.SemanticColors.SurfaceCard,
            titleContentColor = DesignTokens.SemanticColors.TextPrimary,
            textContentColor = DesignTokens.SemanticColors.TextSecondary,
            title = { Text("平仓确认") },
            text = {
                val dirText = if (order.direction == OrderDirection.BUY) "买入" else "卖出"
                Column {
                    Text("${order.displayName}  $dirText  ${order.lots}手")
                    Spacer(Modifier.height(DesignTokens.SpacingTokens.SM))
                    Text("开仓价: ${order.openPrice}")
                    Text("浮动盈亏: $${order.floatingPnL}")
                    Spacer(Modifier.height(DesignTokens.SpacingTokens.SM))
                    Text("确认要平仓吗？")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCloseDialog = false
                        onClose()
                    },
                ) {
                    Text(
                        text = "确认",
                        color = DesignTokens.SemanticColors.PriceDown,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showCloseDialog = false }) {
                    Text(
                        text = "取消",
                        color = DesignTokens.SemanticColors.TextSecondary,
                    )
                }
            },
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            color = DesignTokens.SemanticColors.TextTertiary,
            fontSize = 12.sp,
        )
        Text(
            text = value,
            color = DesignTokens.SemanticColors.TextPrimary,
            fontSize = 12.sp,
        )
    }
}

// ═══════════════════════════════════════════════════════
// 历史成交项
// ═══════════════════════════════════════════════════════

@Composable
private fun DealItem(deal: Deal) {
    val dirColor = if (deal.direction == "BUY") {
        DesignTokens.SemanticColors.PriceUp
    } else {
        DesignTokens.SemanticColors.PriceDown
    }
    val profitBd = deal.profit.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val profitColor = if (profitBd >= BigDecimal.ZERO) {
        DesignTokens.SemanticColors.PriceUp
    } else {
        DesignTokens.SemanticColors.PriceDown
    }
    val dateStr = remember(deal.timeMs) {
        SimpleDateFormat("MM/dd HH:mm", Locale.US).format(Date(deal.timeMs))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                DesignTokens.SemanticColors.SurfaceCard,
                RoundedCornerShape(DesignTokens.RadiusTokens.MD),
            )
            .border(
                DesignTokens.BorderTokens.Thin,
                DesignTokens.SemanticColors.Border,
                RoundedCornerShape(DesignTokens.RadiusTokens.MD),
            )
            .padding(DesignTokens.SpacingTokens.MD),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 图标
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(DesignTokens.SemanticColors.SurfaceElevated),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = deal.displayName.take(1).uppercase(),
                color = DesignTokens.SemanticColors.Accent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.width(DesignTokens.SpacingTokens.SM))

        Column(Modifier.weight(1f)) {
            Text(
                text = deal.displayName,
                color = DesignTokens.SemanticColors.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Row {
                val dirText = if (deal.direction == "BUY") "买入" else "卖出"
                Text(
                    text = dirText,
                    color = dirColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = " ${deal.lots}手 @ ${deal.price}",
                    color = DesignTokens.SemanticColors.TextTertiary,
                    fontSize = 12.sp,
                )
            }
            Text(
                text = dateStr,
                color = DesignTokens.SemanticColors.TextTertiary,
                fontSize = 11.sp,
            )
        }
        Text(
            text = "$${deal.profit}",
            color = profitColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
        )
    }
}

// ═══════════════════════════════════════════════════════
// 挂单列表
// ═══════════════════════════════════════════════════════

@Composable
private fun PendingOrdersContent(
    state: PortfolioUiState.Success,
    viewModel: PortfolioViewModel,
) {
    var cancelTicket by remember { mutableStateOf<Long?>(null) }

    if (state.pendingOrders.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = DesignTokens.SpacingTokens.XXL),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "暂无挂单",
                color = DesignTokens.SemanticColors.TextTertiary,
                fontSize = 14.sp,
            )
        }
    } else {
        Column(
            modifier = Modifier.padding(horizontal = DesignTokens.SpacingTokens.LG),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.SpacingTokens.SM),
        ) {
            state.pendingOrders.forEach { order ->
                PendingOrderItem(
                    order = order,
                    onCancel = { cancelTicket = order.ticket },
                )
            }
        }
    }

    // 取消确认弹窗
    cancelTicket?.let { ticket ->
        AlertDialog(
            onDismissRequest = { cancelTicket = null },
            title = {
                Text(
                    text = "取消挂单",
                    color = DesignTokens.SemanticColors.TextPrimary,
                )
            },
            text = {
                Text(
                    text = "确定取消此挂单？",
                    color = DesignTokens.SemanticColors.TextSecondary,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.cancelPendingOrder(ticket)
                    cancelTicket = null
                }) {
                    Text("确认", color = DesignTokens.SemanticColors.RiskDanger)
                }
            },
            dismissButton = {
                TextButton(onClick = { cancelTicket = null }) {
                    Text("取消", color = DesignTokens.SemanticColors.TextSecondary)
                }
            },
            containerColor = DesignTokens.SemanticColors.SurfaceElevated,
        )
    }
}

@Composable
private fun PendingOrderItem(
    order: com.astralw.core.data.model.PendingOrder,
    onCancel: () -> Unit,
) {
    val typeColor = when {
        order.type.apiValue.contains("BUY") -> DesignTokens.SemanticColors.PriceUp
        else -> DesignTokens.SemanticColors.PriceDown
    }

    val dateStr = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
        .format(Date(order.setupTime))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                DesignTokens.SemanticColors.SurfaceCard,
                RoundedCornerShape(DesignTokens.RadiusTokens.MD),
            )
            .border(
                DesignTokens.BorderTokens.Thin,
                DesignTokens.SemanticColors.Border,
                RoundedCornerShape(DesignTokens.RadiusTokens.MD),
            )
            .padding(DesignTokens.SpacingTokens.MD),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 左侧: 品种 + 类型
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = order.displayName,
                    color = DesignTokens.SemanticColors.TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.width(DesignTokens.SpacingTokens.SM))
                Text(
                    text = order.type.label,
                    color = typeColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
            Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.XXS))
            Text(
                text = "${order.lots} 手 @ ${order.triggerPrice}",
                color = DesignTokens.SemanticColors.TextSecondary,
                fontSize = 12.sp,
            )
            Text(
                text = dateStr,
                color = DesignTokens.SemanticColors.TextTertiary,
                fontSize = 11.sp,
            )
        }

        // 右侧: 当前价 + 取消按钮
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = order.currentPrice,
                color = DesignTokens.SemanticColors.TextPrimary,
                fontSize = 13.sp,
            )
            Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.XS))
            Box(
                modifier = Modifier
                    .background(
                        DesignTokens.SemanticColors.RiskDanger.copy(alpha = 0.15f),
                        RoundedCornerShape(DesignTokens.RadiusTokens.SM),
                    )
                    .clickable { onCancel() }
                    .padding(
                        horizontal = DesignTokens.SpacingTokens.MD,
                        vertical = DesignTokens.SpacingTokens.XS,
                    ),
            ) {
                Text(
                    text = "取消",
                    color = DesignTokens.SemanticColors.RiskDanger,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}
