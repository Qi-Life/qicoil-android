package com.Meditation.Sounds.frequencies.lemeor.ui.main

import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.FAVORITES
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album
import com.Meditation.Sounds.frequencies.lemeor.data.model.HomeResponse
import com.Meditation.Sounds.frequencies.lemeor.data.model.Program
import com.Meditation.Sounds.frequencies.lemeor.data.model.Rife
import com.Meditation.Sounds.frequencies.lemeor.data.model.Scalar
import com.Meditation.Sounds.frequencies.lemeor.data.model.Search
import com.Meditation.Sounds.frequencies.lemeor.data.model.Status
import com.Meditation.Sounds.frequencies.lemeor.data.model.Track
import com.Meditation.Sounds.frequencies.lemeor.data.utils.Resource
import com.Meditation.Sounds.frequencies.lemeor.data.utils.getErrorMsg
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.util.Date

class HomeViewModel(private val repository: HomeRepository, private val db: DataBase) : ViewModel() {
    //val home = repository.getHome(user_id)
    private var _pairData = MutableLiveData<List<Triple<String, List<Search>, Boolean>>>()
    val pairData: LiveData<List<Triple<String, List<Search>, Boolean>>> get() = _pairData

    private var _searchState = MutableLiveData<List<Triple<String, List<Search>, Boolean>>>()
    val searchState: LiveData<List<Triple<String, List<Search>, Boolean>>> get() = _searchState

    fun getHome(id: String): LiveData<Resource<HomeResponse>> {
        return repository.getHome(id)
    }

    fun getRife(): LiveData<Resource<List<Rife>>> {
        return repository.getRife()
    }

    fun getScalar(): LiveData<Resource<List<Scalar>>> {
        return repository.getScalar()
    }

    fun getListAlbum() = repository.getListAlbum()
    fun getListTrack() = repository.getListTrack()

    fun getListRife() = repository.getListRife()

    fun setSearchKeyword(
            key: String,
            idPager: Int,
            context: Context,
            onSearch: (List<Triple<String, List<Search>, Boolean>>) -> Unit,
    ) {
        try {
            if (idPager == 0) {
                _searchState.value?.let {
                    val s = it.first().second.filter { item ->
                        return@filter if (item.obj is Track) {
                            val track = item.obj as Track
                            track.name.lowercase().contains(key.lowercase())
                        } else false
                    }
                    val list = arrayListOf<Triple<String, List<Search>, Boolean>>()
                    list.add(Triple(context.getString(R.string.tv_track), s, it.first().third))
                    list.add(Triple(context.getString(R.string.navigation_lbl_rife), it.last().second, it.last().third))
                    onSearch.invoke(list)
                }
            } else if (idPager == 1) {
                _searchState.value?.let {
                    val s = it.last().second.filter { item ->
                        return@filter if (item.obj is Rife) {
                            val track = item.obj as Rife
                            track.title.lowercase().contains(key.lowercase())
                        } else false
                    }
                    val list = arrayListOf<Triple<String, List<Search>, Boolean>>()
                    list.add(Triple(context.getString(R.string.tv_track), it.first().second, it.first().third))
                    list.add(Triple(context.getString(R.string.navigation_lbl_rife), s, it.last().third))
                    onSearch.invoke(list)
                }
            }
        } catch (_: Exception) {
        }
    }

    fun getLiveData(owner: LifecycleOwner, context: Context) {
        val list = arrayListOf<Triple<String, List<Search>, Boolean>>()
        list.add(Triple(context.getString(R.string.tv_track), arrayListOf(), true))
        list.add(Triple(context.getString(R.string.navigation_lbl_rife), arrayListOf(), true))
        var index = 0
        getListTrack().observe(owner) { listT ->
            CoroutineScope(Dispatchers.IO).launch {
                val listIT = listT.mapNotNull { parcelable ->
                    val track = parcelable as? Track
                    if (track != null) {
                        val album = getAlbumById(track.albumId, track.category_id)
                        Search(++index, track.apply {
                            this.isUnlocked = album?.isUnlocked ?: false
                            this.album = album
                        })
                    } else {
                        null
                    }
                }
                list.firstOrNull()?.let { firstItem ->
                    list[0] = Triple(firstItem.first, listIT, listIT.isEmpty())
                }
                CoroutineScope(Dispatchers.Main).launch {
                    _pairData.value = list
                    _searchState.value = list
                }
            }
        }
        getListRife().observe(owner) { listR ->
            val listIR = listR.mapNotNull { parcelable ->
                val rife = parcelable as? Rife
                if (rife != null) {
                    Search(++index, rife)
                } else {
                    null
                }
            }
            list.lastOrNull()?.let { firstItem ->
                list[1] = Triple(firstItem.first, listIR, listIR.isEmpty())
            }
            _pairData.value = list
            _searchState.value = list
        }
    }

