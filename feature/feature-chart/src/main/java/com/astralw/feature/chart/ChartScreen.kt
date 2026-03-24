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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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

/**
 * K 线图页面
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
                // 标题栏：品种名 + 实时价格
                ChartTopBar(
                    displayName = state.displayName,
                    symbol = state.symbol,
                    bid = state.currentBid,
                    change = state.change,
                    changePercent = state.changePercent,
                    isUp = state.isUp,
                    onBack = onBack,
                )

                // 买卖价
                PriceBar(
                    bid = state.currentBid,
                    ask = state.currentAsk,
                    isUp = state.isUp,
                )

                // K 线图
                CandlestickChart(
                    candles = state.candles,
                    currentBid = state.currentBid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = DesignTokens.SpacingTokens.SM),
                )

                // 时间周期选择
                TimeframeSelector(
                    selectedTimeframe = state.selectedTimeframe,
                    onTimeframeSelected = viewModel::selectTimeframe,
                )

                Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.SM))

                // Trade 按钮
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DesignTokens.SpacingTokens.LG)
                        .height(48.dp)
                        .background(
                            color = DesignTokens.SemanticColors.Accent,
                            shape = RoundedCornerShape(DesignTokens.RadiusTokens.MD),
                        )
                        .clickable { onTrade(state.symbol, state.displayName) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Trade ${state.displayName}",
                        color = DesignTokens.Palette.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.LG))
            }
        }
    }
}

@Composable
private fun ChartTopBar(
    displayName: String,
    symbol: String,
    bid: String,
    change: String,
    changePercent: String,
    isUp: Boolean,
    onBack: () -> Unit,
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
                vertical = DesignTokens.SpacingTokens.MD,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 返回
        Text(
            text = "←",
            color = DesignTokens.SemanticColors.TextSecondary,
            fontSize = 20.sp,
            modifier = Modifier
                .clickable { onBack() }
                .padding(end = DesignTokens.SpacingTokens.MD),
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayName,
                color = DesignTokens.SemanticColors.TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = symbol,
                color = DesignTokens.SemanticColors.TextTertiary,
                fontSize = 12.sp,
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = bid,
                color = priceColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "$change ($changePercent%)",
                color = priceColor,
                fontSize = 12.sp,
            )
        }
    }
}

@Composable
private fun PriceBar(bid: String, ask: String, isUp: Boolean) {
    val upColor = DesignTokens.SemanticColors.PriceUp
    val downColor = DesignTokens.SemanticColors.PriceDown

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.SpacingTokens.LG),
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.SpacingTokens.SM),
    ) {
        // Sell (Bid)
        Box(
            modifier = Modifier
                .weight(1f)
                .border(
                    width = DesignTokens.BorderTokens.Thin,
                    color = downColor.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(DesignTokens.RadiusTokens.SM),
                )
                .background(
                    color = downColor.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(DesignTokens.RadiusTokens.SM),
                )
                .padding(
                    horizontal = DesignTokens.SpacingTokens.MD,
                    vertical = DesignTokens.SpacingTokens.SM,
                ),
        ) {
            Column {
                Text(
                    text = "SELL",
                    color = DesignTokens.SemanticColors.TextTertiary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = bid,
                    color = downColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        // Buy (Ask)
        Box(
            modifier = Modifier
                .weight(1f)
                .border(
                    width = DesignTokens.BorderTokens.Thin,
                    color = upColor.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(DesignTokens.RadiusTokens.SM),
                )
                .background(
                    color = upColor.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(DesignTokens.RadiusTokens.SM),
                )
                .padding(
                    horizontal = DesignTokens.SpacingTokens.MD,
                    vertical = DesignTokens.SpacingTokens.SM,
                ),
        ) {
            Column {
                Text(
                    text = "BUY",
                    color = DesignTokens.SemanticColors.TextTertiary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = ask,
                    color = upColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.SM))
}

@Composable
private fun TimeframeSelector(
    selectedTimeframe: String,
    onTimeframeSelected: (String) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = DesignTokens.SpacingTokens.LG),
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.SpacingTokens.SM),
    ) {
        items(TIMEFRAMES) { tf ->
            val isSelected = tf == selectedTimeframe
            val bgColor = if (isSelected) {
                DesignTokens.SemanticColors.Accent
            } else {
                DesignTokens.SemanticColors.SurfaceElevated
            }
            val textColor = if (isSelected) {
                DesignTokens.Palette.White
            } else {
                DesignTokens.SemanticColors.TextSecondary
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(DesignTokens.RadiusTokens.SM))
                    .background(bgColor)
                    .clickable { onTimeframeSelected(tf) }
                    .padding(
                        horizontal = DesignTokens.SpacingTokens.MD,
                        vertical = DesignTokens.SpacingTokens.SM,
                    ),
            ) {
                Text(
                    text = tf,
                    color = textColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}
