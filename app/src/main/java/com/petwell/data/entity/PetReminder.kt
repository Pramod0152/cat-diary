package com.petwell.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.petwell.data.entity.enums.ReminderType

@Entity(
    tableName = "pet_reminder",
    foreignKeys = [
        ForeignKey(
            entity = PetProfile::class,
            parentColumns = ["id"],
            childColumns = ["pet_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("pet_id")]
)
data class PetReminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "pet_id")
    val petId: Long,

    @ColumnInfo(name = "reminder_type")
    val reminderType: ReminderType,

    @ColumnInfo(name = "title")
    val title: String,

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
