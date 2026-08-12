package com.seungsu.ohmysubway.design.compose.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

val LiteColorScheme = lightColorScheme(
    primaryContainer = Color.White, onPrimaryContainer = Grey90,
    surface = Color.White, onSurface = Grey90
)
val DarkColorScheme = darkColorScheme(
    primaryContainer = Grey90, onPrimaryContainer = Grey05,
    surface = Grey90, onSurface = Grey05
)

@Composable
fun OhMySubwayTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colorScheme = if (darkTheme) DarkColorScheme else LiteColorScheme
    val colors = if (darkTheme) DarkOhMySubwayColors else OhMySubwayColors()
    CompositionLocalProvider(
        LocalDensity provides Density(density = LocalDensity.current.density, fontScale = 1f),
        LocalOhMySubwayColors provides colors,
        LocalOhMySubwayTypography provides OhMySubwayTypography()
    ) {
        MaterialTheme(colorScheme = colorScheme) { content() }
    }
}

object OhMySubwayTheme {
    val colors: OhMySubwayColors @Composable get() = LocalOhMySubwayColors.current
    val typos: OhMySubwayTypography @Composable get() = LocalOhMySubwayTypography.current
}
