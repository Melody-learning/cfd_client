package com.astralw.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * AstralW 主题
 *
 * 始终使用暗色主题（Linear Pro 风格）
 */

private val AstralWDarkColorScheme = darkColorScheme(
    primary = DesignTokens.SemanticColors.Accent,
    onPrimary = DesignTokens.Palette.White,
    secondary = DesignTokens.SemanticColors.AccentSoft,
    onSecondary = DesignTokens.Palette.White,
    background = DesignTokens.SemanticColors.Background,
    onBackground = DesignTokens.SemanticColors.TextPrimary,
    surface = DesignTokens.SemanticColors.Surface,
    onSurface = DesignTokens.SemanticColors.TextPrimary,
    surfaceVariant = DesignTokens.SemanticColors.SurfaceCard,
    onSurfaceVariant = DesignTokens.SemanticColors.TextSecondary,
    outline = DesignTokens.SemanticColors.Border,
    outlineVariant = DesignTokens.SemanticColors.BorderSubtle,
    error = DesignTokens.SemanticColors.RiskDanger,
    onError = DesignTokens.Palette.White,
)

@Composable
fun AstralWTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = AstralWDarkColorScheme,
        typography = AstralWTypography,
        content = content,
    )
}
