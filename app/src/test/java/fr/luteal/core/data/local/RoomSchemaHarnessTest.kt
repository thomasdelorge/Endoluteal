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
        helper.createDatabase(TEST_DB, 8).use { db ->
            db.query("SELECT count(*) FROM sqlite_master WHERE type='table'").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertTrue(cursor.getInt(0) > 0)
            }
        }
        helper.runMigrationsAndValidate(TEST_DB, 8, true).close()
    }

    companion object {
        private const val TEST_DB = "endoluteal-schema-harness-test.db"
    }
}
