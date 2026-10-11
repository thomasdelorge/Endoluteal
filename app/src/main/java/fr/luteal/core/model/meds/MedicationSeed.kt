package fr.luteal.core.model.meds

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class MedicationSeedFile(
    val substances: List<SeedSubstance> = emptyList(),
    val medications: List<SeedMedication> = emptyList(),
    val rules: List<SeedRule> = emptyList()
)

@Serializable
data class SeedSubstance(
    val id: String,
    val name: String,
    @SerialName("drug_class") val drugClass: String,
    @SerialName("max_daily_mg") val maxDailyMg: Double? = null,
    @SerialName("max_per_intake_mg") val maxPerIntakeMg: Double? = null,
    @SerialName("min_interval_minutes") val minIntervalMinutes: Int? = null,
    val notes: String = ""
)

@Serializable
data class SeedMedication(
    val id: String,
    val name: String,
    val form: String,
    val contraindications: String = "",
    val compositions: List<SeedComposition> = emptyList()
)

@Serializable
data class SeedComposition(
    @SerialName("substance_id") val substanceId: String,
    @SerialName("amount_mg_per_unit") val amountMgPerUnit: Double
)

@Serializable
data class SeedRule(
    val id: String,
    @SerialName("target_a_type") val targetAType: String,
    @SerialName("target_a_id") val targetAId: String,
    @SerialName("target_b_type") val targetBType: String,
    @SerialName("target_b_id") val targetBId: String,
    val severity: String,
    val message: String
)

object MedicationSeed {
    private val json = Json { ignoreUnknownKeys = true }

    fun parse(raw: String): MedicationCatalog {
        val file = json.decodeFromString(MedicationSeedFile.serializer(), raw)
        return MedicationCatalog(
            substances = file.substances.map { seed ->
                Substance(
                    id = seed.id,
                    name = seed.name,
                    drugClass = DrugClass.valueOf(seed.drugClass),
                    maxDailyMg = seed.maxDailyMg,
                    maxPerIntakeMg = seed.maxPerIntakeMg,
                    minIntervalMinutes = seed.minIntervalMinutes,
                    validated = false,
                    notes = seed.notes
                )
            },
            medications = file.medications.map { seed ->
                Medication(
                    id = seed.id,
                    name = seed.name,
                    form = seed.form,
                    contraindications = seed.contraindications,
                    archived = false,
                    compositions = seed.compositions.map {
                        Composition(it.substanceId, it.amountMgPerUnit)
                    }
                )
            },
            rules = file.rules.map { seed ->
                InteractionRule(
                    id = seed.id,
                    targetAType = InteractionTargetType.valueOf(seed.targetAType),
                    targetAId = seed.targetAId,
                    targetBType = InteractionTargetType.valueOf(seed.targetBType),
                    targetBId = seed.targetBId,
                    severity = AlertSeverity.valueOf(seed.severity),
                    message = seed.message
                )
            }
        )
    }
}
