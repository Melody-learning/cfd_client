package com.astralw.core.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * AstralW 设计词元 (Design Tokens)
 *
 * 继承自上一阶段的沉淀资产 — Linear Pro 风格：
 * - 纯黑背景 #000000
 * - 0.5dp 极细边框
 * - 1px 顶部微光
 *
 * 铁律: 所有颜色必须引用 SemanticColors，所有间距引用 SpacingTokens
 */
object DesignTokens {

    // ─── 基础色板 ───
    object Palette {
        val Black = Color(0xFF000000)
        val White = Color(0xFFFFFFFF)
        val Gray50 = Color(0xFFFAFAFA)
        val Gray100 = Color(0xFFF5F5F5)
        val Gray200 = Color(0xFFEEEEEE)
        val Gray400 = Color(0xFFBDBDBD)
        val Gray600 = Color(0xFF757575)
        val Gray800 = Color(0xFF424242)
        val Gray900 = Color(0xFF212121)

        val Green400 = Color(0xFF66BB6A)
        val Green500 = Color(0xFF4CAF50)
        val Red400 = Color(0xFFEF5350)
        val Red500 = Color(0xFFF44336)

        val Blue400 = Color(0xFF42A5F5)
        val Blue500 = Color(0xFF2196F3)

        val Amber400 = Color(0xFFFFCA28)
    }

    // ─── 语义化颜色 ───
    object SemanticColors {
        // 背景
        val Background = Palette.Black
        val Surface = Color(0xFF0A0A0A)
        val SurfaceElevated = Color(0xFF141414)
        val SurfaceCard = Color(0xFF1A1A1A)

        // 文字
        val TextPrimary = Palette.White
        val TextSecondary = Palette.Gray400
        val TextTertiary = Palette.Gray600

        // 涨跌
        val PriceUp = Palette.Green500
        val PriceDown = Palette.Red500
        val PriceUpSoft = Palette.Green400
        val PriceDownSoft = Palette.Red400

        // 交互
        val Accent = Palette.Blue500
        val AccentSoft = Palette.Blue400

        // 边框
        val Border = Color(0xFF2A2A2A)
        val BorderSubtle = Color(0xFF1F1F1F)

        // 微光 (顶部 1px 高亮)
        val TopGlow = Color(0xFF333333)

        // 风险
        val RiskSafe = Palette.Green500
        val RiskWarning = Palette.Amber400
        val RiskDanger = Palette.Red500
    }

    // ─── 间距词元 ───
    object SpacingTokens {
        val XXS: Dp = 2.dp
        val XS: Dp = 4.dp
        val SM: Dp = 8.dp
        val MD: Dp = 12.dp
        val LG: Dp = 16.dp
        val XL: Dp = 24.dp
        val XXL: Dp = 32.dp
        val XXXL: Dp = 48.dp
    }

    // ─── 圆角词元 ───
    object RadiusTokens {
        val SM: Dp = 4.dp
        val MD: Dp = 8.dp
        val LG: Dp = 12.dp
        val XL: Dp = 16.dp
        val Full: Dp = 999.dp
    }

    // ─── 边框词元 ───
    object BorderTokens {
        /** 极细边框 — Linear Pro 标志性 0.5dp */
        val Thin: Dp = 0.5.dp
        val Regular: Dp = 1.dp
    }
}
