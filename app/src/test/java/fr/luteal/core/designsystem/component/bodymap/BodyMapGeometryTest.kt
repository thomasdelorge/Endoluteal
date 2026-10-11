package fr.luteal.core.designsystem.component.bodymap

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import fr.luteal.core.model.BodySide
import fr.luteal.core.model.BodyZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BodyMapGeometryTest {

    private val size = Size(200f, 480f)

    @Test
    fun eachDrawnZoneContainsItsCenter() {
        BodySide.entries.filter { it != BodySide.INTERNAL }.forEach { side ->
            BodyMapGeometry.shapes(side).forEach { shape ->
                assertEquals(shape.zone, BodyMapGeometry.zoneAt(shape.centerOn(size), side, size))
            }
        }
    }

    @Test
    fun emptySpaceAndInternalSideMiss() {
        assertNull(BodyMapGeometry.zoneAt(Offset.Zero, BodySide.FRONT, size))
        assertNull(BodyMapGeometry.zoneAt(Offset.Zero, BodySide.BACK, size))
        val head = BodyMapGeometry.shapes(BodySide.FRONT).first { it.zone == BodyZone.HEAD }
        assertNotEquals(BodyZone.HEAD, BodyMapGeometry.zoneAt(head.centerOn(size), BodySide.BACK, size))
        assertNull(BodyMapGeometry.zoneAt(head.centerOn(size), BodySide.INTERNAL, size))
    }

    private fun ZoneShape.centerOn(size: Size): Offset {
        val center = rect.center
        return Offset(
            x = center.x / BodyMapGeometry.VIEWBOX_WIDTH * size.width,
            y = center.y / BodyMapGeometry.VIEWBOX_HEIGHT * size.height
        )
    }
}
