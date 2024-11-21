package com.Meditation.Sounds.frequencies.lemeor.ui.programs.detail

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.feature.base.BaseFragment
import com.Meditation.Sounds.frequencies.lemeor.FAVORITES
import com.Meditation.Sounds.frequencies.lemeor.albumIdBackProgram
import com.Meditation.Sounds.frequencies.lemeor.categoryIdBackProgram
import com.Meditation.Sounds.frequencies.lemeor.currentTrack
import com.Meditation.Sounds.frequencies.lemeor.currentTrackIndex
import com.Meditation.Sounds.frequencies.lemeor.data.api.RetrofitBuilder
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.database.dao.ProgramDao
import com.Meditation.Sounds.frequencies.lemeor.data.model.Program
import com.Meditation.Sounds.frequencies.lemeor.data.model.Scalar
import com.Meditation.Sounds.frequencies.lemeor.data.model.Search
import com.Meditation.Sounds.frequencies.lemeor.data.model.Track
import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper
import com.Meditation.Sounds.frequencies.lemeor.data.utils.ViewModelFactory
import com.Meditation.Sounds.frequencies.lemeor.getConvertedTime
import com.Meditation.Sounds.frequencies.lemeor.getPreloadedSaveDir
import com.Meditation.Sounds.frequencies.lemeor.getSaveDir
import com.Meditation.Sounds.frequencies.lemeor.isMultiPlay
import com.Meditation.Sounds.frequencies.lemeor.isNoReloadCurrentTrackIndex
import com.Meditation.Sounds.frequencies.lemeor.isPlayAlbum
import com.Meditation.Sounds.frequencies.lemeor.isPlayProgram
import com.Meditation.Sounds.frequencies.lemeor.isTrackAdd
import com.Meditation.Sounds.frequencies.lemeor.isUserPaused
import com.Meditation.Sounds.frequencies.lemeor.playAlbumId
import com.Meditation.Sounds.frequencies.lemeor.playListScalar
import com.Meditation.Sounds.frequencies.lemeor.playProgramId
import com.Meditation.Sounds.frequencies.lemeor.playRife
import com.Meditation.Sounds.frequencies.lemeor.playScalar
import com.Meditation.Sounds.frequencies.lemeor.positionFor
import com.Meditation.Sounds.frequencies.lemeor.programName
import com.Meditation.Sounds.frequencies.lemeor.rifeBackProgram
import com.Meditation.Sounds.frequencies.lemeor.selectedNaviFragment
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper
import com.Meditation.Sounds.frequencies.lemeor.tools.downloader.DownloadService
import com.Meditation.Sounds.frequencies.lemeor.tools.downloader.DownloaderActivity
import com.Meditation.Sounds.frequencies.lemeor.tools.player.MusicRepository
import com.Meditation.Sounds.frequencies.lemeor.tools.player.PlayerPlayAction
import com.Meditation.Sounds.frequencies.lemeor.tools.player.PlayerSelected
import com.Meditation.Sounds.frequencies.lemeor.tools.player.PlayerService
import com.Meditation.Sounds.frequencies.lemeor.tools.player.PlayerService.Companion.musicRepository
import com.Meditation.Sounds.frequencies.lemeor.tools.player.ScalarPlayerService
import com.Meditation.Sounds.frequencies.lemeor.trackList
import com.Meditation.Sounds.frequencies.lemeor.typeBack
import com.Meditation.Sounds.frequencies.lemeor.ui.albums.detail.NewAlbumDetailFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.main.NavigationActivity
import com.Meditation.Sounds.frequencies.lemeor.ui.main.UpdateTrack
import com.Meditation.Sounds.frequencies.lemeor.ui.programs.NewProgramFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.programs.NewProgramViewModel
import com.Meditation.Sounds.frequencies.lemeor.ui.programs.dialog.FrequenciesDialogFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.programs.search.AddProgramsFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.scalar.ScalarDownloadService
import com.Meditation.Sounds.frequencies.models.event.ScheduleProgramStatusEvent
import com.Meditation.Sounds.frequencies.models.event.UpdateSwitchQuantumEvent
import com.Meditation.Sounds.frequencies.utils.Constants
import com.Meditation.Sounds.frequencies.utils.Constants.Companion.PREF_SETTING_ADVANCE_SCALAR_ON_OFF
import com.Meditation.Sounds.frequencies.utils.QcAlarmManager
import com.Meditation.Sounds.frequencies.utils.SharedPreferenceHelper
import com.Meditation.Sounds.frequencies.utils.Utils
import com.Meditation.Sounds.frequencies.utils.isNotString
import com.Meditation.Sounds.frequencies.views.ItemLastOffsetBottomDecoration
import kotlinx.android.synthetic.main.fragment_program_detail.action_add_silent_quantum
import kotlinx.android.synthetic.main.fragment_program_detail.action_frequencies
import kotlinx.android.synthetic.main.fragment_program_detail.action_quantum
import kotlinx.android.synthetic.main.fragment_program_detail.action_rife
import kotlinx.android.synthetic.main.fragment_program_detail.btnSwitchSchedule
import kotlinx.android.synthetic.main.fragment_program_detail.fabOption
import kotlinx.android.synthetic.main.fragment_program_detail.program_back
import kotlinx.android.synthetic.main.fragment_program_detail.program_name
import kotlinx.android.synthetic.main.fragment_program_detail.program_play
import kotlinx.android.synthetic.main.fragment_program_detail.program_time
import kotlinx.android.synthetic.main.fragment_program_detail.program_tracks_recycler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.util.Collections
import java.util.Date


