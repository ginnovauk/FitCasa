package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = AthleticGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = AthleticGreenDark,
    secondary = AthleticBlueSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDBEAFE),
    onSecondaryContainer = AthleticBlueDark,
    tertiary = EnergyOrange,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFEDD5),
    onTertiaryContainer = Color(0xFF9A3412),
    background = CleanBackground,
    onBackground = TextPrimary,
    surface = CleanSurface,
    onSurface = TextPrimary,
    surfaceVariant = CleanSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = CleanBorder,
    outlineVariant = Color(0xFFCBD5E1)
)

private val DarkColorScheme = darkColorScheme(
    primary = AthleticGreenLight,
    onPrimary = Color(0xFF022C22),
    primaryContainer = AthleticGreenDark,
    onPrimaryContainer = Color(0xFFA7F3D0),
    secondary = AthleticBlueLight,
    onSecondary = Color(0xFF1E3A8A),
    secondaryContainer = AthleticBlueDark,
    onSecondaryContainer = Color(0xFFBFDBFE),
    tertiary = EnergyOrangeLight,
    onTertiary = Color(0xFF431407),
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = Color(0xFF334155),
    outlineVariant = Color(0xFF1E293B)
)

@Composable
fun FitCasaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // We emphasize the crisp clean athletic theme chosen by the user
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    FitCasaTheme(darkTheme = darkTheme, content = content)
}
