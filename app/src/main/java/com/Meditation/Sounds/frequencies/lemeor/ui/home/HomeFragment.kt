package com.Meditation.Sounds.frequencies.lemeor.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.feature.base.BaseFragment
import com.Meditation.Sounds.frequencies.lemeor.data.api.RetrofitBuilder
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album
import com.Meditation.Sounds.frequencies.lemeor.data.model.Scalar
import com.Meditation.Sounds.frequencies.lemeor.data.model.Track
import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper
import com.Meditation.Sounds.frequencies.lemeor.data.utils.ViewModelFactory
import com.Meditation.Sounds.frequencies.lemeor.getPreloadedSaveDir
import com.Meditation.Sounds.frequencies.lemeor.getSaveDir
import com.Meditation.Sounds.frequencies.lemeor.isPlayAlbum
import com.Meditation.Sounds.frequencies.lemeor.isPlayProgram
import com.Meditation.Sounds.frequencies.lemeor.observeOnce
import com.Meditation.Sounds.frequencies.lemeor.playAlbumId
import com.Meditation.Sounds.frequencies.lemeor.playProgramId
import com.Meditation.Sounds.frequencies.lemeor.playRife
import com.Meditation.Sounds.frequencies.lemeor.tools.player.MusicRepository
import com.Meditation.Sounds.frequencies.lemeor.tools.player.PlayerService
import com.Meditation.Sounds.frequencies.lemeor.tools.player.SilentQuantumPlayerService
import com.Meditation.Sounds.frequencies.lemeor.trackList
import com.Meditation.Sounds.frequencies.lemeor.ui.main.HomeViewModel
import com.Meditation.Sounds.frequencies.lemeor.ui.main.NavigationActivity
import com.Meditation.Sounds.frequencies.lemeor.ui.silent.SilentQuantumDownloadService
import com.Meditation.Sounds.frequencies.utils.Utils
import com.Meditation.Sounds.frequencies.utils.extensions.dpToPx
import kotlinx.android.synthetic.main.fragment_home.rvHome
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

class HomeFragment : BaseFragment() {
    private val homeAdapter: HomeAdapter by lazy {
        HomeAdapter(onClickItem = {
            (requireActivity() as NavigationActivity).onAlbumDetails(it)
        },onClickFavorites = { item ->

        })
    }
    private var allAlbums = arrayListOf<Album>()

    private val homeViewModel by lazy {
        ViewModelProvider(
            this, ViewModelFactory(
                ApiHelper(RetrofitBuilder(requireContext()).apiService),
                DataBase.getInstance(requireContext())
            )
        )[HomeViewModel::class.java]
    }

    override fun initLayout(): Int = R.layout.fragment_home

    @SuppressLint("ClickableViewAccessibility")
    override fun initComponents() {
        if (activity?.resources?.configuration?.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            homeAdapter.changeLayoutManager(requireContext(), false, requireContext().dpToPx(16))
        } else if (activity?.resources?.configuration?.orientation == Configuration.ORIENTATION_PORTRAIT) {
            homeAdapter.changeLayoutManager(requireContext(), true, requireContext().dpToPx(16))
        }

        rvHome.adapter = homeAdapter

        adjustDataForFullRows(arrayListOf())
        homeViewModel.get48AlbumUnlockedLiveData().observeOnce(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                homeAdapter.setData(it as ArrayList<Album>)
            }
        }

