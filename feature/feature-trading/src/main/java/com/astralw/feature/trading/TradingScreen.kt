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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.astralw.core.data.model.OrderType
import com.astralw.core.ui.theme.DesignTokens

/**
 * 下单交易页面 — 支持市价单 + 挂单
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
            .navigationBarsPadding()
            .padding(horizontal = DesignTokens.SpacingTokens.LG)
            .verticalScroll(rememberScrollState()),
    ) {
        // ─── 标题栏 ───
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = DesignTokens.SpacingTokens.MD),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = DesignTokens.SemanticColors.TextSecondary,
                )
            }
            Text(
                text = "新订单",
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

        // ─── 订单类型选择器 ───
        OrderTypeSelector(
            selectedType = uiState.orderType,
            onTypeSelected = viewModel::onOrderTypeChanged,
        )

        Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.MD))

        // ─── 实时价格 ───
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

        // ─── 挂单价格 (仅挂单模式显示) ───
        if (uiState.isPendingOrder) {
            OrderField(
                label = "挂单价格",
                value = uiState.pendingPrice,
                onValueChange = viewModel::onPendingPriceChanged,
            )
            Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.MD))

            // 到期时间选择
            ExpirationSelector(
                selectedType = uiState.expirationType,
                onTypeSelected = viewModel::onExpirationChanged,
            )
            Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.MD))
        }

        // ─── 手数输入 ───
        OrderField(
            label = "手数",
            value = uiState.lots,
            onValueChange = viewModel::onLotsChanged,
        )

        Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.MD))

        // ─── 止损 / 止盈 ───
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.SpacingTokens.MD),
        ) {
            OrderField(
                label = "止损",
                value = uiState.stopLoss,
                onValueChange = viewModel::onStopLossChanged,
                modifier = Modifier.weight(1f),
            )
            OrderField(
                label = "止盈",
                value = uiState.takeProfit,
                onValueChange = viewModel::onTakeProfitChanged,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.LG))

        // ─── 资金信息 ───
        InfoRow(label = "预估保证金", value = "$${uiState.estimatedMargin}")
        Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.SM))
        InfoRow(label = "余额", value = "$${uiState.balance}")
        Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.SM))
        InfoRow(
            label = "可用保证金",
            value = "$${uiState.freeMargin}",
            valueColor = if (uiState.freeMargin.startsWith("-")) {
                DesignTokens.SemanticColors.PriceDown
            } else {
                DesignTokens.SemanticColors.PriceUp
            },
        )

        // ─── 错误提示 ───
        if (uiState.errorMessage != null) {
            Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.MD))
            Text(
                text = uiState.errorMessage ?: "",
                color = DesignTokens.SemanticColors.RiskDanger,
                fontSize = 13.sp,
            )
        }

        Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.XL))

        // ─── SELL / BUY 按钮 ───
        val buttonLabel = if (uiState.isPendingOrder) {
            "卖出" to "买入"
        } else {
            "SELL" to "BUY"
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.SpacingTokens.MD),
        ) {
            TradeButton(
                label = buttonLabel.first,
                price = uiState.currentBid,
                color = DesignTokens.SemanticColors.PriceDown,
                isLoading = uiState.isSubmitting,
                onClick = { viewModel.placeOrder(OrderDirection.SELL) },
                modifier = Modifier.weight(1f),
            )
            TradeButton(
                label = buttonLabel.second,
                price = uiState.currentAsk,
                color = DesignTokens.SemanticColors.PriceUp,
                isLoading = uiState.isSubmitting,
                onClick = { viewModel.placeOrder(OrderDirection.BUY) },
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.XL))
    }
}

// ─── 订单类型选择器 ───

@Composable
private fun OrderTypeSelector(
    selectedType: OrderType,
    onTypeSelected: (OrderType) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
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
                .clickable { expanded = true }
                .padding(DesignTokens.SpacingTokens.MD),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = selectedType.label,
                color = DesignTokens.SemanticColors.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = "选择订单类型",
                tint = DesignTokens.SemanticColors.TextSecondary,
                modifier = Modifier.size(20.dp),
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(DesignTokens.SemanticColors.SurfaceElevated),
        ) {
            OrderType.entries.forEach { type ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = type.label,
                            color = if (type == selectedType) {
                                DesignTokens.SemanticColors.Accent
                            } else {
                                DesignTokens.SemanticColors.TextPrimary
                            },
                            fontSize = 14.sp,
                        )
                    },
                    onClick = {
                        onTypeSelected(type)
                        expanded = false
                    },
                )
            }
        }
    }
}

// ─── 到期时间选择器 ───

@Composable
private fun ExpirationSelector(
    selectedType: ExpirationType,
    onTypeSelected: (ExpirationType) -> Unit,
) {
    Column {
        Text(
            text = "到期时间",
            color = DesignTokens.SemanticColors.TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = DesignTokens.SpacingTokens.XS),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.SpacingTokens.SM),
        ) {
            ExpirationType.entries.forEach { type ->
                val isSelected = type == selectedType
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = if (isSelected) DesignTokens.SemanticColors.Accent.copy(alpha = 0.15f)
                            else DesignTokens.SemanticColors.SurfaceCard,
                            shape = RoundedCornerShape(DesignTokens.RadiusTokens.SM),
                        )
                        .border(
                            width = DesignTokens.BorderTokens.Thin,
                            color = if (isSelected) DesignTokens.SemanticColors.Accent
                            else DesignTokens.SemanticColors.Border,
                            shape = RoundedCornerShape(DesignTokens.RadiusTokens.SM),
                        )
                        .clickable { onTypeSelected(type) }
                        .padding(vertical = DesignTokens.SpacingTokens.SM),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = type.label,
                        color = if (isSelected) DesignTokens.SemanticColors.Accent
                        else DesignTokens.SemanticColors.TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}

// ─── 通用组件 ───

@Composable
private fun TradeButton(
    label: String,
    price: String,
    color: Color,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .background(
                color = color,
                shape = RoundedCornerShape(DesignTokens.RadiusTokens.MD),
            )
            .clickable(enabled = !isLoading) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = DesignTokens.Palette.White,
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = label,
                    color = DesignTokens.Palette.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = price,
                    color = DesignTokens.Palette.White.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    valueColor: Color = DesignTokens.SemanticColors.TextPrimary,
) {
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
            text = label,
            color = DesignTokens.SemanticColors.TextSecondary,
            fontSize = 13.sp,
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
        )
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
