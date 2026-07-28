package com.purrcare.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.purrcare.data.dao.CatDao
import com.purrcare.data.dao.DailyLogDao
import com.purrcare.data.dao.MedicationDao
import com.purrcare.data.entity.CatProfile
import com.purrcare.data.entity.DailyLog
import com.purrcare.data.entity.Medication
import com.purrcare.data.entity.MedicationLog

@Database(
    entities = [
        CatProfile::class,
        DailyLog::class,
        Medication::class,
        MedicationLog::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PurrCareDatabase : RoomDatabase() {

    abstract fun catDao(): CatDao

    abstract fun dailyLogDao(): DailyLogDao

    abstract fun medicationDao(): MedicationDao
}
