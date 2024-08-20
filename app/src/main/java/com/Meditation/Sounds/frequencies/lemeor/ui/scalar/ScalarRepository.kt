package com.Meditation.Sounds.frequencies.lemeor.ui.scalar

import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper

class ScalarRepository(private val apiHelper: ApiHelper, private val localData: DataBase) {

    fun getLiveDataScalar() = localData.scalarDao().getLiveDataScalars()
    suspend fun getListScalar() = localData.scalarDao().getData()
    fun getScalarById(id: Int) = localData.scalarDao().getScalarById(id)
}