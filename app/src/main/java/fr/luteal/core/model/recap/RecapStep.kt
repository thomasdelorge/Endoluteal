package fr.luteal.core.model.recap

enum class RecapStep {
    CYCLE,
    PAIN,
    PSY,
    GENERAL,
    BOWEL;

    companion object {
        val ordered: List<RecapStep> = entries
    }
}
