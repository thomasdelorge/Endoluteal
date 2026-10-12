package fr.luteal.core.designsystem.component.bodymap

import android.content.Context
import androidx.compose.ui.geometry.Offset

/**
 * CHOIR Bodymap, female, Stanford University.
 * Devised by Ming-Chih J. Kao and Sean Mackey (CHOIR).
 * Attribution required: https://choir.stanford.edu
 * Region polygons from the REDCap imagemap module (painmap_female).
 */
data class ChoirRegion(
    val key: String,
    val title: String,
    val front: Boolean,
    val points: List<Offset>
)

object ChoirPainMap {
    const val IMAGE_WIDTH = 518f
    const val IMAGE_HEIGHT = 580f

    @Volatile
    private var cached: List<ChoirRegion>? = null

    fun regions(context: Context): List<ChoirRegion> {
        cached?.let { return it }
        val parsed = context.assets.open("choir_painmap_female.txt").bufferedReader().use { reader ->
            reader.lineSequence().filter { it.isNotBlank() }.map { line ->
                val parts = line.split("|", limit = 3)
                val nums = parts[2].split(",").map { it.toFloat() }
                val points = nums.chunked(2).map { Offset(it[0], it[1]) }
                ChoirRegion(parts[0], parts[1], parts[0].startsWith("f"), points)
            }.toList()
        }
        cached = parsed
        return parsed
    }

    fun hit(x: Float, y: Float, front: Boolean, regions: List<ChoirRegion>): ChoirRegion? =
        regions.lastOrNull { region ->
            region.front == front && contains(x, y, region.points)
        }

    private fun contains(x: Float, y: Float, points: List<Offset>): Boolean {
        var inside = false
        var j = points.lastIndex
        for (i in points.indices) {
            val current = points[i]
            val previous = points[j]
            val intersect = (current.y > y) != (previous.y > y) &&
                x < (previous.x - current.x) * (y - current.y) / (previous.y - current.y) + current.x
            if (intersect) inside = !inside
            j = i
        }
        return inside
    }
}
