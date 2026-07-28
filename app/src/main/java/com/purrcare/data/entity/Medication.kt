package com.purrcare.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medication",
    foreignKeys = [
        ForeignKey(
            entity = CatProfile::class,
            parentColumns = ["id"],
            childColumns = ["cat_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("cat_id")]
)
data class Medication(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "cat_id")
    val catId: Long,

    @ColumnInfo(name = "med_name")
    val medName: String,

    @ColumnInfo(name = "dosage")
    val dosage: String,

    @ColumnInfo(name = "frequency_hours")
    val frequencyHours: Int,

    @ColumnInfo(name = "is_enabled")
    val isEnabled: Boolean = true,

    @ColumnInfo(name = "alarm_hour")
    val alarmHour: Int = 8,

    @ColumnInfo(name = "alarm_minute")
    val alarmMinute: Int = 0
)
