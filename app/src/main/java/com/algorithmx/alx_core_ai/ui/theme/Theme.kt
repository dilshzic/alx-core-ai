package com.algorithmx.alx_core_ai.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
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
    primary = Color(0xFF2D4C3B),
    onPrimary = Color(0xFFF9F5ED),
    secondary = Color(0xFFD6895D),
    onSecondary = Color(0xFF2B1B12),
    tertiary = Color(0xFF6C8C74),
    background = Color(0xFFF8F2EA),
    onBackground = Color(0xFF1E1B16),
    surface = Color(0xFFFCFAF6),
    onSurface = Color(0xFF1E1B16),
    error = Color(0xFFB44B3A)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF89B59C),
    onPrimary = Color(0xFF0F1D17),
    secondary = Color(0xFFE2A578),
    onSecondary = Color(0xFF2A1B11),
    tertiary = Color(0xFF9CB9A3),
    background = Color(0xFF101613),
    onBackground = Color(0xFFECE6DD),
    surface = Color(0xFF151C18),
    onSurface = Color(0xFFECE6DD),
    error = Color(0xFFE08D7F)
)

private val AppTypography = androidx.compose.material3.Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 48.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    )
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        content = content
    )
}
