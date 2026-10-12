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
    palette: String = "EVERGREEN",
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalPhaseColors provides if (darkTheme) DarkPhaseColors else LightPhaseColors
    ) {
        MaterialTheme(
            colorScheme = colorSchemeFor(palette, darkTheme),
            typography = LutealTypography,
            shapes = LutealShapes,
            content = content
        )
    }
}
