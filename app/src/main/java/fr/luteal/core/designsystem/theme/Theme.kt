package fr.luteal.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

fun splitAppearanceChoice(value: String): Pair<String, String> {
    val parts = value.split("|", limit = 2)
    val mode = parts.getOrElse(0) { "SYSTEM" }.ifBlank { "SYSTEM" }
    val palette = parts.getOrElse(1) { "EVERGREEN" }.ifBlank { "EVERGREEN" }
    return mode to palette
}

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
