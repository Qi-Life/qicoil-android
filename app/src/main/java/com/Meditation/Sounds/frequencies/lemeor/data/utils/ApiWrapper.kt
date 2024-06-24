package com.Meditation.Sounds.frequencies.lemeor.data.utils

import com.google.gson.JsonParser
import retrofit2.HttpException

fun getErrorMsg(
        exception: HttpException
): String? {
    return try {
        JsonParser().parse(exception.response()?.errorBody()?.string())
            .asJsonObject["message"]
            .asString
    } catch (_: Exception) {
        null
    }


}