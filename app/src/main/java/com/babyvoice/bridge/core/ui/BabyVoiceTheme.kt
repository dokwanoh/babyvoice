package com.babyvoice.bridge.core.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = Color(0xFF2563EB),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDEBFF),
    onPrimaryContainer = Color(0xFF0F172A),
    secondary = Color(0xFF16A34A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD7F7E1),
    onSecondaryContainer = Color(0xFF0F172A),
    tertiary = Color(0xFFD97706),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFDE7BF),
    onTertiaryContainer = Color(0xFF0F172A),
    background = Color(0xFFFCFAF6),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF4F7F4),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFD7E0DB),
    outlineVariant = Color(0xFFE7EEEA),
    error = Color(0xFFDC2626),
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF60A5FA),
    onPrimary = Color(0xFF0B0F14),
    primaryContainer = Color(0xFF102033),
    onPrimaryContainer = Color(0xFFF8FAFC),
    secondary = Color(0xFF4ADE80),
    onSecondary = Color(0xFF0B0F14),
    secondaryContainer = Color(0xFF12311F),
    onSecondaryContainer = Color(0xFFF8FAFC),
    tertiary = Color(0xFFF59E0B),
    onTertiary = Color(0xFF0B0F14),
    tertiaryContainer = Color(0xFF3B2C0F),
    onTertiaryContainer = Color(0xFFF8FAFC),
    background = Color(0xFF0B0F14),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF121821),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF17212B),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF2B3643),
    outlineVariant = Color(0xFF1F2833),
    error = Color(0xFFF87171),
    onError = Color(0xFF0B0F14),
    errorContainer = Color(0xFF3B1111),
    onErrorContainer = Color(0xFFF8FAFC),
)

private val AppTypography = androidx.compose.material3.Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 40.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 46.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 38.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 24.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 30.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 26.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 18.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 28.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 26.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 22.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 20.sp,
    ),
)

@Composable
fun BabyVoiceTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}

