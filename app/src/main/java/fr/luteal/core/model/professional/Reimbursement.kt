package fr.luteal.core.model.professional

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

enum class ReimbursementStatus {
    UNSPECIFIED,
    IN_PROGRESS,
    NO,
    DONE;

    companion object {
        fun fromStored(value: String?): ReimbursementStatus =
            entries.find { it.name == value } ?: UNSPECIFIED
    }
}

fun remainingChargeCents(feeCents: Int?, cpamCents: Int?, mutuelleCents: Int?): Int? {
    val fee = feeCents ?: return null
    return fee - (cpamCents ?: 0) - (mutuelleCents ?: 0)
}

fun parseEurosToCents(raw: String): Int? {
    val trimmed = raw.trim().replace(',', '.')
    if (trimmed.isEmpty()) return null
    val value = trimmed.toBigDecimalOrNull() ?: return null
    return value.movePointRight(2).setScale(0, RoundingMode.HALF_UP).intValueExact()
}

fun formatCents(cents: Int): String {
    val euros = BigDecimal(cents).movePointLeft(2)
    return String.format(Locale.FRANCE, "%.2f", euros)
}
