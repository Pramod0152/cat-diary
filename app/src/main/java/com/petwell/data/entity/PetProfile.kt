package com.petwell.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.petwell.data.entity.enums.Species

@Entity(tableName = "pet_profile")
data class PetProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "species")
    val species: Species,

    @ColumnInfo(name = "birth_year")
    val birthYear: Int,

    @ColumnInfo(name = "target_weight")
    val targetWeight: Float,

    @ColumnInfo(name = "condition_notes")
    val conditionNotes: String = "",

    @ColumnInfo(name = "avatar_uri")
    val avatarUri: String = ""
)
