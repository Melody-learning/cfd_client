package com.astralw.feature.trading

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.astralw.core.data.model.OrderDirection
import com.astralw.core.ui.theme.DesignTokens

/**
 * 下单交易页面
 */
@Composable
fun TradingScreen(
    onBack: () -> Unit = {},
    onOrderPlaced: () -> Unit = {},
    viewModel: TradingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.orderPlaced) {
        onOrderPlaced()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DesignTokens.SemanticColors.Background)
            .padding(horizontal = DesignTokens.SpacingTokens.LG),
    ) {
        // 标题栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = DesignTokens.SpacingTokens.MD),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "←",
                color = DesignTokens.SemanticColors.TextSecondary,
                fontSize = 20.sp,
                modifier = Modifier
                    .clickable { onBack() }
                    .padding(end = DesignTokens.SpacingTokens.MD),
            )
            Text(
                text = "New Order",
                color = DesignTokens.SemanticColors.TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = uiState.displayName,
                color = DesignTokens.SemanticColors.TextSecondary,
                fontSize = 14.sp,
            )
        }

        // 实时价格
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = "Bid",
                    color = DesignTokens.SemanticColors.TextTertiary,
                    fontSize = 12.sp,
                )
                Text(
                    text = uiState.currentBid,
                    color = DesignTokens.SemanticColors.PriceDown,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Ask",
                    color = DesignTokens.SemanticColors.TextTertiary,
                    fontSize = 12.sp,
                )
                Text(
                    text = uiState.currentAsk,
                    color = DesignTokens.SemanticColors.PriceUp,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.XL))

        // 手数输入
        OrderField(
            label = "Volume (lots)",
            value = uiState.lots,
            onValueChange = viewModel::onLotsChanged,
        )

        Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.MD))

        // 止损 / 止盈
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.SpacingTokens.MD),
        ) {
            OrderField(
                label = "Stop Loss",
                value = uiState.stopLoss,
                onValueChange = viewModel::onStopLossChanged,
                modifier = Modifier.weight(1f),
            )
            OrderField(
                label = "Take Profit",
                value = uiState.takeProfit,
                onValueChange = viewModel::onTakeProfitChanged,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.LG))

        // 预估保证金
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = DesignTokens.SemanticColors.SurfaceCard,
                    shape = RoundedCornerShape(DesignTokens.RadiusTokens.MD),
                )
                .border(
                    width = DesignTokens.BorderTokens.Thin,
                    color = DesignTokens.SemanticColors.Border,
                    shape = RoundedCornerShape(DesignTokens.RadiusTokens.MD),
                )
                .padding(DesignTokens.SpacingTokens.MD),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Required Margin",
                color = DesignTokens.SemanticColors.TextSecondary,
                fontSize = 13.sp,
            )
            Text(
                text = "$${uiState.estimatedMargin}",
                color = DesignTokens.SemanticColors.TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.SM))

        // Balance
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = DesignTokens.SemanticColors.SurfaceCard,
                    shape = RoundedCornerShape(DesignTokens.RadiusTokens.MD),
                )
                .border(
                    width = DesignTokens.BorderTokens.Thin,
                    color = DesignTokens.SemanticColors.Border,
                    shape = RoundedCornerShape(DesignTokens.RadiusTokens.MD),
                )
                .padding(DesignTokens.SpacingTokens.MD),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Balance",
                color = DesignTokens.SemanticColors.TextSecondary,
                fontSize = 13.sp,
            )
            Text(
                text = "$${uiState.balance}",
                color = DesignTokens.SemanticColors.TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.SM))

        // Free Margin
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = DesignTokens.SemanticColors.SurfaceCard,
                    shape = RoundedCornerShape(DesignTokens.RadiusTokens.MD),
                )
                .border(
                    width = DesignTokens.BorderTokens.Thin,
                    color = DesignTokens.SemanticColors.Border,
                    shape = RoundedCornerShape(DesignTokens.RadiusTokens.MD),
                )
                .padding(DesignTokens.SpacingTokens.MD),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Free Margin",
                color = DesignTokens.SemanticColors.TextSecondary,
                fontSize = 13.sp,
            )
            val freeMarginColor = if (uiState.freeMargin.startsWith("-")) {
                DesignTokens.SemanticColors.PriceDown
            } else {
                DesignTokens.SemanticColors.PriceUp
            }
            Text(
                text = "$${uiState.freeMargin}",
                color = freeMarginColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        // 错误提示
        if (uiState.errorMessage != null) {
            Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.MD))
            Text(
                text = uiState.errorMessage ?: "",
                color = DesignTokens.SemanticColors.RiskDanger,
                fontSize = 13.sp,
            )
        }

        Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.XL))

        // SELL / BUY 按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.SpacingTokens.MD),
        ) {
            // SELL
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .background(
                        color = DesignTokens.SemanticColors.PriceDown,
                        shape = RoundedCornerShape(DesignTokens.RadiusTokens.MD),
                    )
                    .clickable(enabled = !uiState.isSubmitting) {
                        viewModel.placeOrder(OrderDirection.SELL)
                    },
                contentAlignment = Alignment.Center,
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        color = DesignTokens.Palette.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "SELL",
                            color = DesignTokens.Palette.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = uiState.currentBid,
                            color = DesignTokens.Palette.White.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                        )
                    }
                }
            }

            // BUY
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .background(
                        color = DesignTokens.SemanticColors.PriceUp,
                        shape = RoundedCornerShape(DesignTokens.RadiusTokens.MD),
                    )
                    .clickable(enabled = !uiState.isSubmitting) {
                        viewModel.placeOrder(OrderDirection.BUY)
                    },
                contentAlignment = Alignment.Center,
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        color = DesignTokens.Palette.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "BUY",
                            color = DesignTokens.Palette.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = uiState.currentAsk,
                            color = DesignTokens.Palette.White.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            color = DesignTokens.SemanticColors.TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = DesignTokens.SpacingTokens.XS),
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = TextFieldDefaults.colors(
                focusedTextColor = DesignTokens.SemanticColors.TextPrimary,
                unfocusedTextColor = DesignTokens.SemanticColors.TextPrimary,
                cursorColor = DesignTokens.SemanticColors.Accent,
                focusedContainerColor = DesignTokens.SemanticColors.SurfaceCard,
                unfocusedContainerColor = DesignTokens.SemanticColors.SurfaceCard,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            shape = RoundedCornerShape(DesignTokens.RadiusTokens.MD),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = DesignTokens.BorderTokens.Thin,
                    color = DesignTokens.SemanticColors.Border,
                    shape = RoundedCornerShape(DesignTokens.RadiusTokens.MD),
                ),
        )
    }
}
