package com.purrcare.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cat_profile")
data class CatProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "birth_year")
    val birthYear: Int,

    @ColumnInfo(name = "target_weight")
    val targetWeight: Float,

    @ColumnInfo(name = "condition_notes")
    val conditionNotes: String = "",

    @ColumnInfo(name = "avatar_uri")
    val avatarUri: String = ""
)
