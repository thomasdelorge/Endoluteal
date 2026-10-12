package fr.luteal.core.designsystem.component.bodymap

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import fr.luteal.core.model.BodySide
import fr.luteal.core.model.PainMark
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PainPinGeometryTest {

    @Test
    fun normalizedPinClampsToUnitSquare() {
        val (x, y) = PainPinGeometry.normalizedPin(Offset(50f, 100f), Size(100f, 200f))
        assertEquals(0.5f, x)
        assertEquals(0.5f, y)
    }

    @Test
    fun nearestPinRespectsHitRadius() {
        val mark = PainMark(
            id = "p1",
            zone = "f01",
            intensity = 4,
            painTypes = emptyList(),
            pinSide = PainPinGeometry.PIN_SIDE_FRONT,
            pinX = 0.5f,
            pinY = 0.5f
        )
        val size = Size(100f, 100f)
        val center = Offset(50f, 50f)
        assertEquals(mark, PainPinGeometry.nearestPin(listOf(mark), BodySide.FRONT, center, size, 10f))
        assertNull(PainPinGeometry.nearestPin(listOf(mark), BodySide.FRONT, Offset(80f, 50f), size, 10f))
        assertNull(PainPinGeometry.nearestPin(listOf(mark), BodySide.BACK, center, size, 10f))
    }

    @Test
    fun editRadiusAllowsNearbySecondPinInSameZone() {
        val mark = PainMark(
            id = "p1",
            zone = "f08",
            intensity = 4,
            painTypes = emptyList(),
            pinSide = PainPinGeometry.PIN_SIDE_FRONT,
            pinX = 0.5f,
            pinY = 0.5f
        )
        val size = Size(100f, 100f)
        val radius = PainPinGeometry.PIN_EDIT_HIT_RADIUS_DP
        assertEquals(mark, PainPinGeometry.nearestPin(listOf(mark), BodySide.FRONT, Offset(55f, 50f), size, radius))
        assertNull(PainPinGeometry.nearestPin(listOf(mark), BodySide.FRONT, Offset(70f, 50f), size, radius))
        val second = mark.copy(id = "p2", pinX = 0.7f, pinY = 0.5f)
        assertEquals(2, listOf(mark, second).count { it.zone == "f08" && it.isPin })
    }
}
