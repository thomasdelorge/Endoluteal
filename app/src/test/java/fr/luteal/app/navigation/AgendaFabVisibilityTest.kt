package fr.luteal.app.navigation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AgendaFabVisibilityTest {

    @Test
    fun hidesAtScrollEndWhenListOverflows() {
        assertTrue(hideAgendaFab(canScrollBackward = true, canScrollForward = false, fabExpanded = false))
    }

    @Test
    fun staysVisibleWhenMenuOpen() {
        assertFalse(hideAgendaFab(canScrollBackward = true, canScrollForward = false, fabExpanded = true))
    }

    @Test
    fun staysVisibleWhenContentFits() {
        assertFalse(hideAgendaFab(canScrollBackward = false, canScrollForward = false, fabExpanded = false))
    }
}
