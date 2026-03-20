package com.example.vhackwallet.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CelestialBerry,
    secondary = CelestialPlum,
    tertiary = CelestialPink,
    background = CelestialDarkNavy,
    surface = CelestialDeepBlue,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = SoftGreyPurple,
    onSurface = SoftGreyPurple
)

private val LightColorScheme = lightColorScheme(
    primary = CelestialDarkPurple,
    secondary = CelestialMutedPurple,
    tertiary = CelestialBerry,
    background = SoftGreyPurple,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = CelestialDarkNavy,
    onSurface = CelestialDarkNavy
)

private val BluePurpleColorScheme = darkColorScheme(
    primary = BPAccent,
    secondary = BPVibrantPurple,
    tertiary = BPLightBlue,
    background = BPDeepBlue,
    surface = BPDeepPurple,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun VhackWalletTheme(
    themeMode: Int = 0, // 0: Light, 1: Dark, 2: Blue-Purple
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        1 -> DarkColorScheme
        2 -> BluePurpleColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
