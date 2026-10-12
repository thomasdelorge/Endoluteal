package fr.luteal.core.calendar

object CalendarEventFilter {
    fun matches(title: String, description: String, keywords: List<String>): Boolean {
        if (keywords.isEmpty()) return false
        val haystack = buildString {
            append(title)
            append('\n')
            append(description)
        }.lowercase()
        return keywords.any { keyword ->
            val trimmed = keyword.trim()
            trimmed.isNotEmpty() && haystack.contains(trimmed.lowercase())
        }
    }

    fun parseKeywords(raw: String): List<String> =
        raw.split(',', '\n', ';')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
}
