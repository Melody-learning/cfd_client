package com.astralw.feature.settings

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.astralw.core.ui.theme.DesignTokens

/**
 * 更多/设置页面 — MT5 风格
 *
 * 布局: 账户头部 → 财务概览 → 功能列表 → 登出
 */
@Composable
fun SettingsScreen(
    onLogout: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
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
                text = stringResource(R.string.settings_title),
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
            is SettingsUiState.Loading -> {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = DesignTokens.SemanticColors.Accent)
                }
            }
            is SettingsUiState.Error -> {
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
                                text = stringResource(R.string.settings_retry),
                                color = DesignTokens.SemanticColors.Accent,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }
            is SettingsUiState.Success -> {
                SettingsContent(
                    state = state,
                    onLogout = { viewModel.logout { onLogout() } },
                )
            }
        }
    }
}

@Composable
private fun SettingsContent(
    state: SettingsUiState.Success,
    onLogout: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // ─── 1. 账户信息头部 ───
        AccountHeader(state)

        Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.MD))

        // ─── 2. 财务概览 ───
        FinanceOverview(state)

        Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.MD))

        // ─── 3. 功能列表 ───
        FunctionList()

        Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.LG))

        // ─── 4. 登出按钮 ───
        LogoutButton(onClick = onLogout)

        Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.XXXL))
    }
}

// ═══════════════════════════════════════════════════════
// 1. 账户信息头部
// ═══════════════════════════════════════════════════════

@Composable
private fun AccountHeader(state: SettingsUiState.Success) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = DesignTokens.SpacingTokens.LG,
                vertical = DesignTokens.SpacingTokens.LG,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 头像
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(DesignTokens.SemanticColors.SurfaceElevated),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = state.name.take(1).uppercase().ifEmpty { "U" },
                color = DesignTokens.SemanticColors.TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.width(DesignTokens.SpacingTokens.MD))

        Column {
            Text(
                text = state.name,
                color = DesignTokens.SemanticColors.TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "${stringResource(R.string.settings_login_id)}: ${state.login}",
                color = DesignTokens.SemanticColors.TextTertiary,
                fontSize = 12.sp,
            )
        }
    }
}

// ═══════════════════════════════════════════════════════
// 2. 财务概览
// ═══════════════════════════════════════════════════════

@Composable
private fun FinanceOverview(state: SettingsUiState.Success) {
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            FinanceItem(
                label = stringResource(R.string.settings_balance),
                value = "$${state.balance}",
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp)
                    .background(DesignTokens.SemanticColors.Border),
            )

            FinanceItem(
                label = stringResource(R.string.settings_equity),
                value = "$${state.equity}",
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp)
                    .background(DesignTokens.SemanticColors.Border),
            )

            FinanceItem(
                label = stringResource(R.string.settings_free_margin),
                value = "$${state.freeMargin}",
            )
        }
    }
}

@Composable
private fun FinanceItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = DesignTokens.SemanticColors.TextTertiary,
            fontSize = 11.sp,
        )
        Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.XS))
        Text(
            text = value,
            color = DesignTokens.SemanticColors.TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

// ═══════════════════════════════════════════════════════
// 3. 功能列表 (消息 + 关于)
// ═══════════════════════════════════════════════════════

@Composable
private fun FunctionList() {
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
            ),
    ) {
        // 消息
        FunctionRow(
            icon = Icons.Outlined.Email,
            label = stringResource(R.string.settings_messages),
            onClick = { /* TODO: 消息页面 */ },
        )

        // 分割线
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DesignTokens.SpacingTokens.LG)
                .height(1.dp)
                .background(DesignTokens.SemanticColors.Border),
        )

        // 关于
        FunctionRow(
            icon = Icons.Outlined.Info,
            label = stringResource(R.string.settings_about),
            trailingText = "v0.1.0",
            onClick = { /* TODO: 关于页面 */ },
        )
    }
}

@Composable
private fun FunctionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    trailingText: String? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(
                horizontal = DesignTokens.SpacingTokens.LG,
                vertical = DesignTokens.SpacingTokens.MD,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = DesignTokens.SemanticColors.TextSecondary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(DesignTokens.SpacingTokens.MD))
        Text(
            text = label,
            color = DesignTokens.SemanticColors.TextPrimary,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f),
        )
        if (trailingText != null) {
            Text(
                text = trailingText,
                color = DesignTokens.SemanticColors.TextTertiary,
                fontSize = 12.sp,
            )
            Spacer(modifier = Modifier.width(DesignTokens.SpacingTokens.XS))
        }
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = DesignTokens.SemanticColors.TextTertiary,
            modifier = Modifier.size(18.dp),
        )
    }
}

// ═══════════════════════════════════════════════════════
// 4. 登出按钮
// ═══════════════════════════════════════════════════════

@Composable
private fun LogoutButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.SpacingTokens.LG)
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
            text = stringResource(R.string.settings_logout),
            color = DesignTokens.SemanticColors.RiskDanger,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
        )
    }
}
