package com.Meditation.Sounds.frequencies.lemeor.ui.albums.detail

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.Meditation.Sounds.frequencies.QApplication
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.feature.base.BaseFragment
import com.Meditation.Sounds.frequencies.lemeor.albumIdBackProgram
import com.Meditation.Sounds.frequencies.lemeor.categoryIdBackProgram
import com.Meditation.Sounds.frequencies.lemeor.convertSecondsToTime
import com.Meditation.Sounds.frequencies.lemeor.currentTrack
import com.Meditation.Sounds.frequencies.lemeor.currentTrackIndex
import com.Meditation.Sounds.frequencies.lemeor.data.api.RetrofitBuilder
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album
import com.Meditation.Sounds.frequencies.lemeor.data.model.Rife
import com.Meditation.Sounds.frequencies.lemeor.data.model.Track
import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper
import com.Meditation.Sounds.frequencies.lemeor.data.utils.ViewModelFactory
import com.Meditation.Sounds.frequencies.lemeor.getPreloadedSaveDir
import com.Meditation.Sounds.frequencies.lemeor.getSaveDir
import com.Meditation.Sounds.frequencies.lemeor.isMultiPlay
import com.Meditation.Sounds.frequencies.lemeor.isPlayAlbum
import com.Meditation.Sounds.frequencies.lemeor.isPlayProgram
import com.Meditation.Sounds.frequencies.lemeor.isTrackAdd
import com.Meditation.Sounds.frequencies.lemeor.isUserPaused
import com.Meditation.Sounds.frequencies.lemeor.loadImage
import com.Meditation.Sounds.frequencies.lemeor.playAlbumId
import com.Meditation.Sounds.frequencies.lemeor.playListScalar
import com.Meditation.Sounds.frequencies.lemeor.playProgramId
import com.Meditation.Sounds.frequencies.lemeor.playRife
import com.Meditation.Sounds.frequencies.lemeor.playtimeRife
import com.Meditation.Sounds.frequencies.lemeor.rifeBackProgram
import com.Meditation.Sounds.frequencies.lemeor.selectedNaviFragment
import com.Meditation.Sounds.frequencies.lemeor.showAlert
import com.Meditation.Sounds.frequencies.lemeor.showAlertInfo
import com.Meditation.Sounds.frequencies.lemeor.tierPosition
import com.Meditation.Sounds.frequencies.lemeor.tierPositionSelected
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper
import com.Meditation.Sounds.frequencies.lemeor.tools.downloader.DownloaderActivity
import com.Meditation.Sounds.frequencies.lemeor.tools.player.MusicRepository
import com.Meditation.Sounds.frequencies.lemeor.tools.player.PlayerSelected
import com.Meditation.Sounds.frequencies.lemeor.tools.player.PlayerService
import com.Meditation.Sounds.frequencies.lemeor.tools.player.PlayerStatus
import com.Meditation.Sounds.frequencies.lemeor.trackList
import com.Meditation.Sounds.frequencies.lemeor.typeBack
import com.Meditation.Sounds.frequencies.lemeor.ui.albums.tabs.TiersPagerFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.main.NavigationActivity
import com.Meditation.Sounds.frequencies.lemeor.ui.programs.NewProgramFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.rife.NewRifeViewModel
import com.Meditation.Sounds.frequencies.lemeor.ui.silent.SilentQuantumFragment
import com.Meditation.Sounds.frequencies.utils.Constants
import com.Meditation.Sounds.frequencies.utils.Constants.Companion.PREF_SETTING_ADVANCE_SCALAR_ON_OFF
import com.Meditation.Sounds.frequencies.utils.PlayerUtils
import com.Meditation.Sounds.frequencies.utils.SharedPreferenceHelper
import com.Meditation.Sounds.frequencies.utils.Utils
import com.Meditation.Sounds.frequencies.utils.firstIndexOrNull
import com.Meditation.Sounds.frequencies.views.AlertMessageDialog
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_new_album_detail.album_add_scalar
import kotlinx.android.synthetic.main.fragment_new_album_detail.album_back
import kotlinx.android.synthetic.main.fragment_new_album_detail.album_image
import kotlinx.android.synthetic.main.fragment_new_album_detail.album_play
import kotlinx.android.synthetic.main.fragment_new_album_detail.album_tracks_recycler
import kotlinx.android.synthetic.main.fragment_new_album_detail.programName
import kotlinx.android.synthetic.main.fragment_new_album_detail.program_time
import kotlinx.android.synthetic.main.fragment_new_album_detail.tvDescription
import kotlinx.android.synthetic.main.fragment_new_album_detail.view_space_silent_quantum
import kotlinx.android.synthetic.main.player_ui_fragment.track_image
import kotlinx.android.synthetic.main.player_ui_fragment.track_name
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.observeOn
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.util.Date
import kotlin.math.abs


