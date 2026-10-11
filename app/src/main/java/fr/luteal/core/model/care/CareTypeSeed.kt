package fr.luteal.core.model.care

object CareTypeSeed {
    fun defaults(): List<CareType> = listOf(
        CareType("care-tens", "TENS", emptyList(), false),
        CareType("care-hot-bottle", "Bouillotte", emptyList(), false),
        CareType("care-hot-bath", "Bain chaud", emptyList(), false),
        CareType("care-physio", "Kinésithérapie", emptyList(), false),
        CareType("care-osteo", "Ostéopathie", emptyList(), false),
        CareType("care-stretch", "Étirements", emptyList(), false)
    )
}
