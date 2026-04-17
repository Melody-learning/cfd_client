package com.astralw.feature.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.astralw.core.ui.theme.DesignTokens

import androidx.compose.foundation.layout.navigationBarsPadding

/**
 * K 线图页面 — 按原型图重构
 *
 * 布局：顶部导航 → 品种信息 → 时间周期 → K线图 → 底部价格+买卖栏
 */
@Composable
fun ChartScreen(
    onBack: () -> Unit = {},
    onTrade: (symbol: String, displayName: String) -> Unit = { _, _ -> },
    viewModel: ChartViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DesignTokens.SemanticColors.Background)
            .navigationBarsPadding()
    ) {
        when (val state = uiState) {
            is ChartUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = DesignTokens.SemanticColors.Accent,
                    )
                }
            }
            is ChartUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.message,
                        color = DesignTokens.SemanticColors.RiskDanger,
                    )
                }
            }
            is ChartUiState.Success -> {
                // ─── 顶部导航栏：← | 交易 | 资金 ───
                ChartTopNavBar(onBack = onBack)

                // ─── 品种信息行 ───
                SymbolInfoRow(
                    displayName = state.displayName,
                    symbol = state.symbol,
                )

                // ─── 时间周期选择器 ───
                TimeframeSelector(
                    selectedTimeframe = state.selectedTimeframe,
                    onTimeframeSelected = viewModel::selectTimeframe,
                )

                // ─── K 线图 ───
                CandlestickChart(
                    candles = state.candles,
                    currentBid = state.currentBid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = DesignTokens.SpacingTokens.SM),
                )

                // ─── 底部价格信息栏 ───
                BottomPriceBar(
                    bid = state.currentBid,
                    change = state.change,
                    changePercent = state.changePercent,
                    isUp = state.isUp,
                )

                // ─── 底部交易栏 ───
                BottomTradeBar(
                    symbol = state.symbol,
                    displayName = state.displayName,
                    bid = state.currentBid,
                    ask = state.currentAsk,
                    onTrade = onTrade,
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// 顶部导航栏：← 返回 | 交易（居中标题）| 资金图标
// ═══════════════════════════════════════════════════════

@Composable
private fun ChartTopNavBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.SpacingTokens.XS),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = DesignTokens.SemanticColors.TextPrimary,
            )
        }

        Text(
            text = "交易",
            color = DesignTokens.SemanticColors.TextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )

        IconButton(onClick = { /* TODO: 资金页 */ }) {
            Icon(
                imageVector = Icons.Outlined.AccountBalance,
                contentDescription = "资金",
                tint = DesignTokens.SemanticColors.TextPrimary,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

// ═══════════════════════════════════════════════════════
// 品种信息行：品种名 ▼ | 描述 + 规则 >
// ═══════════════════════════════════════════════════════

@Composable
private fun SymbolInfoRow(displayName: String, symbol: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = DesignTokens.SpacingTokens.LG,
                vertical = DesignTokens.SpacingTokens.XS,
            ),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = displayName,
                color = DesignTokens.SemanticColors.TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.width(DesignTokens.SpacingTokens.XS))
            Text(
                text = "▼",
                color = DesignTokens.SemanticColors.TextTertiary,
                fontSize = 12.sp,
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$symbol 永续合约",
                color = DesignTokens.SemanticColors.TextTertiary,
                fontSize = 12.sp,
            )
            Text(
                text = " ｜ ",
                color = DesignTokens.SemanticColors.TextTertiary,
                fontSize = 12.sp,
            )
            Text(
                text = "规则",
                color = DesignTokens.SemanticColors.TextTertiary,
                fontSize = 12.sp,
            )
            Spacer(modifier = Modifier.width(DesignTokens.SpacingTokens.XXS))
            Text(
                text = ">",
                color = DesignTokens.SemanticColors.TextTertiary,
                fontSize = 12.sp,
            )
        }
    }
}

// ═══════════════════════════════════════════════════════
// 时间周期选择器
// ═══════════════════════════════════════════════════════

