package com.petwell.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.petwell.data.dao.DailyLogDao
import com.petwell.data.dao.JournalDao
import com.petwell.data.dao.PetDao
import com.petwell.data.dao.PetReminderDao
import com.petwell.data.entity.DailyLog
import com.petwell.data.entity.JournalEntry
import com.petwell.data.entity.PetProfile
import com.petwell.data.entity.PetReminder
import com.petwell.data.entity.PetReminderLog

@Database(
    entities = [
        PetProfile::class,
        DailyLog::class,
        PetReminder::class,
        PetReminderLog::class,
        JournalEntry::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PetWellDatabase : RoomDatabase() {

    abstract fun petDao(): PetDao

    abstract fun dailyLogDao(): DailyLogDao

    abstract fun petReminderDao(): PetReminderDao

    abstract fun journalDao(): JournalDao

    companion object {
        /**
         * Migration list for PetWellDatabase.
         *
         * EVERY future schema version bump MUST ship a real Migration object here.
         * Destructive fallback is gated behind BuildConfig.DEBUG and must NEVER
         * be the default for release builds.
         *
         * v1 -> v2: Added JournalEntry entity, DailyLog.mood column, removed customNotes
         * v2 -> v3: Added PetReminder.nextReminderDate column
         *
         * Current baseline is v3. No migrations are provided for v1/v2 because
         * the v3 schema is the baseline for this release.
         */
        val MIGRATIONS: Array<Migration> = arrayOf()
    }
}
