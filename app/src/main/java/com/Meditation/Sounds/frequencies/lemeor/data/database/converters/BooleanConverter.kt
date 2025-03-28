package com.Meditation.Sounds.frequencies.lemeor.data.database.converters

import androidx.room.TypeConverter

class BooleanConverter {
    @TypeConverter
    fun fromBoolean(value: Boolean): Int {
        return if (value) 1 else 0
    }

    @TypeConverter
    fun toBoolean(value: Int): Boolean {
        return value == 1
    }
}