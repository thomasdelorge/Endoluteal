package fr.luteal.core.designsystem.component.bodymap

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import fr.luteal.core.model.BodySide
import fr.luteal.core.model.PainMark
import kotlin.math.hypot

object PainPinGeometry {
    const val PIN_SIDE_FRONT = "front"
    const val PIN_SIDE_BACK = "back"

    /** Tap radius to select an existing pin for edit (not for placing a new one). */
    const val PIN_EDIT_HIT_RADIUS_DP = 14f

    fun sideKey(side: BodySide): String =
        if (side == BodySide.FRONT) PIN_SIDE_FRONT else PIN_SIDE_BACK

    fun normalizedPin(offset: Offset, canvasSize: Size): Pair<Float, Float> =
        Pair(
            (offset.x / canvasSize.width).coerceIn(0f, 1f),
            (offset.y / canvasSize.height).coerceIn(0f, 1f)
        )

    fun canvasOffset(mark: PainMark, canvasSize: Size): Offset? {
        val x = mark.pinX ?: return null
        val y = mark.pinY ?: return null
        return Offset(x * canvasSize.width, y * canvasSize.height)
    }

    fun nearestPin(
        marks: List<PainMark>,
        side: BodySide,
        tap: Offset,
        canvasSize: Size,
        hitRadiusPx: Float
    ): PainMark? {
        val sideKey = sideKey(side)
        return marks
            .filter { it.pinSide == sideKey && it.isPin }
            .minByOrNull { mark ->
                val center = canvasOffset(mark, canvasSize) ?: return@minByOrNull Float.MAX_VALUE
                hypot(center.x - tap.x, center.y - tap.y)
            }
            ?.takeIf { mark ->
                val center = canvasOffset(mark, canvasSize) ?: return@takeIf false
                hypot(center.x - tap.x, center.y - tap.y) <= hitRadiusPx
            }
    }
}