@Composable
private fun TimeframeSelector(
    selectedTimeframe: String,
    onTimeframeSelected: (String) -> Unit,
) {
    val displayLabels = mapOf(
        "M1" to "1分",
        "M5" to "5分",
        "M15" to "15分",
        "H1" to "1小时",
        "H4" to "4小时",
        "D1" to "日线",
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = DesignTokens.SpacingTokens.LG),
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.SpacingTokens.MD),
        modifier = Modifier.padding(vertical = DesignTokens.SpacingTokens.SM),
    ) {
        items(TIMEFRAMES) { tf ->
            val isSelected = tf == selectedTimeframe
            val textColor = if (isSelected) {
                DesignTokens.SemanticColors.TextPrimary
            } else {
                DesignTokens.SemanticColors.TextTertiary
            }

            Text(
                text = displayLabels[tf] ?: tf,
                color = textColor,
                fontSize = if (isSelected) 15.sp else 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier
                    .clickable { onTimeframeSelected(tf) }
                    .padding(vertical = DesignTokens.SpacingTokens.XS),
            )
        }
    }
}

// ═══════════════════════════════════════════════════════
// 底部价格信息栏：当前价格 + 涨跌 | ● 交易中
// ═══════════════════════════════════════════════════════

@Composable
private fun BottomPriceBar(
    bid: String,
    change: String,
    changePercent: String,
    isUp: Boolean,
) {
    val priceColor = if (isUp) {
        DesignTokens.SemanticColors.PriceUp
    } else {
        DesignTokens.SemanticColors.PriceDown
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = DesignTokens.SpacingTokens.LG,
                vertical = DesignTokens.SpacingTokens.SM,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 价格
        Text(
            text = bid,
            color = DesignTokens.SemanticColors.TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.width(DesignTokens.SpacingTokens.SM))

        // 涨跌幅
        Column {
            Text(
                text = change,
                color = priceColor,
                fontSize = 12.sp,
            )
            Text(
                text = "$changePercent%",
                color = priceColor,
                fontSize = 12.sp,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 交易状态
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(DesignTokens.SemanticColors.PriceUp),
            )
            Spacer(modifier = Modifier.width(DesignTokens.SpacingTokens.XS))
            Text(
                text = "交易中",
                color = DesignTokens.SemanticColors.TextSecondary,
                fontSize = 13.sp,
            )
        }
    }
}

// ═══════════════════════════════════════════════════════
// 底部交易栏：持仓图标 | 卖出 | 买入
// ═══════════════════════════════════════════════════════

@Composable
private fun BottomTradeBar(
    symbol: String,
    displayName: String,
    bid: String,
    ask: String,
    onTrade: (String, String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = DesignTokens.SpacingTokens.LG,
                vertical = DesignTokens.SpacingTokens.SM,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.SpacingTokens.SM),
    ) {
        // 持仓快捷入口
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable { /* TODO: 跳转持仓 */ },
        ) {
            Icon(
                imageVector = Icons.Outlined.Work,
                contentDescription = "持仓",
                tint = DesignTokens.SemanticColors.TextSecondary,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = "持仓",
                color = DesignTokens.SemanticColors.TextSecondary,
                fontSize = 10.sp,
            )
        }

        // 卖出按钮
        Box(
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .border(
                    width = DesignTokens.BorderTokens.Thin,
                    color = DesignTokens.SemanticColors.Border,
                    shape = RoundedCornerShape(DesignTokens.RadiusTokens.MD),
                )
                .clickable { onTrade(symbol, displayName) },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "卖出",
                color = DesignTokens.SemanticColors.TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        // 买入按钮（强调色）
        Box(
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .background(
                    color = DesignTokens.SemanticColors.TextPrimary,
                    shape = RoundedCornerShape(DesignTokens.RadiusTokens.MD),
                )
                .clickable { onTrade(symbol, displayName) },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "买入",
                color = DesignTokens.SemanticColors.Background,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
