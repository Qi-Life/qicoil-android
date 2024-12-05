package com.Meditation.Sounds.frequencies.lemeor.ui.silent

import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.model.ScalarSubscriptionResponse
import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper

class SilentQuantumRepository(private val apiHelper: ApiHelper, private val localData: DataBase) {

    fun getLiveDataScalar() = localData.scalarDao().getLiveDataScalars()
    suspend fun getListScalar() = localData.scalarDao().getData()
    fun getScalarById(id: Int) = localData.scalarDao().getScalarById(id)
    suspend fun getScalarSubscription(): ScalarSubscriptionResponse {
        return apiHelper.getScalarSubscription()
    }

}