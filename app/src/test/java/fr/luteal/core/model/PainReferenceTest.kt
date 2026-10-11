package fr.luteal.core.model

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PainReferenceTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun bodyZoneIdsAreUniqueAndLabeled() {
        val ids = BodyZone.entries.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
        BodyZone.entries.forEach { zone ->
            assertTrue(context.getString(zone.labelRes).isNotBlank())
        }
    }

    @Test
    fun painTypeIdsAreUniqueAndLabeled() {
        val ids = PainType.entries.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
        PainType.entries.forEach { type ->
            assertTrue(context.getString(type.labelRes).isNotBlank())
        }
    }
}
