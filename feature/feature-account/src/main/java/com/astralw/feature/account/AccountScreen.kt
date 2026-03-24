package com.astralw.feature.account

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.astralw.core.ui.theme.DesignTokens

/**
 * 账户页面 — 显示 MT5 真实账户信息和登出
 */
@Composable
fun AccountScreen(
    onLogout: () -> Unit = {},
    viewModel: AccountViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
                text = "Account",
                style = MaterialTheme.typography.headlineMedium,
                color = DesignTokens.SemanticColors.TextPrimary,
                fontWeight = FontWeight.Bold,
            )
        }

        when (val state = uiState) {
            is AccountUiState.Loading -> {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = DesignTokens.SemanticColors.Accent)
                }
            }
            is AccountUiState.Error -> {
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
                                .clickable { viewModel.loadAccountInfo() }
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
            is AccountUiState.Success -> {
                // 账户基本信息卡
                AccountInfoCard(state)

                Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.MD))

                // 资金信息卡
                BalanceCard(state)

                Spacer(modifier = Modifier.weight(1f))

                // 登出按钮
                LogoutButton(onClick = { viewModel.logout { onLogout() } })
            }
        }
    }
}

@Composable
private fun AccountInfoCard(state: AccountUiState.Success) {
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
        InfoRow("MT5 Account", state.login)
        Spacer(Modifier.height(DesignTokens.SpacingTokens.MD))
        InfoRow("Name", state.name.ifEmpty { "-" })
        Spacer(Modifier.height(DesignTokens.SpacingTokens.MD))
        InfoRow("Group", state.group.ifEmpty { "-" })
        Spacer(Modifier.height(DesignTokens.SpacingTokens.MD))
        InfoRow("Leverage", state.leverage)
        Spacer(Modifier.height(DesignTokens.SpacingTokens.MD))
        InfoRow("Currency", state.currency)
    }
}

@Composable
private fun BalanceCard(state: AccountUiState.Success) {
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
        InfoRow("Balance", "$${state.balance}")
        Spacer(Modifier.height(DesignTokens.SpacingTokens.MD))
        InfoRow("Equity", "$${state.equity}")
        Spacer(Modifier.height(DesignTokens.SpacingTokens.MD))
        InfoRow("Margin", "$${state.margin}")
        Spacer(Modifier.height(DesignTokens.SpacingTokens.MD))
        InfoRow("Free Margin", "$${state.freeMargin}")
        Spacer(Modifier.height(DesignTokens.SpacingTokens.MD))
        InfoRow(
            "Margin Level",
            if (state.marginLevel == "0") "∞" else "${state.marginLevel}%",
        )
    }
}

@Composable
private fun LogoutButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.SpacingTokens.LG)
            .padding(bottom = DesignTokens.SpacingTokens.XXXL)
            .height(48.dp)
            .border(
                DesignTokens.BorderTokens.Thin,
                DesignTokens.SemanticColors.RiskDanger.copy(alpha = 0.5f),
                RoundedCornerShape(DesignTokens.RadiusTokens.MD),
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Sign Out",
            color = DesignTokens.SemanticColors.RiskDanger,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = DesignTokens.SemanticColors.TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            color = DesignTokens.SemanticColors.TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}
