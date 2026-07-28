package com.petwell.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pet_reminder_log",
    foreignKeys = [
        ForeignKey(
            entity = PetReminder::class,
            parentColumns = ["id"],
            childColumns = ["reminder_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("reminder_id"), Index("timestamp")]
)
data class PetReminderLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "reminder_id")
    val reminderId: Long,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "was_administered")
    val wasAdministered: Boolean
)
