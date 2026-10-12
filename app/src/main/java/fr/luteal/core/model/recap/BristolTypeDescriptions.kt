package fr.luteal.core.model.recap

import androidx.annotation.StringRes
import fr.luteal.app.R

/**
 * Bristol stool scale labels for recap (descriptive, no illustration in v1).
 */
object BristolTypeDescriptions {
    fun label(type: Int): String = when (type) {
        1 -> "Type 1 — granules dures"
        2 -> "Type 2 — grumeleux"
        3 -> "Type 3 — surface craquelée"
        4 -> "Type 4 — lisse et souple"
        5 -> "Type 5 — morceaux mous"
        6 -> "Type 6 — pâteuse"
        7 -> "Type 7 — liquide"
        else -> "Type $type"
    }

    @StringRes
    fun descriptionResId(type: Int): Int = when (type) {
        1 -> R.string.bristol_desc_1
        2 -> R.string.bristol_desc_2
        3 -> R.string.bristol_desc_3
        4 -> R.string.bristol_desc_4
        5 -> R.string.bristol_desc_5
        6 -> R.string.bristol_desc_6
        7 -> R.string.bristol_desc_7
        else -> 0
    }
}