class ProgramDetailFragment : BaseFragment() {

    val programId: Int by lazy {
        arguments?.getInt(ARG_PROGRAM_ID)
            ?: throw IllegalArgumentException("Must call through newInstance()")
    }

    private lateinit var mViewModel: ProgramDetailViewModel
    private lateinit var mNewProgramViewModel: NewProgramViewModel
    private var mTracks: ArrayList<Any>? = null
    private var program: Program? = null
    private var isFirst = true
    private var isReload = false
    private var timeDelay = 500L
    private val tracks: ArrayList<Search> = ArrayList()

    private val programTrackAdapter by lazy {
        ProgramTrackAdapter(
            onClickItem = { item, _ ->
                if (playProgramId != programId) {
                    //clear scalar
                    if (playListScalar.isNotEmpty()) {
                        val lastScalar = playListScalar.last()
                        playScalar = lastScalar
                        playAndDownloadScalar(lastScalar)
                    }
                }
                isMultiPlay = false
                if (item.obj is Scalar) {
                    //play scalar
                    playProgramId = programId
                    playScalar = item.obj as Scalar
                    playAndDownloadScalar(item.obj as Scalar)
                } else {
                    //play quantum
                    val listTracks =
                        tracks.filter { it.obj !is Scalar }.map { it.obj } as ArrayList<Any>
                    play(listTracks)
                    Handler(Looper.getMainLooper()).postDelayed({
                        EventBus.getDefault().post(PlayerSelected(listTracks.indexOf(item.obj)))
                        timeDelay = 200L
                    }, timeDelay)
                }
            },
            onClickOptions = { item ->
                positionFor = item.id
                when (item.obj) {
                    is Track -> {
                        val t = item.obj as Track
                        startActivityForResult(
                            PopActivity.newIntent(
                                requireContext(), t.id.toDouble()
                            ), 1002
                        )
                    }

                    is MusicRepository.Frequency -> {
                        val f = item.obj as MusicRepository.Frequency
                        startActivityForResult(
                            PopActivity.newIntent(
                                requireContext(), f.frequency.toDouble()
                            ), 1002
                        )
                    }

                    is Scalar -> {
                        val t = item.obj as Scalar
                        startActivityForResult(
                            PopActivity.newIntent(
                                requireContext(), t.id.toDouble()
                            ), 1002
                        )
                    }
                }

            },
        )
    }

    private val itemDecoration by lazy {
        ItemLastOffsetBottomDecoration(resources.getDimensionPixelOffset(R.dimen.dp_70))
    }

