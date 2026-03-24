package com.astralw.feature.market

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
import androidx.compose.foundation.lazy.LazyColumn
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
import com.astralw.core.data.model.Quote
import com.astralw.core.ui.theme.DesignTokens

/**
 * 行情列表页面
 */
@Composable
fun MarketScreen(
    onSymbolClick: (symbol: String, displayName: String) -> Unit = { _, _ -> },
    viewModel: MarketViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DesignTokens.SemanticColors.Background)
    ) {
        // 标题栏
        MarketTopBar()

        when (val state = uiState) {
            is MarketUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = DesignTokens.SemanticColors.Accent,
                    )
                }
            }
            is MarketUiState.Error -> {
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
            is MarketUiState.Success -> {
                // 分类筛选栏
                CategoryFilterRow(
                    selectedCategory = state.selectedCategory,
                    onCategorySelected = viewModel::selectCategory,
                )

                // 行情列表
                LazyColumn(
                    contentPadding = PaddingValues(
                        horizontal = DesignTokens.SpacingTokens.LG,
                        vertical = DesignTokens.SpacingTokens.SM,
                    ),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.SpacingTokens.XXS),
                ) {
                    items(
                        items = state.quotes,
                        key = { it.symbol },
                    ) { quote ->
                        QuoteItem(
                            quote = quote,
                            onClick = { onSymbolClick(quote.symbol, quote.displayName) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MarketTopBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = DesignTokens.SpacingTokens.LG,
                vertical = DesignTokens.SpacingTokens.MD,
            )
    ) {
        Text(
            text = "Markets",
            style = MaterialTheme.typography.headlineMedium,
            color = DesignTokens.SemanticColors.TextPrimary,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun CategoryFilterRow(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
) {
    val labels = mapOf(
        "all" to "All",
        "forex" to "Forex",
        "commodities" to "Commodities",
        "indices" to "Indices",
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = DesignTokens.SpacingTokens.LG),
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.SpacingTokens.SM),
    ) {
        items(MARKET_CATEGORIES) { category ->
            val isSelected = category == selectedCategory
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
                    .clip(RoundedCornerShape(DesignTokens.RadiusTokens.Full))
                    .background(bgColor)
                    .clickable { onCategorySelected(category) }
                    .padding(
                        horizontal = DesignTokens.SpacingTokens.LG,
                        vertical = DesignTokens.SpacingTokens.SM,
                    ),
            ) {
                Text(
                    text = labels[category] ?: category,
                    color = textColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.SM))
}

/**
 * 行情列表项 — Linear Pro 风格
 *
 * 0.5dp 极细边框 + 1px 顶部微光
 */
@Composable
private fun QuoteItem(quote: Quote, onClick: () -> Unit = {}) {
    val priceColor = if (quote.isUp) {
        DesignTokens.SemanticColors.PriceUp
    } else {
        DesignTokens.SemanticColors.PriceDown
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = DesignTokens.BorderTokens.Thin,
                color = DesignTokens.SemanticColors.Border,
                shape = RoundedCornerShape(DesignTokens.RadiusTokens.MD),
            )
            .background(
                color = DesignTokens.SemanticColors.SurfaceCard,
                shape = RoundedCornerShape(DesignTokens.RadiusTokens.MD),
            )
            .clickable { onClick() }
            .padding(
                horizontal = DesignTokens.SpacingTokens.LG,
                vertical = DesignTokens.SpacingTokens.MD,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 左侧: 品种名称
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = quote.displayName,
                color = DesignTokens.SemanticColors.TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Spread: ${quote.spread}",
                color = DesignTokens.SemanticColors.TextTertiary,
                fontSize = 11.sp,
            )
        }

        // 中间: Bid / Ask
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = quote.bid,
                color = DesignTokens.SemanticColors.TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = quote.ask,
                color = DesignTokens.SemanticColors.TextSecondary,
                fontSize = 12.sp,
            )
        }

        Spacer(modifier = Modifier.width(DesignTokens.SpacingTokens.MD))

        // 右侧: 涨跌幅
        Box(
            modifier = Modifier
                .background(
                    color = priceColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(DesignTokens.RadiusTokens.SM),
                )
                .padding(
                    horizontal = DesignTokens.SpacingTokens.SM,
                    vertical = DesignTokens.SpacingTokens.XS,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "${quote.changePercent}%",
                color = priceColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