class NewAlbumDetailFragment : BaseFragment() {

    val albumId: Int by lazy {
        arguments?.getInt(ARG_ALBUM_ID)
            ?: throw IllegalArgumentException("Must call through newInstance()")
    }

    val categoryId: Int by lazy {
        arguments?.getInt(ARG_CATEGORY_ID)
            ?: throw IllegalArgumentException("Must call through newInstance()")
    }

    val type: String by lazy {
        arguments?.getString(ARG_TYPE)
            ?: throw IllegalArgumentException("Must call through newInstance()")
    }

    val rifeId: Int by lazy {
        arguments?.getInt(ARG_RIFE_ID)
            ?: throw IllegalArgumentException("Must call through newInstance()")
    }

    private lateinit var mViewModel: NewAlbumDetailViewModel
    private lateinit var rifeViewModel: NewRifeViewModel
    private var mAlbum: Album? = null
    private var mRife: Rife? = null
    private var isPlaying = false

    private val trackAdapter by lazy {
        AlbumTrackAdapter(onClickItem = { track, pos, _ ->
            Log.d("download", "${track.isDownloaded}")
            if (track.isDownloaded) {
                isMultiPlay = false
                mAlbum?.play()
                Handler(Looper.getMainLooper()).postDelayed({
                    EventBus.getDefault().post(PlayerSelected(pos))
                    timeDelay = 200L
                }, timeDelay)

            } else {
                AlertMessageDialog(
                    requireContext(),
                    message = getString(R.string.only_downloaded_frequencies_can_be_played),
                    title = getString(R.string.notice),
                    isHideBtnNo = true
                ) {
                }.show()
                downloadAlbum(album = mAlbum!!)
            }
        }, onClickOptions = { t, _ ->
            startActivityForResult(
                TrackOptionsPopUpActivity.newIntent(
                    requireContext(), t.id.toDouble()
                ), 1001
            )
        })
    }

