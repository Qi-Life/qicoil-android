package com.Meditation.Sounds.frequencies.lemeor.ui.programs

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Meditation.Sounds.frequencies.lemeor.FAVORITES
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album
import com.Meditation.Sounds.frequencies.lemeor.data.model.Program
import com.Meditation.Sounds.frequencies.lemeor.data.model.Rife
import com.Meditation.Sounds.frequencies.lemeor.data.model.Scalar
import com.Meditation.Sounds.frequencies.lemeor.data.model.Search
import com.Meditation.Sounds.frequencies.lemeor.data.model.Track
import com.Meditation.Sounds.frequencies.lemeor.ui.main.UpdateTrack
import com.Meditation.Sounds.frequencies.utils.CombinedLiveData
import com.Meditation.Sounds.frequencies.utils.forEachBreak
import com.Meditation.Sounds.frequencies.utils.isNotString
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NewProgramViewModel(private val repository: ProgramRepository) : ViewModel() {
    private var isLoading = true
    suspend fun insert(program: Program?) {
        repository.insert(program)
    }

    suspend fun delete(program: Program?) {
        repository.delete(program)
    }

    suspend fun getTrackById(id: Int): Track? {
        return repository.getTrackById(id)
    }

    suspend fun getAlbumById(id: Int, categoryId: Int): Album? {
        return repository.getAlbumById(id, categoryId)
    }

    suspend fun createProgram(name: String) = repository.createProgram(name)

    suspend fun deleteProgram(idProgram: String) = repository.deleteProgram(idProgram)

    suspend fun updateTrackToProgram(track: UpdateTrack) = repository.updateTrackToProgram(track)

    suspend fun udpate(program: Program) {
        repository.update(program)
    }

    ///update updateProgram
    suspend fun updateProgram(program: Program) {
        repository.updateProgram(program)
    }

    fun getPrograms(): LiveData<List<Program>> {
        return repository.getPrograms()
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun addTrackToProgram(id: Int, list: List<Search>, onDone: (() -> Unit)? = null) {
        GlobalScope.launch(Dispatchers.IO) {
            val program = repository.getProgramById(id)
            val listT = arrayListOf<String>()
            val listR = arrayListOf<String>()
            val listL = arrayListOf<String>()
            program?.let { p ->
                list.forEach { s ->
                    when (s.obj) {
                        is Track -> {
                            val a = s.obj as Track
                            a.albumId
                            p.records.add(a.id.toString())
                            listT.add(a.id.toString())
                        }

                        is Rife -> {
                            val r = s.obj as Rife
                            r.getFrequency().forEach { fre ->
                                val fr = "${r.id}|${fre}"
                                p.records.add(fr)
                                listR.add(fr)
                            }
                        }

                        is Scalar -> {
                            val a = s.obj as Scalar
                            p.records.add(a.id + "-scalar")
                            listL.add(a.id)
                        }
                    }
                }

                repository.updateProgram(p)
                if (p.user_id.isNotEmpty()) {
                    try {
                        if (listT.isNotEmpty()) {
                            updateTrackToProgram(
                                UpdateTrack(
                                    track_id = listT,
                                    id = p.id,
                                    "mp3",
                                    request_type = "add",
                                    is_favorite = (p.name.uppercase() == FAVORITES.uppercase() && p.favorited)
                                )
                            )
                        }
                        if (listR.isNotEmpty()) {
                            updateTrackToProgram(
                                UpdateTrack(
                                    track_id = listR,
                                    id = p.id,
                                    "rife",
                                    request_type = "add",
                                    is_favorite = (p.name.uppercase() == FAVORITES.uppercase() && p.favorited)
                                )
                            )
                        }
                        if (listL.isNotEmpty()) {
                            updateTrackToProgram(
                                UpdateTrack(
                                    track_id = listL,
                                    id = p.id,
                                    "mp3",
                                    request_type = "add",
                                    is_favorite = (p.name.uppercase() == FAVORITES.uppercase() && p.favorited)
                                )
                            )
                        }
                        withContext(Dispatchers.Main) {
                            onDone?.invoke()
                        }
                    } catch (_: Exception) {
                    }
                }
            }
        }
    }

    fun addFrequencyToProgram(id: Int, frequency: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val program = repository.getProgramById(id)
            program?.let { p ->
                p.records.add("${-frequency}")
                repository.updateProgram(p)
                if (p.user_id.isNotEmpty()) {
                    try {
                        updateTrackToProgram(
                            UpdateTrack(
                                track_id = listOf("${-frequency}"),
                                id = p.id,
                                "rife",
                                request_type = "add",
                                is_favorite = (p.name.uppercase() == FAVORITES.uppercase() && p.favorited)
                            )
                        )
                    } catch (_: Exception) {
                    }
                }
            }
        }
    }

    fun getPrograms(
        owner: LifecycleOwner,
        onChange: (List<Program>) -> Unit,
        showLoading: (Boolean) -> Unit,
    ) {
        CombinedLiveData(repository.getListProgram(),
            repository.getListTrack(),
            combine = { listA, listT ->
                val listAb = listA ?: arrayListOf()
                val listTr = listT ?: arrayListOf()
                if (listAb.isNotEmpty() && listTr.isNotEmpty()) {
                    return@CombinedLiveData Pair(listAb, listTr)
                } else {
                    return@CombinedLiveData Pair(null, null)
                }
            }).observe(owner) { pair ->
            val listA = pair.first ?: arrayListOf()
            val listT = pair.second ?: arrayListOf()
            if (listT.isEmpty() || listA.isEmpty()) {
                showLoading(false)
            }
            if (listT.isNotEmpty()) {
                if(isLoading){
                    showLoading(true)
                }
                viewModelScope.launch(Dispatchers.IO) {
                    val programs = async { checkUnlocked(listA) }
                    val listProgramHandled = programs.await()

                    val validIds = listT.map { it.id.toString() }.toSet()

                    listProgramHandled.forEach { item ->
                        item.records = item.records.filter { record ->
                            validIds.contains(record) || record.contains('|') || record.contains(
                                '-'
                            )
                        } as ArrayList<String>
                    }
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                        isLoading = false
                        onChange.invoke(listProgramHandled)
                    }
                }
            }
        }
    }

    private suspend fun checkUnlocked(list: List<Program>): List<Program> {
        list.forEach { program ->
            val tracks = program.records.filterNot { !it.isNotString() }
                .mapNotNull { it.toDoubleOrNull()?.toInt() }.mapNotNull { getTrackById(it) }
            val isUnlocked = tracks.forEachBreak { track ->
                val tempAlbum = getAlbumById(track.albumId, track.category_id)
                tempAlbum?.isUnlocked ?: true
            }
            program.isUnlocked = isUnlocked
        }
        return list
    }
}