    fun getProfile() = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = repository.getProfile()))
        } catch (exception: HttpException) {
            emit(Resource.error(data = null, message = getErrorMsg(exception)))
        } catch (exception: Throwable) {
            emit(Resource.error(data = null, message = exception.message))
        }
    }

    suspend fun getApkList(): List<String> {
        return repository.getApkList()
    }

    suspend fun reportTrack(trackId: Int, trackUrl: String): Status {
        return repository.reportTrack(trackId, trackUrl)
    }

    suspend fun getAlbumById(id: Int, category_id: Int): Album? {
        return repository.getAlbumById(id, category_id)
    }

    suspend fun getAlbumNameOne(albumName: String): Album? {
        var albumResult = repository.getAlbumsByNameOnce(albumName)
        if (albumResult == null) {
            albumName.split(" ").forEach { s->
                val album = repository.getAlbumsByNameOnce("%$s%")
                if (album != null && albumResult == null) {
                    albumResult = album
                }
            }
        }
        return albumResult
    }

    suspend fun searchAlbum(searchString: String): List<Album> {
        return repository.searchAlbum(searchString)
    }

    suspend fun searchTrack(searchString: String): List<Track> {
        return repository.searchTrack(searchString)
    }

    suspend fun searchProgram(searchString: String): List<Program> {
        return repository.searchProgram(searchString)
    }

    fun loadFromCache(context: Context) {
        val cache: HomeResponse = Gson().fromJson(context.assets.open("db_caсhe.json").bufferedReader().use { reader ->
            reader.readText()
        }, HomeResponse::class.java)

        CoroutineScope(Dispatchers.IO).launch { repository.localSave(cache) }
    }

    fun loadDataLastHomeResponse(context: Context) {
        val homeResponse = PreferenceHelper.getLastHomeResponse(context)
        if (homeResponse?.tiers != null && homeResponse.tiers.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch { repository.localSave(homeResponse) }
        }
    }

    fun syncProgramsToServer(onDone :(()->Unit)? = null) = viewModelScope.launch {
        try {
//            val localData = db.programDao().getData(true).toMutableList()
            val localAllData = db.programDao().getAllData().toMutableList()
            withContext(Dispatchers.IO) {
                val data = repository.getProgramsRemote()
                Log.d("TAG", "syncProgramsToServer: $data")
                val listRemote = data.data?.data ?: arrayListOf<Program>()
                if (localAllData.isNotEmpty()) {
                    localAllData.forEach { local ->
                        val remote = listRemote.firstOrNull { it.id == local.id }
                        try {
                            if (remote != null) {
                                if (remote.deleted) {
                                    db.programDao().delete(local)
                                } else if (remote.updated_at > local.updated_at) {
                                    db.programDao().updateProgram(remote.copy(updated_at = Date().time))
                                } else if (remote.updated_at < local.updated_at) {
                                    if (local.deleted) {
                                        repository.deleteProgram(local.id.toString())
                                        db.programDao().delete(local)
                                    } else {
                                        val update = Update(
                                            id = if (local.user_id.isEmpty()) -1 else local.id,
                                            name = local.name,
                                            favorited = (local.name.lowercase() == FAVORITES.lowercase() && local.favorited),
                                            tracks = local.records.toList()
                                        )
                                        repository.syncProgramsApi(arrayListOf(update))
                                    }
                                }
                            }
                            if (remote == null) {
                                if (local.deleted) {
                                    repository.deleteProgram(local.id.toString())
                                    db.programDao().delete(local)
                                } else {
                                    val update = Update(
                                        id = if (local.user_id.isEmpty()) -1 else local.id,
                                        name = local.name,
                                        favorited = (local.name.lowercase() == FAVORITES.lowercase() && local.favorited),
                                        tracks = local.records.toList()
                                    )
                                    repository.syncProgramsApi(arrayListOf(update))
                                }
                            }
                        } catch (_: Throwable) {
                        }
                    }
                }
            }
        } catch (_: Exception) {
        } finally {
            onDone?.invoke()
        }
    }
}


data class Update(
        val id: Int = 0,
        val name: String = "",
        val favorited: Boolean = false,
        var tracks: List<String> = listOf(),
)

data class UpdateTrack(
        var track_id: List<String> = listOf(),
        var id: Int = 0,
        var track_type: String = "mp3",
        var request_type: String = "add",
        var is_favorite: Boolean = false,
)