    private val rifeAdapter by lazy {
        RifeAdapter(onClickItem = { f, pos ->
            if (-f.frequency.toDouble() >= Constants.defaultHz) {
                isMultiPlay = false
                mRife?.play()
                Handler(Looper.getMainLooper()).postDelayed({
                    EventBus.getDefault().post(PlayerSelected(pos))
                    timeDelay = 200L
                }, timeDelay)
            }
        }, onClickOptions = { f, _ ->
            if (-f.frequency.toDouble() >= Constants.defaultHz) {
                startActivityForResult(
                    TrackOptionsPopUpActivity.newIntent(
                        requireContext(), -f.frequency.toDouble(), rife = mRife
                    ), 1001
                )
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_hz_exceeded, abs(Constants.defaultHz).toString()),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private var isFirst = true
    private var timeDelay = 500L
    override fun initLayout() = R.layout.fragment_new_album_detail
    override fun initComponents() {
        firebaseAnalytics = Firebase.analytics
        initUI()
        program_time.visibility = View.GONE
        reloadUI()
        updateViewSilentQuantum()
        view?.isFocusableInTouchMode = true
        view?.requestFocus()
        view?.setOnKeyListener { _, keyCode, event ->
            if (event != null && event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                onBackPressed()
                true
            } else false
        }
    }

    private fun reloadUI() {
        if (type == Constants.TYPE_ALBUM) {
            reloadAlbumUI()
        } else if (type == Constants.TYPE_RIFE) {
            reloadRifeUI()
        }
    }

    @OptIn(FlowPreview::class)
    @SuppressLint("NotifyDataSetChanged")
    private fun reloadAlbumUI() {
        album_tracks_recycler.adapter = trackAdapter
//        mViewModel.album(albumId, categoryId)?.observe(viewLifecycleOwner) { a ->
//            if (a != null) {
//                mAlbum = a
//                programName.text = a.name
//                a.initView()
//            }
//        }
//
//        mViewModel.getTrackByAlbumId(albumId, categoryId).debounce(300).asLiveData()
//            .observe(viewLifecycleOwner) {
//                Log.d("log", "notifyDataSetChanged")
//                trackAdapter.notifyDataSetChanged()
//                viewLifecycleOwner.lifecycleScope.launch {
//                    val newsTracks = trackAdapter.currentList.map {
//                        val itemTrack =
//                            DataBase.getInstance(QApplication.getInstance().applicationContext)
//                                .trackDao().getTrackById(it.id)
//                        it.apply { isDownloaded = itemTrack?.isDownloaded == true }
//                    }
//
//                    trackAdapter.submitList(newsTracks)
//                }
//            }
        mViewModel.album(albumId, categoryId)?.asFlow()
            ?.combine(mViewModel.getTrackByAlbumId(albumId, categoryId)) { a, t ->
                Pair(a, t)
            }?.debounce(100)?.asLiveData()?.observe(viewLifecycleOwner) { pair ->
                val album = pair.first
                mAlbum = album
                programName.text = album.name
                album.initView()
                //update status
                val newsTracks = pair.first.tracks.map {
                    it.copy(isDownloaded = checkItemDownloaded(album, it))
                }
                trackAdapter.submitList(newsTracks)
                Log.d("submitList", newsTracks.filter { it.isDownloaded }.size.toString())
            }
    }

    private fun updateViewSilentQuantum() {
        if (SharedPreferenceHelper.getInstance().getBool(PREF_SETTING_ADVANCE_SCALAR_ON_OFF)) {
            view_space_silent_quantum.visibility = View.VISIBLE
            album_add_scalar.visibility = View.VISIBLE
        } else {
            view_space_silent_quantum.visibility = View.GONE
            album_add_scalar.visibility = View.GONE
        }
    }

    private fun reloadRifeUI() {
        album_tracks_recycler.adapter = rifeAdapter
        if (rifeId >= 0) {
            rifeViewModel.getRifeById(rifeId).observe(viewLifecycleOwner) {
                mRife = it
                mRife?.let { r ->
                    program_time.visibility = View.VISIBLE
                    if (playRife != null) {
                        if (playRife?.id == r.id && playtimeRife > 0L) {
                            program_time.text = getString(
                                R.string.total_time, convertSecondsToTime(playtimeRife)
                            )
                        } else {
                            program_time.text = getString(
                                R.string.total_time,
                                convertSecondsToTime((mRife!!.getFrequency().size * 3 * 60).toLong())
                            )
                        }
                    } else {
                        program_time.text = getString(
                            R.string.total_time,
                            convertSecondsToTime((mRife!!.getFrequency().size * 3 * 60).toLong())
                        )
                    }
                    programName.text = r.title
                    r.initView()
                }
            }
        }

    }

    override fun addListener() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Suppress("DEPRECATION")
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        parentFragmentManager.beginTransaction().detach(this).commitAllowingStateLoss()
        Handler().postDelayed({
            super.onConfigurationChanged(newConfig)
            parentFragmentManager.beginTransaction().attach(this).commitAllowingStateLoss()
        }, 500)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: Any?) {
        event?.let { ev ->
            if (ev is Rife) {
                mRife?.let { m ->
                    if (ev.id == m.id) {
                        program_time.text =
                            getString(R.string.total_time, convertSecondsToTime(ev.playtime))
                    }
                }
            }

            if (ev is PlayerStatus) {
                if (ev.isPlaying) {
                    if (type == Constants.TYPE_ALBUM) {
                        updateViewPlay(albumId)
                    } else {
                        updateViewPlay(rifeId)
                    }
                }
            }
        }
    }

    private fun onBackPressed() {
        tierPositionSelected = tierPosition
        var fragment = selectedNaviFragment
        if (fragment == null) {
            fragment = TiersPagerFragment()
        }
        parentFragmentManager.beginTransaction().setCustomAnimations(
            R.anim.trans_left_to_right_in,
            R.anim.trans_left_to_right_out,
            R.anim.trans_right_to_left_in,
            R.anim.trans_right_to_left_out
        ).replace(R.id.nav_host_fragment, fragment, fragment.javaClass.simpleName).commit()
    }

    private fun initUI() {
        mViewModel = ViewModelProvider(
            this, ViewModelFactory(
                ApiHelper(RetrofitBuilder(requireContext()).apiService),
                DataBase.getInstance(requireContext())
            )
        )[NewAlbumDetailViewModel::class.java]
        rifeViewModel = ViewModelProvider(
            this,
            ViewModelFactory(
                ApiHelper(RetrofitBuilder(requireContext()).apiService),
                DataBase.getInstance(requireContext())
            )
        )[NewRifeViewModel::class.java]
    }

    private fun Album.initView() {
        currentTrackIndex.observe(viewLifecycleOwner) {
            val track =
                tracks.firstIndexOrNull { index, _ -> index == it && playAlbumId == albumId }
            track?.let { item ->
                trackAdapter.setSelectedItem(item)
            }
        }

        tvDescription.text = benefits_text
        album_back.setOnClickListener { onBackPressed() }

        album_image.radius = resources.getDimensionPixelOffset(R.dimen.corner_radius_album)

        loadImage(requireContext(), album_image, this)

        updateViewPlay(albumId)
        album_play.setOnClickListener {
            if (tracks.isNotEmpty()) {
                if (isPlaying) {
                    isPlaying = false
                    album_play.setImageResource(R.drawable.bg_play_detail)
                    EventBus.getDefault().post(PlayerStatus(isPause = true))
                } else {
                    isPlaying = true
                    album_play.setImageResource(R.drawable.bg_pause_detail)
                    PlayerUtils.checkSchedulePlaying(requireContext()) {
                        if (this.tracks.any { checkItemDownloaded(album = this, track = it) }) {
                            playAndDownload(this)
                        } else {
                            //only download
                            downloadAlbum(this)
                            AlertMessageDialog(
                                requireContext(),
                                message = getString(R.string.msg_download_data),
                                title = getString(R.string.notice),
                                isHideBtnNo = true
                            ) {
                            }.show()
                        }
                    }
                }
            }
        }
        album_add_scalar.setOnClickListener {
            if (selectedNaviFragment != null && selectedNaviFragment is SilentQuantumFragment) {
                onBackPressed()
            } else {
                (activity as NavigationActivity).onScalarSelect()
            }
        }

        if (currentTrack.value != null) {
            val track = currentTrack.value
            if (track is MusicRepository.Track) {
                tracks.firstOrNull { it.id == track.trackId }?.let {
                    trackAdapter.setSelectedItem(it)
                }
            }
        }
    }

    private fun Rife.initView() {
        val local = getFrequency().mapIndexed { index, s ->
            MusicRepository.Frequency(
                index,
                title,
                s,
                id,
                index,
                false,
                0,
                0,
            )
        }
        currentTrackIndex.observe(viewLifecycleOwner) {
            val frequency = local.firstIndexOrNull { index, _ -> index == it && playAlbumId == id }
            frequency?.let { item ->
                rifeAdapter.setSelectedItem(item)
            }
        }
        rifeAdapter.submitList(local)
        album_back.setOnClickListener { onBackPressed() }
        album_image.radius = resources.getDimensionPixelOffset(R.dimen.corner_radius_album)
        album_image.setImageResource(R.drawable.frequency_v2)
        tvDescription.text = description
        updateViewPlay(rifeId)
        album_play.setOnClickListener {
            if (getFrequency().isNotEmpty()) {
                if (isPlaying) {
                    isPlaying = false
                    EventBus.getDefault().post(PlayerStatus(isPause = true))
                } else {
                    isPlaying = true
                    PlayerUtils.checkSchedulePlaying(requireContext()) {
                        play()
                    }
                }
            }
        }
        album_add_scalar.setOnClickListener {
            if (selectedNaviFragment != null && selectedNaviFragment is SilentQuantumFragment) {
                onBackPressed()
            } else {
                (activity as NavigationActivity).onScalarSelect()
            }
        }
        if (currentTrack.value != null) {
            val item = currentTrack.value
            if (item is MusicRepository.Frequency) {
                val indexSelected = item.index
                if (indexSelected >= 0 && playAlbumId == id) {
                    rifeAdapter.setSelectedItem(item)
                }
            }
        }

    }

    private fun updateViewPlay(id: Int) {
        if (playAlbumId == id && isPlayAlbum && !isUserPaused) {
            isPlaying = true
            album_play.setImageResource(R.drawable.bg_pause_detail)
        } else {
            isPlaying = false
            album_play.setImageResource(R.drawable.bg_play_detail)
        }
    }


    private fun downloadAlbum(album: Album) {
        if (Utils.isConnectedToNetwork(requireContext())) {
            val tracks = ArrayList<Track>()
            val trackDao = DataBase.getInstance(requireContext()).trackDao()
            CoroutineScope(Dispatchers.IO).launch {
                album.tracks.forEach { t ->
                    val file = File(getSaveDir(requireContext(), t.filename, album.audio_folder))
                    val preloaded =
                        File(getPreloadedSaveDir(requireContext(), t.filename, album.audio_folder))

                    if (!file.exists() && !preloaded.exists()) {
                        trackDao.isTrackDownloaded(false, t.id)
                        t.isDownloaded = false
                        tracks.add(t)
                    }
                    t.album = Album(
                        album.id,
                        album.category_id,
                        album.tier_id,
                        album.name,
                        album.image,
                        album.audio_folder,
                        album.is_free,
                        album.order,
                        album.order_by,
                        album.updated_at,
                        null,
                        listOf(),
                        null,
                        isDownloaded = false,
                        isUnlocked = false,
                        album.unlock_url,
                        album.benefits_text
                    )

                }

                CoroutineScope(Dispatchers.Main).launch {
                    activity?.let {
                        DownloaderActivity.startDownload(it, tracks)
                    }
                }

            }
        } else {
            Toast.makeText(
                requireContext(), getString(R.string.err_network_available), Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun playAndDownload(album: Album) {
        SharedPreferenceHelper.getInstance().addRecentAlbum(album)
        playRife = null
        firebaseAnalytics.logEvent("Downloads") {
            param("Album Id", album.id.toString())
            param("Album Name", album.name)
            // param(FirebaseAnalytics.Param.CONTENT_TYPE, "image")
        }
        if (Utils.isConnectedToNetwork(requireContext())) {
            val tracks = ArrayList<Track>()
            val trackDao = DataBase.getInstance(requireContext()).trackDao()
            CoroutineScope(Dispatchers.IO).launch {
                album.tracks.forEach { t ->
                    val file = File(getSaveDir(requireContext(), t.filename, album.audio_folder))
                    val preloaded =
                        File(getPreloadedSaveDir(requireContext(), t.filename, album.audio_folder))

                    if (!file.exists() && !preloaded.exists()) {
                        trackDao.isTrackDownloaded(false, t.id)
                        t.isDownloaded = false
                        tracks.add(t)
                    }
                    t.album = Album(
                        album.id,
                        album.category_id,
                        album.tier_id,
                        album.name,
                        album.image,
                        album.audio_folder,
                        album.is_free,
                        album.order,
                        album.order_by,
                        album.updated_at,
                        null,
                        listOf(),
                        null,
                        isDownloaded = false,
                        isUnlocked = false,
                        album.unlock_url,
                        album.benefits_text
                    )

                }

                CoroutineScope(Dispatchers.Main).launch {
                    activity?.let {
                        DownloaderActivity.startDownload(it, tracks)
                    }
                }

            }
        } else {
            Toast.makeText(
                requireContext(), getString(R.string.err_network_available), Toast.LENGTH_SHORT
            ).show()
        }
        album.play()
        EventBus.getDefault().post(PlayerSelected(0))
    }

    @Suppress("UNCHECKED_CAST")
    fun Album.play() {
        playRife = null
        val activity = activity as NavigationActivity

        if (isPlayProgram || playAlbumId != id) {
            activity.hidePlayerUI()
        }

        isPlayAlbum = true
        playAlbumId = id
        isUserPaused = false
        isPlayProgram = false
        playProgramId = -1

        CoroutineScope(Dispatchers.IO).launch {
            val data = tracks.mapIndexedNotNull { _, t ->
                try {
                    if (!checkItemDownloaded(this@play, t)) {
                        null
                    } else {
                        MusicRepository.Track(
                            t.id,
                            t.name,
                            name,
                            id,
                            this@play,
                            R.drawable.launcher,
                            t.duration,
                            0,
                            t.filename
                        )
                    }
                } catch (ex: Exception) {
                    null
                }
            } as ArrayList<MusicRepository.Music>

//            if (isFirst) {
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
                isFirst = false
            } catch (e: Exception) {
                e.printStackTrace()
            }
//            }
            CoroutineScope(Dispatchers.Main).launch {
                isPlaying = true
                album_play.setImageResource(R.drawable.bg_pause_detail)
                activity.showPlayerUI()
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun Rife.play() {
        if (playRife != null) {
            if (playRife!!.id != id) {
                playRife = this
            }
        } else {
            playRife = this
        }
        val activity = activity as NavigationActivity

        if (isPlayProgram || playAlbumId != id) {
            activity.hidePlayerUI()
        }

        isPlayAlbum = true
        playAlbumId = id
        isUserPaused = false
        isPlayProgram = false
        playProgramId = -1

        CoroutineScope(Dispatchers.IO).launch {
            val data = getFrequency().mapIndexedNotNull { index, s ->
                try {
                    MusicRepository.Frequency(
                        index,
                        title,
                        s,
                        id,
                        index,
                        false,
                        0,
                        0,
                    )
                } catch (ex: Exception) {
                    null
                }
            } as ArrayList<MusicRepository.Music>
            if (isFirst) {
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
                    isFirst = false
                } catch (_: Exception) {
                }

            }
            CoroutineScope(Dispatchers.Main).launch {
                album_play.setImageResource(R.drawable.bg_pause_detail)
                activity.showPlayerUI()
            }
        }
    }

    private fun checkItemDownloaded(album: Album, track: Track): Boolean {
        val file = File(getSaveDir(requireContext(), track.filename, album.audio_folder))
        val preloaded =
            File(getPreloadedSaveDir(requireContext(), track.filename, album.audio_folder))
        return (file.exists() || preloaded.exists())
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            typeBack = type
            rifeBackProgram = mRife
            albumIdBackProgram = albumId
            categoryIdBackProgram = categoryId
            isTrackAdd = true
            parentFragmentManager.beginTransaction().setCustomAnimations(
                R.anim.trans_right_to_left_in,
                R.anim.trans_right_to_left_out,
                R.anim.trans_left_to_right_in,
                R.anim.trans_left_to_right_out
            ).replace(
                R.id.nav_host_fragment,
                NewProgramFragment(),
                NewProgramFragment().javaClass.simpleName
            ).commit()
        }
    }

    companion object {
        const val ARG_ALBUM_ID = "arg_album"
        const val ARG_CATEGORY_ID = "arg_category"
        const val ARG_TYPE = "arg_type"
        const val ARG_RIFE_ID = "arg_rife_id"

        @JvmStatic
        fun newInstance(
            id: Int,
            categoryId: Int,
            type: String = Constants.TYPE_ALBUM,
            rifeId: Int = -1
        ) = NewAlbumDetailFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_ALBUM_ID, id)
                putInt(ARG_CATEGORY_ID, categoryId)
                putString(ARG_TYPE, type)
                putInt(ARG_RIFE_ID, rifeId)
            }
        }
    }
}
