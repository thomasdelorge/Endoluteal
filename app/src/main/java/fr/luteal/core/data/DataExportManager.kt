package fr.luteal.core.data

import fr.luteal.core.data.datastore.UserPreferencesDataStore
import fr.luteal.core.data.local.BiomarkerDao
import fr.luteal.core.data.local.CycleDao
import fr.luteal.core.data.local.DailyEntryDao
import fr.luteal.core.data.local.JournalEventDao
import fr.luteal.core.data.local.MedicationDao
import fr.luteal.core.data.local.SymptomDao
import fr.luteal.core.model.BiomarkerBackupDto
import fr.luteal.core.model.CompositionBackupDto
import fr.luteal.core.model.CycleBackupDto
import fr.luteal.core.model.DailyEntryBackupDto
import fr.luteal.core.model.EndolutealBackup
import fr.luteal.core.model.InteractionRuleBackupDto
import fr.luteal.core.model.JournalEventBackupDto
import fr.luteal.core.model.LutealBackupPayload
import fr.luteal.core.model.MedicationBackupDto
import fr.luteal.core.model.PainMarkBackupDto
import fr.luteal.core.model.PeriodDayBackupDto
import fr.luteal.core.model.SubstanceBackupDto
import fr.luteal.core.model.SymptomLogBackupDto
import fr.luteal.core.model.UserPreferencesBackupDto
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.json.JSONArray
import java.io.OutputStream
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataExportManager @Inject constructor(
    private val cycleDao: CycleDao,
    private val dailyEntryDao: DailyEntryDao,
    private val symptomDao: SymptomDao,
    private val biomarkerDao: BiomarkerDao,
    private val journalEventDao: JournalEventDao,
    private val medicationDao: MedicationDao,
    private val userPreferencesDataStore: UserPreferencesDataStore
) {
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    suspend fun createBackupPayload(appVersion: String = "1.3.0"): LutealBackupPayload {
        val cycles = cycleDao.getAllCyclesOnce().map { entity ->
            val periodDays = mutableListOf<PeriodDayBackupDto>()
            if (entity.periodDaysJson.isNotBlank()) {
                runCatching {
                    val arr = JSONArray(entity.periodDaysJson)
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val symptomsList = mutableListOf<String>()
                        val symptomsArr = obj.optJSONArray("symptomIds")
                        if (symptomsArr != null) {
                            for (j in 0 until symptomsArr.length()) {
                                symptomsList.add(symptomsArr.getString(j))
                            }
                        }
                        periodDays.add(
                            PeriodDayBackupDto(
                                date = obj.getString("date"),
                                bleedingIntensity = obj.getString("bleedingIntensity"),
                                notes = obj.optString("notes", ""),
                                symptomIds = symptomsList
                            )
                        )
                    }
                }
            }
            CycleBackupDto(
                id = entity.id,
                startDate = entity.startDate,
                endDate = entity.endDate,
                averageLengthDays = entity.averageLengthDays,
                lutealPhaseLengthDays = entity.lutealPhaseLengthDays,
                periodDays = periodDays,
                isExcludedFromEstimates = entity.isExcludedFromEstimates,
                exclusionReason = entity.exclusionReason
            )
        }

        val dailyEntries = dailyEntryDao.getAllEntriesOnce().map { entity ->
            val symptomIds = mutableListOf<String>()
            if (entity.symptomIdsJson.isNotBlank()) {
                runCatching {
                    val arr = JSONArray(entity.symptomIdsJson)
                    for (i in 0 until arr.length()) {
                        symptomIds.add(arr.getString(i))
                    }
                }
            }
            DailyEntryBackupDto(
                date = entity.date,
                bleedingIntensity = entity.bleedingIntensity,
                painLevel = entity.painLevel,
                moodLevel = entity.moodLevel,
                energyLevel = entity.energyLevel,
                symptomIds = symptomIds,
                notes = entity.notes,
                updatedAt = Instant.ofEpochMilli(entity.updatedAtEpochMillis).toString()
            )
        }

        val symptomLogs = symptomDao.getAllSymptomLogsOnce().map { entity ->
            SymptomLogBackupDto(
                id = entity.id,
                timestamp = Instant.ofEpochMilli(entity.timestampEpochMillis).toString(),
                date = entity.date,
                symptomId = entity.symptomId,
                severity = entity.severity,
                notes = entity.notes
            )
        }

        val biomarkerObservations = biomarkerDao.getAllObservationsOnce().map { entity ->
            val disturbances = mutableListOf<String>()
            if (entity.bbtDisturbancesJson.isNotBlank()) {
                runCatching {
                    val arr = JSONArray(entity.bbtDisturbancesJson)
                    for (i in 0 until arr.length()) {
                        disturbances.add(arr.getString(i))
                    }
                }
            }
            BiomarkerBackupDto(
                date = entity.date,
                bbtCelsius = entity.bbtCelsius,
                bbtTime = entity.bbtTime,
                bbtQuality = entity.bbtQuality,
                bbtDisturbances = disturbances,
                cervicalSensation = entity.cervicalSensation,
                cervicalTexture = entity.cervicalTexture,
                lhTestResult = entity.lhTestResult,
                hcgTestResult = entity.hcgTestResult,
                notes = entity.notes,
                updatedAt = Instant.ofEpochMilli(entity.updatedAtEpochMillis).toString()
            )
        }

        val journalEvents = journalEventDao.getAllOnce().map { entity ->
            JournalEventBackupDto(
                id = entity.id,
                type = entity.type,
                startAt = Instant.ofEpochMilli(entity.startAt).toString(),
                endAt = entity.endAt?.let { Instant.ofEpochMilli(it).toString() },
                zoneId = entity.zoneId,
                notes = entity.notes,
                linkedEventId = entity.linkedEventId,
                payload = json.parseToJsonElement(entity.payloadJson),
                createdAt = Instant.ofEpochMilli(entity.createdAt).toString(),
                updatedAt = Instant.ofEpochMilli(entity.updatedAt).toString()
            )
        }
        val painMarks = journalEventDao.getAllMarksOnce().map { entity ->
            PainMarkBackupDto(
                id = entity.id,
                eventId = entity.eventId,
                zone = entity.zone,
                intensity = entity.intensity,
                painTypes = entity.painTypes.toStringList()
            )
        }
        val compositions = medicationDao.compositionsOnce().groupBy { it.medicationId }
        val substances = medicationDao.substancesOnce().map { entity ->
            SubstanceBackupDto(
                id = entity.id,
                name = entity.name,
                drugClass = entity.drugClass,
                maxDailyMg = entity.maxDailyMg,
                maxPerIntakeMg = entity.maxPerIntakeMg,
                minIntervalMinutes = entity.minIntervalMinutes,
                validated = entity.validated,
                notes = entity.notes
            )
        }
        val medications = medicationDao.medicationsOnce().map { entity ->
            MedicationBackupDto(
                id = entity.id,
                name = entity.name,
                form = entity.form,
                contraindications = entity.contraindications,
                archived = entity.archived,
                compositions = compositions[entity.id].orEmpty().map { row ->
                    CompositionBackupDto(row.substanceId, row.amountMgPerUnit)
                }
            )
        }
        val interactionRules = medicationDao.rulesOnce().map { entity ->
            InteractionRuleBackupDto(
                id = entity.id,
                targetAType = entity.targetAType,
                targetAId = entity.targetAId,
                targetBType = entity.targetBType,
                targetBId = entity.targetBId,
                severity = entity.severity,
                message = entity.message
            )
        }
        val prefs = userPreferencesDataStore.userPreferencesFlow.first()
        val preferencesDto = UserPreferencesBackupDto(
            userRole = prefs.userRole,
            locale = prefs.locale,
            trackPmdd = prefs.trackPmdd,
            trackPms = prefs.trackPms,
            trackEndometriosis = prefs.trackEndometriosis,
            trackPcos = prefs.trackPcos,
            trackPerimenopause = prefs.trackPerimenopause,
            trackThyroid = prefs.trackThyroid,
            ageBand = prefs.ageBand,
            temperatureUnit = prefs.temperatureUnit
        )

        return LutealBackupPayload(
            backupFormat = EndolutealBackup.FORMAT,
            schemaVersion = 1,
            exportedAt = Instant.now().toString(),
            appVersion = appVersion,
            cycles = cycles,
            dailyEntries = dailyEntries,
            symptomLogs = symptomLogs,
            biomarkerObservations = biomarkerObservations,
            journalEvents = journalEvents,
            painMarks = painMarks,
            substances = substances,
            medications = medications,
            interactionRules = interactionRules,
            preferences = preferencesDto
        )
    }

    private fun String.toStringList(): List<String> = runCatching {
        val array = JSONArray(this)
        List(array.length()) { index -> array.getString(index) }
    }.getOrDefault(emptyList())

    suspend fun exportToJsonString(appVersion: String = "1.3.0"): String {
        val payload = createBackupPayload(appVersion)
        return json.encodeToString(payload)
    }

    suspend fun exportToStream(outputStream: OutputStream, appVersion: String = "1.3.0") {
        val jsonString = exportToJsonString(appVersion)
        outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
            writer.write(jsonString)
            writer.flush()
        }
    }
}
