package com.purrcare.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.purrcare.data.entity.enums.LitterUrination
import com.purrcare.data.entity.enums.WaterIntake

@Entity(
    tableName = "daily_log",
    foreignKeys = [
        ForeignKey(
            entity = CatProfile::class,
            parentColumns = ["id"],
            childColumns = ["cat_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("cat_id"), Index("timestamp")]
)
data class DailyLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "cat_id")
    val catId: Long,

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
