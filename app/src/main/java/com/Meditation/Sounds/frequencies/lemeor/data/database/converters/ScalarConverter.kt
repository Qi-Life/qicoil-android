package com.Meditation.Sounds.frequencies.lemeor.data.database.converters

import androidx.room.TypeConverter
import com.Meditation.Sounds.frequencies.lemeor.data.model.Scalar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ScalarConverter {
    companion object {
        @TypeConverter
        @JvmStatic
        fun to(value: String): List<Scalar> {
            val type = object : TypeToken<List<Scalar>>() {}.type
            return Gson().fromJson(value, type)
        }

        @TypeConverter
        @JvmStatic
        fun from(list: List<Scalar>): String {
            val type = object : TypeToken<List<Scalar>>() {}.type
            return Gson().toJson(list, type)
        }
    }
}