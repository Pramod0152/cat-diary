package com.petwell.data.database

import androidx.room.TypeConverter
import com.petwell.data.entity.enums.LitterUrination
import com.petwell.data.entity.enums.Mood
import com.petwell.data.entity.enums.ReminderType
import com.petwell.data.entity.enums.Species
import com.petwell.data.entity.enums.WaterIntake

class Converters {

    @TypeConverter
    fun fromWaterIntake(value: WaterIntake): String = value.name

    @TypeConverter
    fun toWaterIntake(value: String): WaterIntake = WaterIntake.valueOf(value)

    @TypeConverter
    fun fromLitterUrination(value: LitterUrination): String = value.name

    @TypeConverter
    fun toLitterUrination(value: String): LitterUrination = LitterUrination.valueOf(value)

    @TypeConverter
    fun fromSpecies(value: Species): String = value.name

    @TypeConverter
    fun toSpecies(value: String): Species = Species.valueOf(value)

    @TypeConverter
    fun fromReminderType(value: ReminderType): String = value.name

    @TypeConverter
    fun toReminderType(value: String): ReminderType = ReminderType.valueOf(value)

    @TypeConverter
    fun fromMood(value: Mood?): String? = value?.name

    @TypeConverter
    fun toMood(value: String?): Mood? = value?.let { Mood.valueOf(it) }
}
