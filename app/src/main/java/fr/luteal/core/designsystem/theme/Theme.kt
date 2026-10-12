package fr.luteal.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

fun resolveDarkTheme(mode: String, systemDark: Boolean): Boolean = when (mode) {
    "LIGHT" -> false
    "DARK" -> true
    else -> systemDark
}

@Composable
fun LutealTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalPhaseColors provides if (darkTheme) DarkPhaseColors else LightPhaseColors
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = LutealTypography,
            shapes = LutealShapes,
            content = content
        )
    }
}
