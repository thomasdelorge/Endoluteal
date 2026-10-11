package fr.luteal.core.model.professional

data class Professional(
    val id: String,
    val name: String,
    val specialty: Specialty,
    val customSpecialty: String,
    val place: String,
    val phone: String,
    val notes: String
) {
    fun displaySpecialty(): String =
        if (specialty == Specialty.OTHER && customSpecialty.isNotBlank()) customSpecialty else specialty.label
}
