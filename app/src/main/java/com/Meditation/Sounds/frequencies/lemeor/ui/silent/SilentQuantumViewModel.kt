package com.Meditation.Sounds.frequencies.lemeor.ui.silent

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.Meditation.Sounds.frequencies.lemeor.data.model.Scalar
import com.Meditation.Sounds.frequencies.lemeor.data.utils.Resource
import com.Meditation.Sounds.frequencies.lemeor.data.utils.getErrorMsg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException

class SilentQuantumViewModel(private val repository: SilentQuantumRepository) : ViewModel() {

    private var listScalar = listOf<Scalar>()

    private val _result = MutableLiveData<List<Scalar>>()

    val result: LiveData<List<Scalar>>
        get() = _result

    fun getScalarList() = repository.getLiveDataScalar()

    fun getScalarById(id: Int) = repository.getScalarById(id)

    fun getScalarLocal(onDone: (List<Scalar>) -> Unit) {
        viewModelScope.launch {
            listScalar = repository.getListScalar()
            onDone.invoke(listScalar)
        }
    }

    fun search(keySearch: String) {
        if (keySearch == "" || keySearch.isEmpty()) {
            _result.value = listScalar.sortedWith(compareBy<Scalar> {
                when {
                    it.name.lowercase().firstOrNull()?.isLetter() == true -> 0
                    else -> 1
                }
            }.thenBy { it.name.lowercase() })
        } else {
            _result.value =
                listScalar.filter { it.name.lowercase().contains(keySearch.lowercase()) }
                    .sortedBy { it.name.lowercase().indexOf(keySearch.lowercase()) != 0 }
        }
    }

    fun searchMain(keySearch: String) {
        if (keySearch == "" || keySearch.isEmpty()) {
            _result.value = arrayListOf()
        } else {
            _result.value =
                listScalar.filter { it.name.lowercase().contains(keySearch.lowercase()) }
                    .sortedBy { it.name.lowercase().indexOf(keySearch.lowercase()) != 0 }
        }
    }

    fun getScalarSubscription(tier: String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = repository.getScalarSubscription(tier)))
        } catch (exception: HttpException) {
            emit(Resource.error(data = null, message = getErrorMsg(exception)))
        } catch (exception: Throwable) {
            emit(Resource.error(data = null, message = exception.message))
        }
    }
}