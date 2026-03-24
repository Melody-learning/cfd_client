package com.astralw.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.astralw.core.ui.theme.DesignTokens

/**
 * 登录/注册页面 — Linear Pro 风格，尽量简洁
 */
@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            onLoginSuccess()
        }
    }

    if (uiState.isLoggedIn) return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DesignTokens.SemanticColors.Background)
            .padding(horizontal = DesignTokens.SpacingTokens.XL),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        // Logo / 标题
        Text(
            text = "AstralW",
            color = DesignTokens.SemanticColors.TextPrimary,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.SM))

        Text(
            text = "CFD Trading",
            color = DesignTokens.SemanticColors.TextTertiary,
            fontSize = 14.sp,
            letterSpacing = 3.sp,
        )

        Spacer(modifier = Modifier.height(48.dp))

        // 注册模式 — 显示名称输入
        if (uiState.isRegisterMode) {
            AuthTextField(
                value = uiState.displayName,
                onValueChange = viewModel::onDisplayNameChanged,
                placeholder = "Display Name",
            )
            Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.MD))
        }

        // Email
        AuthTextField(
            value = uiState.email,
            onValueChange = viewModel::onEmailChanged,
            placeholder = "Email",
            keyboardType = KeyboardType.Email,
        )

        Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.MD))

        // Password
        AuthTextField(
            value = uiState.password,
            onValueChange = viewModel::onPasswordChanged,
            placeholder = "Password",
            isPassword = true,
        )

        // 错误提示
        if (uiState.errorMessage != null) {
            Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.MD))
            Text(
                text = uiState.errorMessage ?: "",
                color = DesignTokens.SemanticColors.RiskDanger,
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.XL))

        // 提交按钮
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(
                    color = DesignTokens.SemanticColors.Accent,
                    shape = RoundedCornerShape(DesignTokens.RadiusTokens.MD),
                )
                .clickable(enabled = !uiState.isLoading) { viewModel.submit() },
            contentAlignment = Alignment.Center,
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = DesignTokens.Palette.White,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Text(
                    text = if (uiState.isRegisterMode) "Create Account" else "Sign In",
                    color = DesignTokens.Palette.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Spacer(modifier = Modifier.height(DesignTokens.SpacingTokens.LG))

        // 切换登录/注册
        Text(
            text = if (uiState.isRegisterMode) {
                "Already have an account? Sign In"
            } else {
                "Don't have an account? Register"
            },
            color = DesignTokens.SemanticColors.Accent,
            fontSize = 13.sp,
            modifier = Modifier
                .clickable { viewModel.toggleMode() }
                .padding(DesignTokens.SpacingTokens.SM),
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * 统一风格输入框 — 0.5dp 极细边框
 */
@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                color = DesignTokens.SemanticColors.TextTertiary,
                fontSize = 14.sp,
            )
        },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
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
