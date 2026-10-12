package fr.luteal.core.designsystem.theme

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
    fun unknownModeFallsBackToTheDevice() {
        assertTrue(resolveDarkTheme("NOPE", systemDark = true))
    }
}
