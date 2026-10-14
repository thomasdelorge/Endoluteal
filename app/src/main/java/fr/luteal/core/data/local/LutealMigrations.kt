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

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE appointments ADD COLUMN feeCents INTEGER")
        db.execSQL("ALTER TABLE appointments ADD COLUMN reimbursementStatus TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE appointments ADD COLUMN cpamCents INTEGER")
        db.execSQL("ALTER TABLE appointments ADD COLUMN mutuelleCents INTEGER")
    }
}
