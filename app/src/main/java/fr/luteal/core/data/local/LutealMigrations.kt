package fr.luteal.core.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE pain_marks ADD COLUMN pinSide TEXT")
        db.execSQL("ALTER TABLE pain_marks ADD COLUMN pinX REAL")
        db.execSQL("ALTER TABLE pain_marks ADD COLUMN pinY REAL")
    }
}