    override fun initLayout() = R.layout.fragment_program_detail

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: Any?) {
        if (event == DownloadService.DOWNLOAD_FINISH) {
            program?.let { p ->
                try {
                    mViewModel.convertData(p) { list ->
                        mTracks?.clear()
                        mTracks?.addAll(list.map { it.obj })
                        program_play.text = getString(R.string.btn_play)
                    }
                } catch (_: Exception) {
                }
            }
        }

        if (event is String && event == "clear player") {
            programTrackAdapter.setSelectedItem(null)
        }

        if (event is UpdateSwitchQuantumEvent) {
            if (event.program?.id != programId) {
                isFirst = true
                programTrackAdapter.setSelectedItem(null)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun initComponents() {
        mViewModel = ViewModelProvider(
            this, ViewModelFactory(
                ApiHelper(RetrofitBuilder(requireContext()).apiService),
                DataBase.getInstance(requireContext())
            )
        )[ProgramDetailViewModel::class.java]

        mNewProgramViewModel = ViewModelProvider(
            this, ViewModelFactory(
                ApiHelper(RetrofitBuilder(requireContext()).apiService),
                DataBase.getInstance(requireContext())
            )
        )[NewProgramViewModel::class.java]

        program_tracks_recycler.apply {
            adapter = programTrackAdapter
            addItemDecoration(itemDecoration)
        }

        if (!SharedPreferenceHelper.getInstance().getBool(PREF_SETTING_ADVANCE_SCALAR_ON_OFF)) {
            action_add_silent_quantum.visibility = View.GONE
        }

        view?.isFocusableInTouchMode = true
        view?.requestFocus()
        view?.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                onBackPressed()
                true
            } else false
        }
    }

    override fun addListener() {
        program_back.setOnClickListener { onBackPressed() }

        program_play.setOnClickListener {
            if (tracks.size == 0) {
                Toast.makeText(
                    requireContext(), getString(R.string.tv_empty_list), Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            val list = tracks.filterIsInstance<Track>() as ArrayList<Track>
            if (list.isNotEmpty()) {
                playOrDownload(list)
            }

            //play freg
            val listFreg = tracks.filter { it.obj !is Scalar }.map { it.obj } as ArrayList<Any>
            if (listFreg.isNotEmpty()) {
                play(listFreg)
                programTrackAdapter.setSelectedItem(tracks.first { it.obj !is Scalar })
                EventBus.getDefault().post(PlayerSelected(0))
            } else {
                EventBus.getDefault().post("clear player")
            }

            //play scalar
            if (SharedPreferenceHelper.getInstance().getBool(PREF_SETTING_ADVANCE_SCALAR_ON_OFF)) {
                val listScalars =
                    tracks.filter { it.obj is Scalar && (it.obj as Scalar).is_free == 1}.map { it.obj as Scalar } as ArrayList<Scalar>
                if (listScalars.isNotEmpty()) {
                    val lastScalar = listScalars.last()
                    playScalar = lastScalar
                    listScalars.removeLast()
                    playListScalar.clear()
                    playListScalar.addAll(listScalars)
                    playAndDownloadScalar(lastScalar)
                } else {
                    //clear scalar
                    if (playListScalar.isNotEmpty()) {
                        val lastScalar = playListScalar.last()
                        playScalar = lastScalar
//                    playListScalar.clear()
//                    playingScalar = false
                        playAndDownloadScalar(lastScalar)
                    }
                }
            }
        }

        action_rife.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.trans_right_to_left_in, R.anim.trans_right_to_left_out)
                .replace(
                    R.id.nav_host_fragment,
                    AddProgramsFragment.newInstance(programId, 1),
                    AddProgramsFragment.newInstance(programId).javaClass.simpleName
                ).commit()
        }

        action_quantum.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.trans_right_to_left_in, R.anim.trans_right_to_left_out)
                .replace(
                    R.id.nav_host_fragment,
                    AddProgramsFragment.newInstance(programId),
                    AddProgramsFragment.newInstance(programId).javaClass.simpleName
                ).commit()
        }

        action_frequencies.setOnClickListener {
            fabOption.collapse()
            FrequenciesDialogFragment.newInstance(listener = { f, m ->
                mNewProgramViewModel.addFrequencyToProgram(programId, f)
                m.dismiss()
                isFirst = true
            }).showAllowingStateLoss(childFragmentManager)
        }

        action_add_silent_quantum.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.trans_right_to_left_in, R.anim.trans_right_to_left_out)
                .replace(
                    R.id.nav_host_fragment,
                    AddProgramsFragment.newInstance(programId, isSilentQuantum = 2),
                    AddProgramsFragment.newInstance(programId).javaClass.simpleName
                ).commit()
        }

        mViewModel.program(programId).observe(viewLifecycleOwner) {
            if (it != null && it.id != 0) {
                programTrackAdapter.isMy = it.isMy
                program = it
                initView(it)
            }
        }

        btnSwitchSchedule.isSelected =
            PreferenceHelper.getScheduleProgram(requireContext())?.id == programId && SharedPreferenceHelper.getInstance()
                .getBool(Constants.PREF_SCHEDULE_PROGRAM_STATUS)
        btnSwitchSchedule.setOnClickListener {
            if (btnSwitchSchedule.isSelected) {
                isFirst = true
                if (!isPlayAlbum && isPlayProgram && playProgramId == PreferenceHelper.getScheduleProgram(requireContext())?.id && QcAlarmManager.isCurrentTimeInRange()) {
                    playListScalar.clear()
                    programTrackAdapter.setSelectedItem(null)
                }
                PreferenceHelper.saveScheduleProgram(requireContext(), null)
                if (QcAlarmManager.isCurrentTimeInRange()) {
                    EventBus.getDefault().post(ScheduleProgramStatusEvent(isPlay = false, isHidePlayer = true, isClearScheduleProgram = true))
                }
            } else {
                PreferenceHelper.saveScheduleProgram(requireContext(), program)
            }
            btnSwitchSchedule.isSelected = !btnSwitchSchedule.isSelected
            SharedPreferenceHelper.getInstance().setBool(Constants.PREF_SCHEDULE_PROGRAM_STATUS, btnSwitchSchedule.isSelected)
            QcAlarmManager.setScheduleProgramsAlarms(requireContext())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        isTrackAdd = false
        albumIdBackProgram = -1
        categoryIdBackProgram = -1
    }

    private fun onBackPressed() {
        var fragment: Fragment?

        if (isTrackAdd) {
            fragment = if (typeBack == Constants.TYPE_ALBUM) {
                albumIdBackProgram?.let {
                    NewAlbumDetailFragment.newInstance(
                        it, categoryIdBackProgram!!
                    )
                }
            } else {
                rifeBackProgram?.let {
                    NewAlbumDetailFragment.newInstance(
                        0, categoryIdBackProgram!!, type = typeBack, rifeId = it.id
                    )
                }
            }
        } else {
            fragment = selectedNaviFragment
            if (fragment == null) {
                fragment = NewProgramFragment()
            }
        }

        parentFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.trans_left_to_right_in, R.anim.trans_left_to_right_out)
            .replace(R.id.nav_host_fragment, fragment!!, fragment.javaClass.simpleName).commit()
    }

    private fun initView(program: Program) {
        program_name.text = program.name
        programName = program.name
        programTrackAdapter.setProgram(program)
        mViewModel.convertData(program) { list ->
            if (tracks.size != list.size && isPlayProgram && playProgramId == program.id) {
                isFirst = false
                resetDataMyService(list.map { t -> t.obj } as ArrayList<Any>)
            }
            tracks.clear()
            tracks.addAll(list)
            programTrackAdapter.submitData(tracks)
            program_time.text = getString(
                R.string.total_time, getConvertedTime(tracks.sumOf {
                    if (it.obj is Track) 300000L
                    else 180000L
                })
            )
            mTracks?.clear()
            mTracks?.addAll(tracks.map { it.obj })

            if (currentTrack.value != null) {
                val track = currentTrack.value
                if (track is MusicRepository.Track) {
                    tracks.firstOrNull {
                        (it.obj is Track) && it.id == track.trackId
                    }?.let {
                        programTrackAdapter.setSelectedItem(it)
                    }
                }
            }

            program_play.text = getString(R.string.btn_play)

            currentTrackIndex.observe(viewLifecycleOwner) {
                val allTracks = tracks.filter { it.obj !is Scalar }
                if (allTracks.isNotEmpty() && allTracks.getOrNull(it) != null) {
                    val t = tracks.firstOrNull { item -> item.obj == allTracks[it].obj }
                    t?.let { item ->
                        if (playProgramId == programId) {
                            programTrackAdapter.setSelectedItem(item)
                        }
                    }
                }
            }
        }
    }

    private fun playOrDownload(tracks: ArrayList<Track>) {
        if (Utils.isConnectedToNetwork(requireContext())) {
            val list = ArrayList<Track>()
            CoroutineScope(Dispatchers.IO).launch {
                tracks.forEach { t ->
                    val file =
                        File(getSaveDir(requireContext(), t.filename, t.album?.audio_folder ?: ""))
                    val preloaded = File(
                        getPreloadedSaveDir(
                            requireContext(), t.filename, t.album?.audio_folder ?: ""
                        )
                    )

                    if (!file.exists() && !preloaded.exists()) {
                        var isExist = false
                        for (item in list) {
                            if (item.id == t.id) {
                                isExist = true
                                break
                            }
                        }
                        if (!isExist) {
                            list.add(t)
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    activity?.let {
                        DownloaderActivity.startDownload(it, list)
                    }
                }
            }
        }
    }

    fun play(tracks: ArrayList<Any>) {
        playRife = null
        val activity = activity as NavigationActivity

        if (isPlayAlbum || playProgramId != programId) {
            activity.hidePlayerUI()
        }

        isPlayAlbum = false
        playAlbumId = -1
        isPlayProgram = true
        playProgramId = programId
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
                            ScalarDownloadService.startService(context = requireContext(), scalar)
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
                            ScalarDownloadService.startService(context = requireContext(), scalar)
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
        playStopScalar("ADD_REMOVE")
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun playStopScalar(actionScalar: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val playIntent = Intent(context, ScalarPlayerService::class.java).apply {
                    action = actionScalar
                }
                requireActivity().startService(playIntent)
            } catch (_: Exception) {
            }
            CoroutineScope(Dispatchers.Main).launch { (activity as NavigationActivity).showPlayerUI() }
        }

        Handler(Looper.getMainLooper()).postDelayed({
            programTrackAdapter.notifyDataSetChanged()
        }, 300)
    }

    private fun resetDataMyService(tracks: ArrayList<Any>) {
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
    }

//    private fun getDuration(file: File): Long {
//        val mediaMetadataRetriever = MediaMetadataRetriever()
//        mediaMetadataRetriever.setDataSource(file.absolutePath)
//        val durationStr =
//            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
//        return durationStr?.toLong() ?: 0L
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1002 && resultCode == RESULT_OK && data != null) {
            isFirst = true
            val action = data.getStringExtra(PopActivity.EXTRA_ACTION)

            val db = DataBase.getInstance(requireContext())
            val programDao = db.programDao()
            val trackDao = db.trackDao()

            if (action.equals("track_remove")) {
                AlertDialog.Builder(requireActivity()).setTitle(R.string.app_name)
                    .setMessage(R.string.txt_confirm_delete_frequencies)
                    .setPositiveButton(R.string.txt_yes) { _, _ ->
                        val currentItemIndex = musicRepository?.currentItemIndex
                        mViewModel.checkProgramData(program) { l ->

                            if (isPlayProgram && playProgramId == program?.id && currentItemIndex != l.size - 1) {
                                isNoReloadCurrentTrackIndex = true
                            }

                            val list = ArrayList(l)
                            CoroutineScope(Dispatchers.IO).launch {
                                positionFor?.let { pos ->
                                    mTracks?.get(pos)?.let { item ->
                                        if (item is Track) {
                                            item.id.let { it1 ->
                                                trackDao.isTrackFavorite(
                                                    false, it1
                                                )
                                            }
                                        }
                                    }
                                    var isTrackScalar = false
                                    val trackId = list[pos]
                                    if (trackId.contains("-scalar")) {
                                        isTrackScalar = true
                                        val scalarId = trackId.replace("-scalar", "")
                                        val scalarPlaying =
                                            playListScalar.firstOrNull { it.id == scalarId }
                                        if (scalarPlaying != null) {
                                            removeScalar(scalarPlaying)
                                        }
                                    }
                                    list.removeAt(pos)
                                    program?.records = list
                                    program?.let {
                                        programDao.updateProgram(it.copy(updated_at = Date().time))
                                        if (it.user_id.isNotEmpty()) {
                                            try {
                                                mNewProgramViewModel.updateTrackToProgram(
                                                    UpdateTrack(
                                                        track_id = listOf(trackId),
                                                        id = it.id,
                                                        track_type = if (trackId.isNotString()) "mp3" else "rife",
                                                        request_type = "remove",
                                                        is_favorite = (it.name.uppercase() == FAVORITES.uppercase() && it.favorited)
                                                    )
                                                )
                                            } catch (_: Exception) {
                                            }
                                        }
                                    }

                                    //handle player when track removed
                                    if (isPlayProgram && playProgramId == program?.id) {
                                        mViewModel.convertData(program!!) { list ->
                                            if (list.none { it.obj !is Scalar }) {
                                                EventBus.getDefault().post("clear player")
                                            } else {
                                                if (currentItemIndex == pos) {
                                                    if (!isUserPaused) {
                                                        play(tracks.filter { it.obj !is Scalar }
                                                            .map { it.obj } as ArrayList<Any>)
                                                        Handler(Looper.getMainLooper()).postDelayed(
                                                            {
                                                                EventBus.getDefault()
                                                                    .post(PlayerSelected(pos))
                                                                timeDelay = 200L
                                                            },
                                                            timeDelay
                                                        )
                                                    }
                                                    EventBus.getDefault()
                                                        .post(PlayerPlayAction(isLastPlaying = true))
                                                } else if ((currentItemIndex ?: 0) > pos) {
                                                    if (!isTrackScalar) {
                                                        musicRepository?.currentItemIndex =
                                                            (musicRepository?.currentItemIndex
                                                                ?: 0) - 1
                                                    }
                                                    EventBus.getDefault().post(PlayerPlayAction())
                                                } else {
                                                    EventBus.getDefault().post(PlayerPlayAction())
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }.setNegativeButton(R.string.txt_no) { _, _ ->

                    }.show()

            } else {
                val activity = activity as NavigationActivity
                isPlayAlbum = false
                isPlayProgram = false
                activity.hidePlayerUI()

                CoroutineScope(Dispatchers.IO).launch {
                    val list = program?.records ?: arrayListOf()
                    when {
                        action.equals("track_move_up") -> {
                            if (positionFor == 0) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    Toast.makeText(
                                        requireContext(),
                                        getString(R.string.tv_track_first_pos),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                return@launch
                            }
                            moveTrack(list, true, programDao)
                        }

                        action.equals("track_move_down") -> {
                            if (positionFor == list.size - 1) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    Toast.makeText(
                                        requireContext(),
                                        getString(R.string.tv_track_last_pos),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                return@launch
                            }
                            moveTrack(list, false, programDao)
                        }
                    }
                }
            }
        }
    }

    private suspend fun moveTrack(
        list: MutableList<String>, isMoveUp: Boolean, programDao: ProgramDao
    ) {
        val positionFrom = positionFor!!
        val positionTo = if (isMoveUp) {
            positionFor!! - 1
        } else {
            positionFor!! + 1
        }
        Collections.swap(list, positionFrom, positionTo)
        program?.let {
            it.records = list as java.util.ArrayList<String>
            programDao.updateProgram(it.copy(updated_at = Date().time))
        }
    }

    private fun removeScalar(scalarRemove: Scalar) {
        Handler(Looper.getMainLooper()).postDelayed({
            if (playListScalar.size == 1) {
                val lastScalar = playListScalar.last()
                playScalar = lastScalar
//                playListScalar.clear()
//                playingScalar = false
                playAndDownloadScalar(lastScalar)
            } else {
                val listScalars = ArrayList(playListScalar)
                listScalars.remove(scalarRemove)
                playListScalar.clear()
                val lastScalar = listScalars.last()
                playScalar = lastScalar
                listScalars.removeLast()
                playListScalar.addAll(listScalars)
                if (listScalars.isNotEmpty()) {
                    playAndDownloadScalar(listScalars.last())
                } else {
                    playAndDownloadScalar(lastScalar)
                }
            }

        }, 1000L)
    }

    companion object {
        const val ARG_PROGRAM_ID = "arg_program"

        @JvmStatic
        fun newInstance(id: Int) = ProgramDetailFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_PROGRAM_ID, id)
            }
        }
    }
}