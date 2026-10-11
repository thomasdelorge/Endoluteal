package fr.luteal.core.designsystem.component.bodymap

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import fr.luteal.core.model.BodySide
import fr.luteal.core.model.BodyZone

data class ZoneShape(
    val zone: BodyZone,
    val rect: Rect
)

fun ZoneShape.toPath(): Path = Path().apply { addRect(rect) }

object BodyMapGeometry {
    const val VIEWBOX_WIDTH = 100f
    const val VIEWBOX_HEIGHT = 240f

    private val front = listOf(
        box(BodyZone.HEAD, 38f, 4f, 62f, 26f),
        box(BodyZone.NECK, 44f, 26f, 56f, 36f),
        box(BodyZone.CHEST_LEFT, 26f, 36f, 48f, 60f),
        box(BodyZone.CHEST_RIGHT, 52f, 36f, 74f, 60f),
        box(BodyZone.HYPOCHONDRIUM_LEFT, 26f, 60f, 40f, 76f),
        box(BodyZone.EPIGASTRIUM, 40f, 60f, 60f, 76f),
        box(BodyZone.HYPOCHONDRIUM_RIGHT, 60f, 60f, 74f, 76f),
        box(BodyZone.FLANK_LEFT, 22f, 76f, 38f, 98f),
        box(BodyZone.PERIUMBILICAL, 38f, 76f, 62f, 98f),
        box(BodyZone.FLANK_RIGHT, 62f, 76f, 78f, 98f),
        box(BodyZone.ILIAC_FOSSA_LEFT, 26f, 98f, 42f, 120f),
        box(BodyZone.HYPOGASTRIUM, 42f, 98f, 58f, 120f),
        box(BodyZone.ILIAC_FOSSA_RIGHT, 58f, 98f, 74f, 120f),
        box(BodyZone.INGUINAL_LEFT, 26f, 120f, 42f, 134f),
        box(BodyZone.VULVA, 42f, 120f, 58f, 136f),
        box(BodyZone.INGUINAL_RIGHT, 58f, 120f, 74f, 134f),
        box(BodyZone.THIGH_FRONT_LEFT, 26f, 136f, 46f, 172f),
        box(BodyZone.THIGH_FRONT_RIGHT, 54f, 136f, 74f, 172f),
        box(BodyZone.KNEE_LEFT, 28f, 172f, 46f, 188f),
        box(BodyZone.KNEE_RIGHT, 54f, 172f, 72f, 188f),
        box(BodyZone.LEG_FRONT_LEFT, 30f, 188f, 46f, 232f),
        box(BodyZone.LEG_FRONT_RIGHT, 54f, 188f, 70f, 232f)
    )

    private val back = listOf(
        box(BodyZone.NAPE, 42f, 4f, 58f, 22f),
        box(BodyZone.SHOULDER_LEFT, 18f, 22f, 46f, 44f),
        box(BodyZone.SHOULDER_RIGHT, 54f, 22f, 82f, 44f),
        box(BodyZone.UPPER_BACK, 38f, 44f, 62f, 80f),
        box(BodyZone.LUMBAR_LEFT, 26f, 80f, 48f, 106f),
        box(BodyZone.LUMBAR_RIGHT, 52f, 80f, 74f, 106f),
        box(BodyZone.SACRUM, 40f, 106f, 60f, 122f),
        box(BodyZone.BUTTOCK_LEFT, 24f, 122f, 48f, 150f),
        box(BodyZone.BUTTOCK_RIGHT, 52f, 122f, 76f, 150f),
        box(BodyZone.COCCYX, 44f, 150f, 56f, 162f),
        box(BodyZone.THIGH_BACK_LEFT, 26f, 162f, 46f, 198f),
        box(BodyZone.THIGH_BACK_RIGHT, 54f, 162f, 74f, 198f),
        box(BodyZone.CALF_LEFT, 30f, 198f, 46f, 234f),
        box(BodyZone.CALF_RIGHT, 54f, 198f, 70f, 234f)
    )

    fun shapes(side: BodySide): List<ZoneShape> = when (side) {
        BodySide.FRONT -> front
        BodySide.BACK -> back
        BodySide.INTERNAL -> emptyList()
    }

    fun zoneAt(point: Offset, side: BodySide, size: Size): BodyZone? {
        if (side == BodySide.INTERNAL || size.width <= 0f || size.height <= 0f) return null
        val local = Offset(
            x = point.x / size.width * VIEWBOX_WIDTH,
            y = point.y / size.height * VIEWBOX_HEIGHT
        )
        return shapes(side).firstOrNull { it.rect.contains(local) }?.zone
    }

    private fun box(zone: BodyZone, left: Float, top: Float, right: Float, bottom: Float) =
        ZoneShape(zone, Rect(left, top, right, bottom))
}
