package com.petwell.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.petwell.data.dao.DailyLogDao
import com.petwell.data.dao.PetDao
import com.petwell.data.dao.PetReminderDao
import com.petwell.data.entity.DailyLog
import com.petwell.data.entity.PetProfile
import com.petwell.data.entity.PetReminder
import com.petwell.data.entity.PetReminderLog

@Database(
    entities = [
        PetProfile::class,
        DailyLog::class,
        PetReminder::class,
        PetReminderLog::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PetWellDatabase : RoomDatabase() {

    abstract fun petDao(): PetDao

    abstract fun dailyLogDao(): DailyLogDao

    abstract fun petReminderDao(): PetReminderDao
}
