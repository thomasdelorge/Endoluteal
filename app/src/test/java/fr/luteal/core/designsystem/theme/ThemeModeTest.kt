package fr.luteal.core.designsystem.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeModeTest {
    @Test
    fun systemFollowsTheDevice() {
        assertTrue(resolveDarkTheme("SYSTEM", systemDark = true))
        assertFalse(resolveDarkTheme("SYSTEM", systemDark = false))
    }

    @Test
    fun explicitChoiceIgnoresTheDevice() {
        assertFalse(resolveDarkTheme("LIGHT", systemDark = true))
        assertTrue(resolveDarkTheme("DARK", systemDark = false))
    }

    @Test
    fun appearanceChoiceSplitsModeAndPalette() {
        assertEquals("LIGHT" to "BLUE", splitAppearanceChoice("LIGHT|BLUE"))
    }

    @Test
    fun unknownModeFallsBackToTheDevice() {
        assertTrue(resolveDarkTheme("NOPE", systemDark = true))
    }

    @Test
    fun namedPaletteChangesThePrimaryColor() {
        val evergreen = colorSchemeFor("EVERGREEN", dark = false).primary
        val blue = colorSchemeFor("BLUE", dark = false).primary
        assertTrue(evergreen != blue)
        assertEquals(evergreen, colorSchemeFor("NOPE", dark = false).primary)
    }
}
