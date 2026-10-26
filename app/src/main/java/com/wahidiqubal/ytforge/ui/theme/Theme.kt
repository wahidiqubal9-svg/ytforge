package com.wahidiqubal.ytforge.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Typography

val Ink = Color(0xFFF7FBFF)
val Muted = Color(0xFF8AA0B8)
val Background = Color(0xFF07111F)
val SurfaceDeep = Color(0xFF0B1726)
val SurfaceCard = Color(0xFF0D1D30)
val SurfaceRaised = Color(0xFF122C44)
val Accent = Color(0xFF3EE6B0)
val AccentSoft = Color(0xFF14395C)
val OutlineDark = Color(0xFF1F3A55)
val Danger = Color(0xFFFF7A7A)

private val YTForgeColors = darkColorScheme(
    primary = Accent,
    onPrimary = Color(0xFF04110C),
    primaryContainer = AccentSoft,
    onPrimaryContainer = Ink,
    secondary = Color(0xFF7FD7FF),
    onSecondary = Color(0xFF04131C),
    background = Background,
    onBackground = Ink,
    surface = SurfaceDeep,
    onSurface = Ink,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = Muted,
    outline = OutlineDark,
    error = Danger,
)

private val YTForgeTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 38.sp,
        letterSpacing = (-1).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 31.sp,
        letterSpacing = (-0.5).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
    ),
)

@Composable
fun YTForgeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = YTForgeColors,
        typography = YTForgeTypography,
        content = content,
    )
}
