package com.purrcare.data.database

import androidx.room.TypeConverter
import com.purrcare.data.entity.enums.LitterUrination
import com.purrcare.data.entity.enums.WaterIntake

class Converters {

    @TypeConverter
    fun fromWaterIntake(value: WaterIntake): String = value.name

    @TypeConverter
    fun toWaterIntake(value: String): WaterIntake = WaterIntake.valueOf(value)

    @TypeConverter
    fun fromLitterUrination(value: LitterUrination): String = value.name

    @TypeConverter
    fun toLitterUrination(value: String): LitterUrination = LitterUrination.valueOf(value)
}
