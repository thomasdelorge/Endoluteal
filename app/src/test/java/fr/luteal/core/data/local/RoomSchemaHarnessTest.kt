package fr.luteal.core.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RoomSchemaHarnessTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        LutealDatabase::class.java,
    )

    @Test
    fun latestSchemaCreatesAndValidates() {
        helper.createDatabase(TEST_DB, 10).use { db ->
            db.query("SELECT count(*) FROM questionnaire_results").use { cursor ->
                assertTrue(cursor.moveToFirst())
            }
        }
        helper.runMigrationsAndValidate(TEST_DB, 10, true).close()
    }

    @Test
    fun migratesAppointmentsFrom11To12() {
        helper.createDatabase(MIGRATION_DB, 11).use { db ->
            db.execSQL(
                """
                INSERT INTO journal_events (
                    id, type, startAt, endAt, zoneId, notes, linkedEventId, payloadJson, createdAt, updatedAt
                ) VALUES ('evt', 'APPOINTMENT', 1, NULL, 'Europe/Paris', 'titre', NULL, '{}', 1, 1)
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO appointments (
                    eventId, professionalId, calendarEventKey, location, report, reportStatus
                ) VALUES ('evt', 'pro', NULL, 'Cabinet', 'CR', 'PENDING')
                """.trimIndent()
            )
        }
        helper.runMigrationsAndValidate(MIGRATION_DB, 12, true, MIGRATION_11_12).use { db ->
            db.query(
                "SELECT reimbursementStatus, location, reportStatus FROM appointments WHERE eventId = 'evt'"
            ).use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals("", cursor.getString(0))
                assertEquals("Cabinet", cursor.getString(1))
                assertEquals("PENDING", cursor.getString(2))
            }
        }
    }

    companion object {
        private const val TEST_DB = "endoluteal-schema-harness-test.db"
        private const val MIGRATION_DB = "endoluteal-migration-11-12.db"
    }
}
