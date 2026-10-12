package fr.luteal.core.designsystem.component.bodymap

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import fr.luteal.core.model.BodySide

object BodySilhouettePaths {
    fun path(side: BodySide): Path = when (side) {
        BodySide.FRONT -> front()
        BodySide.BACK -> back()
        BodySide.INTERNAL -> Path()
    }

    private fun front(): Path = Path().apply {
        addOval(Rect(38f, 4f, 62f, 28f))
        addRect(Rect(44f, 26f, 56f, 38f))
        moveTo(30f, 38f)
        lineTo(70f, 38f)
        lineTo(76f, 72f)
        lineTo(72f, 118f)
        lineTo(58f, 120f)
        lineTo(58f, 168f)
        lineTo(52f, 232f)
        lineTo(46f, 232f)
        lineTo(42f, 168f)
        lineTo(42f, 120f)
        lineTo(28f, 118f)
        lineTo(24f, 72f)
        close()
    }

    private fun back(): Path = Path().apply {
        addOval(Rect(40f, 4f, 60f, 24f))
        moveTo(28f, 24f)
        lineTo(72f, 24f)
        lineTo(78f, 48f)
        lineTo(64f, 80f)
        lineTo(64f, 108f)
        lineTo(58f, 122f)
        lineTo(58f, 162f)
        lineTo(52f, 234f)
        lineTo(48f, 234f)
        lineTo(42f, 162f)
        lineTo(42f, 122f)
        lineTo(36f, 108f)
        lineTo(36f, 80f)
        lineTo(22f, 48f)
        close()
    }
}
