package com.petwell.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.petwell.data.entity.enums.LitterUrination
import com.petwell.data.entity.enums.WaterIntake

@Entity(
    tableName = "daily_log",
    foreignKeys = [
        ForeignKey(
            entity = PetProfile::class,
            parentColumns = ["id"],
            childColumns = ["pet_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("pet_id"), Index("timestamp")]
)
data class DailyLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "pet_id")
    val petId: Long,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "weight")
    val weight: Float,

    @ColumnInfo(name = "appetite_score")
    val appetiteScore: Int,

    @ColumnInfo(name = "water_intake")
    val waterIntake: WaterIntake,

    @ColumnInfo(name = "litter_stool_score")
    val litterStoolScore: Int,

    @ColumnInfo(name = "litter_urination")
    val litterUrination: LitterUrination,

    @ColumnInfo(name = "custom_notes")
    val customNotes: String = ""
)
