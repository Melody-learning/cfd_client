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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PortfolioScreen(
    viewModel: PortfolioViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val closeResult by viewModel.closeResult.collectAsStateWithLifecycle()

    // 平仓结果提示（用 Toast）
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
        // 标题
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = DesignTokens.SpacingTokens.LG,
                    vertical = DesignTokens.SpacingTokens.MD,
                )
        ) {
            Text(
                text = "Portfolio",
                style = MaterialTheme.typography.headlineMedium,
                color = DesignTokens.SemanticColors.TextPrimary,
                fontWeight = FontWeight.Bold,
            )
        }

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
                                text = "Retry",
                                color = DesignTokens.SemanticColors.Accent,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }
            is PortfolioUiState.Success -> {
                // 账户摘要
                AccountSummaryCard(state)

                Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.MD))

                // Tab 切换: Positions / History
                var selectedTab by remember { mutableIntStateOf(0) }
                TabRow(
                    tabs = listOf(
                        "Positions (${state.openPositions.size})",
                        "History (${state.historyDeals.size})",
                    ),
                    selectedIndex = selectedTab,
                    onTabSelected = { selectedTab = it },
                )

                Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.SM))

                when (selectedTab) {
                    0 -> PositionsTab(state, viewModel)
                    1 -> HistoryTab(state)
                }
            }
        }
    }
}

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

@Composable
private fun PositionsTab(state: PortfolioUiState.Success, viewModel: PortfolioViewModel) {
    if (state.openPositions.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.SpacingTokens.XXXL),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "No open positions",
                color = DesignTokens.SemanticColors.TextTertiary,
                fontSize = 14.sp,
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.padding(horizontal = DesignTokens.SpacingTokens.LG),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.SpacingTokens.SM),
        ) {
            items(state.openPositions, key = { it.orderId }) { order ->
                PositionItem(
                    order = order,
                    onClose = { viewModel.closePosition(order.orderId) },
                )
            }
        }
    }
}

@Composable
private fun HistoryTab(state: PortfolioUiState.Success) {
    if (state.historyDeals.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.SpacingTokens.XXXL),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "No history",
                color = DesignTokens.SemanticColors.TextTertiary,
                fontSize = 14.sp,
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.padding(horizontal = DesignTokens.SpacingTokens.LG),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.SpacingTokens.SM),
        ) {
            items(state.historyDeals, key = { it.dealId }) { deal ->
                DealItem(deal)
            }
        }
    }
}

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
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            SummaryItem("Balance", "$${state.balance}")
            SummaryItem("Equity", "$${state.equity}")
        }
        Spacer(Modifier.height(DesignTokens.SpacingTokens.MD))
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            SummaryItem("Free Margin", "$${state.freeMargin}")
            SummaryItem("Margin Level", state.marginLevel)
        }
        Spacer(Modifier.height(DesignTokens.SpacingTokens.MD))
        Row(Modifier.fillMaxWidth(), Arrangement.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "P&L",
                    color = DesignTokens.SemanticColors.TextTertiary,
                    fontSize = 11.sp,
                )
                Text(
                    text = "$${state.totalPnL}",
                    color = pnlColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            color = DesignTokens.SemanticColors.TextTertiary,
            fontSize = 11.sp,
        )
        Text(
            text = value,
            color = DesignTokens.SemanticColors.TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun PositionItem(order: Order, onClose: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var showCloseDialog by remember { mutableStateOf(false) }

    val dirColor = if (order.direction == OrderDirection.BUY) {
        DesignTokens.SemanticColors.PriceUp
    } else {
        DesignTokens.SemanticColors.PriceDown
    }

    val pnlDecimal = order.floatingPnL.toBigDecimalOrNull() ?: java.math.BigDecimal.ZERO
    val pnlColor = if (pnlDecimal >= java.math.BigDecimal.ZERO) {
        DesignTokens.SemanticColors.PriceUp
    } else {
        DesignTokens.SemanticColors.PriceDown
    }

    val dateStr = remember(order.openTime) {
        SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US).format(Date(order.openTime))
    }

    Column(
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
    ) {
        // 第一行：品种 + P&L
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = order.displayName,
                    color = DesignTokens.SemanticColors.TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Row {
                    Text(
                        text = order.direction.name,
                        color = dirColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = " ${order.lots} lots",
                        color = DesignTokens.SemanticColors.TextTertiary,
                        fontSize = 12.sp,
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${order.floatingPnL}",
                    color = pnlColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = if (expanded) "▲" else "▼",
                    color = DesignTokens.SemanticColors.TextTertiary,
                    fontSize = 10.sp,
                )
            }
        }

        // 展开详情 + 平仓按钮
        if (expanded) {
            Spacer(Modifier.height(DesignTokens.SpacingTokens.MD))

            // 详情行
            DetailRow("Open Price", order.openPrice)
            DetailRow("Current Price", order.currentPrice)
            order.stopLoss?.let { DetailRow("Stop Loss", it) }
            order.takeProfit?.let { DetailRow("Take Profit", it) }
            DetailRow("Open Time", dateStr)

            Spacer(Modifier.height(DesignTokens.SpacingTokens.MD))

            // 平仓按钮
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
                    text = "Close Position",
                    color = DesignTokens.Palette.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }

    // 平仓确认弹窗
    if (showCloseDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showCloseDialog = false },
            containerColor = DesignTokens.SemanticColors.SurfaceCard,
            titleContentColor = DesignTokens.SemanticColors.TextPrimary,
            textContentColor = DesignTokens.SemanticColors.TextSecondary,
            title = {
                Text("Close Position")
            },
            text = {
                Column {
                    Text("${order.displayName}  ${order.direction.name}  ${order.lots} lots")
                    Spacer(Modifier.height(DesignTokens.SpacingTokens.SM))
                    Text("Open Price: ${order.openPrice}")
                    Text("Current P&L: $${order.floatingPnL}")
                    Spacer(Modifier.height(DesignTokens.SpacingTokens.SM))
                    Text("Are you sure you want to close this position?")
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        showCloseDialog = false
                        onClose()
                    },
                ) {
                    Text(
                        text = "Confirm",
                        color = DesignTokens.SemanticColors.PriceDown,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { showCloseDialog = false },
                ) {
                    Text(
                        text = "Cancel",
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

@Composable
private fun DealItem(deal: Deal) {
    val dirColor = if (deal.direction == "BUY") {
        DesignTokens.SemanticColors.PriceUp
    } else {
        DesignTokens.SemanticColors.PriceDown
    }
    val profitBd = deal.profit.toBigDecimalOrNull() ?: java.math.BigDecimal.ZERO
    val profitColor = if (profitBd >= java.math.BigDecimal.ZERO) {
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
        Column(Modifier.weight(1f)) {
            Text(
                text = deal.displayName,
                color = DesignTokens.SemanticColors.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Row {
                Text(
                    text = deal.direction,
                    color = dirColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = " ${deal.lots} lots @ ${deal.price}",
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
