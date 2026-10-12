package fr.luteal.core.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
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

    companion object {
        private const val TEST_DB = "endoluteal-schema-harness-test.db"
    }
}
