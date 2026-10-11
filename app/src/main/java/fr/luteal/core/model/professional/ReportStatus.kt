package fr.luteal.core.model.professional

enum class ReportStatus {
    PENDING,
    DONE;

    companion object {
        fun fromStored(value: String): ReportStatus = entries.find { it.name == value } ?: PENDING
    }
}