        homeViewModel.getAlbumsUnlockedLiveData().observe(viewLifecycleOwner) {
            allAlbums.clear()
            allAlbums.addAll(it)
            adjustDataForFullRows(allAlbums)
        }
    }

    fun play(tracks: ArrayList<Any>) {
        playRife = null
        val activity = activity as NavigationActivity

        if (isPlayAlbum || playProgramId != 1) {
            activity.hidePlayerUI()
        }

        isPlayAlbum = false
        playAlbumId = -1
        isPlayProgram = true
        playProgramId = 1
        CoroutineScope(Dispatchers.IO).launch {
            val data = tracks.mapNotNull { t ->
                when (t) {
                    is Track -> {
                        MusicRepository.Track(
                            t.id,
                            t.name,
                            t.album?.name ?: "",
                            t.albumId,
                            t.album!!,
                            R.drawable.launcher,
                            t.duration,
                            0,
                            t.filename
                        )

                    }

                    is MusicRepository.Frequency -> {
                        t
                    }

                    else -> null
                }
            } as ArrayList<MusicRepository.Music>
            if (true) {
                trackList?.clear()
                trackList = data
                val mIntent = Intent(requireContext(), PlayerService::class.java).apply {
                    putParcelableArrayListExtra("playlist", arrayListOf<MusicRepository.Music>())
                }
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        requireActivity().stopService(mIntent)
                        requireActivity().startForegroundService(mIntent)
                    } else {
                        requireActivity().stopService(mIntent)
                        requireActivity().startService(mIntent)
                    }
                } catch (_: Exception) {
                }
            }
            CoroutineScope(Dispatchers.Main).launch {
                activity.showPlayerUI()
            }
        }
    }

    private fun playAndDownloadScalar(scalar: Scalar) {
        if (Utils.isConnectedToNetwork(requireContext())) {
            CoroutineScope(Dispatchers.IO).launch {
                val file =
                    File(getSaveDir(requireContext(), scalar.audio_file, scalar.audio_folder))
                val preloaded =
                    File(
                        getPreloadedSaveDir(
                            requireContext(),
                            scalar.audio_file,
                            scalar.audio_folder
                        )
                    )
                if (!file.exists() && !preloaded.exists()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                requireActivity(), Manifest.permission.READ_MEDIA_IMAGES
                            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                                requireActivity(), Manifest.permission.READ_MEDIA_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                                requireActivity(), Manifest.permission.READ_MEDIA_VIDEO
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            SilentQuantumDownloadService.startService(context = requireContext(), scalar)
                        } else {
                            ActivityCompat.requestPermissions(
                                requireActivity(), arrayOf(
                                    Manifest.permission.READ_MEDIA_IMAGES,
                                    Manifest.permission.READ_MEDIA_AUDIO,
                                    Manifest.permission.READ_MEDIA_VIDEO
                                ), 1001
                            )
                        }
                    } else {
                        if (ContextCompat.checkSelfPermission(
                                requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            SilentQuantumDownloadService.startService(context = requireContext(), scalar)
                        } else {
                            ActivityCompat.requestPermissions(
                                requireActivity(),
                                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                1001
                            )
                        }
                    }
                }
            }
        } else {
            Toast.makeText(
                requireContext(), getString(R.string.err_network_available), Toast.LENGTH_SHORT
            ).show()
        }
        playStopScalar()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun playStopScalar() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val playIntent = Intent(context, SilentQuantumPlayerService::class.java).apply {
                    action = "ADD_REMOVE"
                }
                requireActivity().startService(playIntent)
            } catch (_: Exception) {
            }
            CoroutineScope(Dispatchers.Main).launch { (activity as NavigationActivity).showPlayerUI() }
        }
    }

    override fun observerValue() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                homeViewModel.listSearchFlow.collectLatest {
                    homeAdapter.favoritesData = it.toMutableList()
                }
            }
        }
    }

    private fun adjustDataForFullRows(albums: ArrayList<Album>) {
        val isLandscape =
            activity?.resources?.configuration?.orientation == Configuration.ORIENTATION_LANDSCAPE

        val columns = if (isLandscape) 8 else 5

        val albumList = mutableListOf<Album>()

        albumList.addAll(albums)

        val totalItems = albumList.size
        val remainingItems = totalItems % columns
        if (remainingItems != 0) {
            val emptyItemsToAdd = columns - remainingItems
            for (i in 0 until emptyItemsToAdd) {
                albumList.add(
                    Album(
                        id = -1,
                        audio_folder = "",
                        unlock_url = "",
                        benefits_text = ""
                    )
                )
            }
        }
        if (columns == 8) {
            for (i in 1..8) {
                albumList.add(
                    Album(
                        id = -1,
                        audio_folder = "",
                        unlock_url = "",
                        benefits_text = ""
                    )
                )
            }
        } else {
            for (i in 1..5) {
                albumList.add(
                    Album(
                        id = -1,
                        audio_folder = "",
                        unlock_url = "",
                        benefits_text = ""
                    )
                )
            }
        }

        homeAdapter.setData(albumList as ArrayList<Album>)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.isFocusableInTouchMode = true
        view.requestFocus()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (activity?.resources?.configuration?.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            homeAdapter.changeLayoutManager(requireContext(), false, requireContext().dpToPx(16))
        } else if (activity?.resources?.configuration?.orientation == Configuration.ORIENTATION_PORTRAIT) {
            homeAdapter.changeLayoutManager(requireContext(), true, requireContext().dpToPx(16))
        }
        adjustDataForFullRows(allAlbums)
    }

    override fun addListener() {

    }

}