package com.astralw.feature.history

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.astralw.core.data.model.Deal
import com.astralw.core.ui.theme.DesignTokens
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 历史成交页面
 *
 * MT5 风格: 时间筛选 Tab + 成交记录列表
 */
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DesignTokens.SemanticColors.Background),
    ) {
        // ─── 居中标题 ───
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = DesignTokens.SpacingTokens.MD),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.history_title),
                color = DesignTokens.SemanticColors.TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        // ─── 分割线 ───
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(DesignTokens.SemanticColors.Border),
        )

        when (val state = uiState) {
            is HistoryUiState.Loading -> {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = DesignTokens.SemanticColors.Accent)
                }
            }
            is HistoryUiState.Error -> {
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
                                .clickable { viewModel.retry() }
                                .padding(
                                    horizontal = DesignTokens.SpacingTokens.LG,
                                    vertical = DesignTokens.SpacingTokens.SM,
                                ),
                        ) {
                            Text(
                                text = stringResource(R.string.history_retry),
                                color = DesignTokens.SemanticColors.Accent,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }
            is HistoryUiState.Success -> {
                HistoryContent(
                    state = state,
                    onFilterSelected = viewModel::selectFilter,
                )
            }
        }
    }
}

@Composable
private fun HistoryContent(
    state: HistoryUiState.Success,
    onFilterSelected: (Int) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // ─── 时间筛选 Tab ───
        item {
            TimeFilterTabs(
                selectedIndex = state.selectedFilterIndex,
                onSelected = onFilterSelected,
            )
        }

        if (state.deals.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DesignTokens.SpacingTokens.XXXL),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.history_empty),
                        color = DesignTokens.SemanticColors.TextTertiary,
                        fontSize = 14.sp,
                    )
                }
            }
        } else {
            items(state.deals) { deal ->
                DealItem(deal = deal)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// 时间筛选 Tab
// ═══════════════════════════════════════════════════════

@Composable
private fun TimeFilterTabs(
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
) {
    val filters = listOf(
        R.string.history_today,
        R.string.history_7days,
        R.string.history_30days,
        R.string.history_all,
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = DesignTokens.SpacingTokens.LG,
                vertical = DesignTokens.SpacingTokens.MD,
            ),
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.SpacingTokens.SM),
    ) {
        filters.forEachIndexed { index, labelRes ->
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
                    .clickable { onSelected(index) }
                    .padding(vertical = DesignTokens.SpacingTokens.SM),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(labelRes),
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
            .padding(
                horizontal = DesignTokens.SpacingTokens.LG,
                vertical = DesignTokens.SpacingTokens.XS,
            )
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
        // 品种图标
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

        // 品种名 + 方向手数 + 时间
        Column(Modifier.weight(1f)) {
            Text(
                text = deal.displayName,
                color = DesignTokens.SemanticColors.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Row {
                val dirText = if (deal.direction == "BUY") {
                    stringResource(R.string.history_buy)
                } else {
                    stringResource(R.string.history_sell)
                }
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

        // 盈亏
        val sign = if (profitBd >= BigDecimal.ZERO) "+" else ""
        Text(
            text = "$sign$${deal.profit}",
            color = profitColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
        )
    }
}
