package fr.luteal.app.backup

import org.junit.Assert.assertEquals
import org.junit.Test

class BackupRotationTest {
    @Test
    fun keepsTheNewestNames() {
        val names = listOf(
            "endoluteal-backup-20260801-0300.json",
            "notes.txt",
            "endoluteal-backup-20260803-0300.json",
            "endoluteal-backup-20260802-0300.json"
        )
        assertEquals(
            listOf("endoluteal-backup-20260801-0300.json"),
            backupsToDelete(names, keep = 2)
        )
    }
}
