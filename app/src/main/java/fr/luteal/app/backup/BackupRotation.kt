package fr.luteal.app.backup

internal val backupFileName = Regex("""endoluteal-backup-(\d{8}-\d{4})\.json""")

fun backupsToDelete(names: List<String>, keep: Int): List<String> {
    val matching = names.filter { backupFileName.matches(it) }
    return matching.sortedDescending().drop(keep.coerceAtLeast(0))
}
