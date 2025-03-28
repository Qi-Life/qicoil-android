package com.Meditation.Sounds.frequencies.lemeor.ui.albums.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album
import com.Meditation.Sounds.frequencies.lemeor.data.model.Rife
import com.Meditation.Sounds.frequencies.lemeor.data.model.Track
import com.Meditation.Sounds.frequencies.lemeor.ui.programs.ProgramRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class NewAlbumDetailViewModel(private val repository: AlbumDetailRepository, private val programRepository: ProgramRepository) : ViewModel() {
    fun album(id: Int, category_id: Int): LiveData<Album>? {
        return repository.getAlbumsById(id, category_id)
    }

    fun getTrackByAlbumId(albumId : Int, categoryId : Int): Flow<List<Track>> {
        return programRepository.getTrackByAlbumId(albumId, categoryId)
    }

    suspend fun getTrack(id : Int): Track? {
        return programRepository.getTrackById(id)
    }

    fun addRife(rife: Rife) {
        CoroutineScope(Dispatchers.IO).launch {
            repository.insertRife(rife)
        }
    }
}