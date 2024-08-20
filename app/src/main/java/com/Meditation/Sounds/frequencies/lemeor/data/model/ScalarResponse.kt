package com.Meditation.Sounds.frequencies.lemeor.data.model

import androidx.room.TypeConverters
import com.Meditation.Sounds.frequencies.lemeor.data.database.converters.ScalarConverter

data class ScalarResponse(
    val message: String,
    @TypeConverters(ScalarConverter::class) var data: List<Scalar>,
)