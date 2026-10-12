package fr.luteal.core.model.recap

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

    fun description(type: Int): String = when (type) {
        1 -> "Petites boules dures, difficiles à évacuer."
        2 -> "Forme de saucisse mais grumeleuse."
        3 -> "Saucisse avec des fissures en surface."
        4 -> "Saucisse lisse et souple, facile à passer."
        5 -> "Morceaux mous aux bords nets."
        6 -> "Morceaux pâteux aux bords irréguliers."
        7 -> "Entièrement liquide, sans morceaux solides."
        else -> ""
    }
}
