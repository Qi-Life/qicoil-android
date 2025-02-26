package com.Meditation.Sounds.frequencies.lemeor.tools.player

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.RemoteException
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.asFlow
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.currentPosition
import com.Meditation.Sounds.frequencies.lemeor.currentTrack
import com.Meditation.Sounds.frequencies.lemeor.duration
import com.Meditation.Sounds.frequencies.lemeor.getConvertedTime
import com.Meditation.Sounds.frequencies.lemeor.isMultiPlay
import com.Meditation.Sounds.frequencies.lemeor.isUserPaused
import com.Meditation.Sounds.frequencies.lemeor.loadImage
import com.Meditation.Sounds.frequencies.lemeor.loadImageScalar
import com.Meditation.Sounds.frequencies.lemeor.max
import com.Meditation.Sounds.frequencies.lemeor.playListScalar
import com.Meditation.Sounds.frequencies.lemeor.playProgramId
import com.Meditation.Sounds.frequencies.lemeor.playingScalar
import com.Meditation.Sounds.frequencies.lemeor.programName
import com.Meditation.Sounds.frequencies.lemeor.tools.player.PlayerService.Companion.musicRepository
import com.Meditation.Sounds.frequencies.lemeor.trackList
import com.Meditation.Sounds.frequencies.lemeor.ui.albums.detail.NewAlbumDetailFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.base.NewBaseFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.main.NavigationActivity
import com.Meditation.Sounds.frequencies.lemeor.ui.programs.detail.ProgramDetailFragment
import com.Meditation.Sounds.frequencies.models.event.UpdateViewSilentQuantumEvent
import com.Meditation.Sounds.frequencies.utils.Constants
import com.Meditation.Sounds.frequencies.utils.Constants.Companion.PREF_SETTING_ADVANCE_SCALAR_ON_OFF
import com.Meditation.Sounds.frequencies.utils.SharedPreferenceHelper
import com.Meditation.Sounds.frequencies.utils.Utils
import com.google.android.exoplayer2.Player
import kotlinx.android.synthetic.main.player_ui_fragment.player_next
import kotlinx.android.synthetic.main.player_ui_fragment.player_play
import kotlinx.android.synthetic.main.player_ui_fragment.player_play_scalar
import kotlinx.android.synthetic.main.player_ui_fragment.player_previous
import kotlinx.android.synthetic.main.player_ui_fragment.player_repeat
import kotlinx.android.synthetic.main.player_ui_fragment.player_shuffle
import kotlinx.android.synthetic.main.player_ui_fragment.seekBar
import kotlinx.android.synthetic.main.player_ui_fragment.track_duration
import kotlinx.android.synthetic.main.player_ui_fragment.track_image
import kotlinx.android.synthetic.main.player_ui_fragment.track_image_scalar
import kotlinx.android.synthetic.main.player_ui_fragment.track_name
import kotlinx.android.synthetic.main.player_ui_fragment.track_name_scalar
import kotlinx.android.synthetic.main.player_ui_fragment.track_position
import kotlinx.android.synthetic.main.player_ui_fragment.track_title
import kotlinx.android.synthetic.main.player_ui_fragment.tv_scalar_play_status
import kotlinx.android.synthetic.main.player_ui_fragment.viewPlayerScalar
import kotlinx.android.synthetic.main.player_ui_fragment.view_album_info
import kotlinx.android.synthetic.main.player_ui_fragment.view_album_scalar
import kotlinx.android.synthetic.main.player_ui_fragment.view_scalar_status_playing
import kotlinx.android.synthetic.main.player_ui_fragment.view_scalar_status_stoping
import kotlinx.android.synthetic.main.player_ui_fragment.view_space_when_slient_quantum_gone
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class PlayerUIFragment : NewBaseFragment() {
    private var isScreenRotation = false
    private var playerServiceBinder: PlayerService.PlayerServiceBinder? = null
    private var mediaController: MediaControllerCompat? = null
    private var callback: MediaControllerCompat.Callback =
        object : MediaControllerCompat.Callback() {
            override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
                if (state == null || trackList.isNullOrEmpty() || !isAdded) return
                playing = state.state == PlaybackStateCompat.STATE_PLAYING
                player_play?.post {
                    if (playing) {
                        EventBus.getDefault().post("play Album")
                        player_play.setImageDrawable(
                            getDrawable(
                                requireActivity().applicationContext,
                                R.drawable.bg_pause_song
                            )
                        )
                    } else {
                        player_play.setImageDrawable(
                            getDrawable(
                                requireActivity().applicationContext,
                                R.drawable.bg_play_song
                            )
                        )
                    }
                    player_next?.setImageResource(R.drawable.bg_next_song_new)

                    val orientation = resources.configuration.orientation
                    when (repeat) {
                        Player.REPEAT_MODE_OFF -> {
                            if (Utils.isTablet(requireContext()) && orientation == Configuration.ORIENTATION_LANDSCAPE && viewPlayerScalar.visibility == View.VISIBLE) {
                                player_repeat.setImageResource(R.drawable.bg_repeat_land_off)
                            } else {
                                player_repeat.setImageResource(R.drawable.bg_repeat_off)
                            }
                        }

                        Player.REPEAT_MODE_ONE -> {
                            if (Utils.isTablet(requireContext()) && orientation == Configuration.ORIENTATION_LANDSCAPE && viewPlayerScalar.visibility == View.VISIBLE) {
                                player_repeat.setImageResource(R.drawable.bg_repeat_land_one)
                            } else {
                                player_repeat.setImageResource(R.drawable.bg_repeat_one)
                            }
                        }

                        Player.REPEAT_MODE_ALL -> {
                            if (Utils.isTablet(requireContext()) && orientation == Configuration.ORIENTATION_LANDSCAPE && viewPlayerScalar.visibility == View.VISIBLE) {
                                player_repeat.setImageResource(R.drawable.bg_repeat_land_all)
                            } else {
                                player_repeat.setImageResource(R.drawable.bg_repeat_all)
                            }
                        }
                    }
                    if (!shuffle) {
                        player_shuffle.setImageResource(R.drawable.bg_shuffle_new_off)
                    } else {
                        player_shuffle.setImageResource(R.drawable.bg_shuffle_new_on)
                    }
                }
            }
        }

    private var serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            playerServiceBinder = binder as PlayerService.PlayerServiceBinder
            try {
                mediaController = MediaControllerCompat(
                    requireContext(),
                    playerServiceBinder!!.mediaSessionToken
                )
                mediaController?.registerCallback(callback)
                callback.onPlaybackStateChanged(mediaController!!.playbackState)

                if (mediaController != null) {
                    if (playing) mediaController?.transportControls?.pause()
                    else {
                        if (trackList != null && trackList?.isNotEmpty() == true) {
                            mediaController?.transportControls?.play()
                        }
                    }
                }
            } catch (e: RemoteException) {
                mediaController = null
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            playerServiceBinder = null
            mediaController?.unregisterCallback(callback)
            mediaController = null
        }
    }
    private var playing: Boolean = false
    private var repeat: Int = Player.REPEAT_MODE_ALL
    private var shuffle: Boolean = false
    private var isSeeking = false
    private var isTrack = true

    //Scalar
    private var indexAlbumScalar = -1
    private val handlerDisplayAlbum = Handler()
    private val runnableDisplayAlbum = object : Runnable {
        override fun run() {
            if (indexAlbumScalar < playListScalar.size) {
                updateViewAlbumScalar()
                indexAlbumScalar++
                handlerDisplayAlbum.postDelayed(this, 2000)
            }
        }
    }
    private var playerServiceBinderScalar: SilentQuantumPlayerService.PlayerServiceBinder? = null
    private var mediaScalarController: MediaControllerCompat? = null
    private var callbackScalar: MediaControllerCompat.Callback =
        object : MediaControllerCompat.Callback() {
            override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
                if (state == null || !isAdded) return
                playingScalar =
                    state.state == PlaybackStateCompat.STATE_PLAYING && playListScalar.isNotEmpty()
                EventBus.getDefault().post(ScalarPlayerStatus())
                player_play_scalar?.post {
                    if (playingScalar) {
                        player_play_scalar.setImageDrawable(
                            getDrawable(
                                requireActivity().applicationContext,
                                R.drawable.bg_silent_scalar_on
                            )
                        )
                    } else {
                        player_play_scalar.setImageDrawable(
                            getDrawable(
                                requireActivity().applicationContext,
                                R.drawable.bg_silent_scalar_off
                            )
                        )
                    }
                }
                updateViewPlayerScalar()
            }
        }

    private var serviceConnectionScalar = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            playerServiceBinderScalar = binder as SilentQuantumPlayerService.PlayerServiceBinder
            try {
                mediaScalarController = MediaControllerCompat(
                    requireContext(),
                    playerServiceBinderScalar!!.mediaSessionToken
                )
                mediaScalarController?.registerCallback(callbackScalar)
                callbackScalar.onPlaybackStateChanged(mediaScalarController!!.playbackState)
//                if (mediaScalarController != null) {
//                    if (playingScalar) mediaScalarController?.transportControls?.pause()
//                    else mediaScalarController?.transportControls?.play()
//                }
            } catch (e: RemoteException) {
                mediaScalarController = null
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            playerServiceBinderScalar = null
            mediaScalarController?.unregisterCallback(callbackScalar)
            mediaScalarController = null
        }
    }

    private var currentView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        currentView = inflater.inflate(R.layout.player_ui_fragment, container, false)
        return currentView
    }

    @Suppress("DEPRECATION")
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        isScreenRotation = true
        parentFragmentManager.beginTransaction().detach(this).commitAllowingStateLoss()
        Handler().postDelayed({
            super.onConfigurationChanged(newConfig)
            parentFragmentManager.beginTransaction().attach(this).commitAllowingStateLoss()
        }, 500)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireContext().bindService(
            Intent(requireContext(), PlayerService::class.java),
            serviceConnection as ServiceConnection,
            AppCompatActivity.BIND_AUTO_CREATE
        )
        requireContext().bindService(
            Intent(requireContext(), SilentQuantumPlayerService::class.java),
            serviceConnectionScalar as ServiceConnection,
            AppCompatActivity.BIND_AUTO_CREATE
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: Any?) {
        if (event is String && (event == "play Rife" || event == "pause player")) {
            if (mediaController != null)
                if (playing) {
                    isUserPaused = true
                    mediaController?.transportControls?.pause()
                }
        }

        if (event is String && event == "play player") {
            if (mediaController != null)
                if (!playing) {
                    isUserPaused = false
                    mediaController?.transportControls?.play()
                }
        }

        if (event is String && event == "clear player") {
            if (mediaController != null)
                if (playing) {
//                    isUserPaused = true
                    mediaController?.transportControls?.pause()
                }
            Handler().postDelayed({
                setPlayerDefaultDisable()
            }, 500)
        }

        if (event is PlayerStatus) {
            if (event.isPause && mediaController != null) {
                isUserPaused = true
                mediaController?.transportControls?.pause()
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateViewSilentQuantumEvent(event: UpdateViewSilentQuantumEvent) {
        updateViewPlayerScalar()
        updateViewWhenRotation()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdatePlayerPlayEvent(event: PlayerPlayAction) {
        musicRepository?.getCurrent()
//        if (event.isLastPlaying) {
        if (playing) {
            playing = false
            player_play.performClick()
        }
//        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        playerInit()
        setListeners()
        updateViewPlayerScalar()
        if (isScreenRotation) {
            updateViewWhenRotation()
        } else {
            val orientation = resources.configuration.orientation
            if (Utils.isTablet(requireContext()) && orientation == Configuration.ORIENTATION_LANDSCAPE && viewPlayerScalar.visibility == View.VISIBLE) {
                player_repeat.setImageResource(R.drawable.ic_repeat_land_all_disable)
            } else {
                player_repeat.setImageResource(R.drawable.ic_repeat_all_disable)
            }
        }
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun setListeners() {
        currentTrack.observe(viewLifecycleOwner) { track ->
            track?.let {
                setUI(it)
                if (it is MusicRepository.Track) {
                    isTrack = true
                    track_name.text = it.title

                    loadImage(requireContext(), track_image, it.album)
                    Log.i("currenttracl", "t-->" + it.duration)
                } else if (it is MusicRepository.Frequency) {
                    isTrack = false
                    track_name.text = it.frequency.toString()
                    track_image.setImageResource(R.drawable.frequency_v2)
                    Log.i("currenttracl", "t-->" + it.duration)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            delay(1000)
            combine(
                currentPosition.asFlow(),
                max.asFlow()
            ) { a, b -> (Pair(a, b)) }.flowWithLifecycle(
                viewLifecycleOwner.lifecycle,
                Lifecycle.State.RESUMED
            ).collectLatest {
                val currentPosition = ((it.first / 1000).toInt() * 1000).toLong()
                val max = ((it.second / 1000).toInt() * 1000).toLong()

                track_position.text = getConvertedTime(currentPosition)
                if (!isSeeking) {
                    seekBar.progress = currentPosition.toInt()
                }
                seekBar.max = max.toInt()
                val duration = max - currentPosition
                seekBar.isEnabled = duration > 0
                track_duration.text = getConvertedTime(duration)
            }
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar, p1: Int, p2: Boolean) {}

            override fun onStartTrackingTouch(p0: SeekBar) {
//                if (isTrack) {
//
//                }
                isSeeking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
//                if (isTrack) {
//
//                }
                isSeeking = false
                EventBus.getDefault().post(PlayerSeek(seekBar.progress))
            }
        })
    }

    private fun setUI(obj: Any) {
        track_image.setOnClickListener {
            val fragmentList = requireActivity().supportFragmentManager.fragments
            if (playProgramId >= 0) {
                val programDetailFragment = fragmentList.lastOrNull {
                    it is ProgramDetailFragment
                } as ProgramDetailFragment?
                if (programDetailFragment != null) {
                    val programId = programDetailFragment.programId
                    if (programId != playProgramId) {
                        parentFragmentManager.beginTransaction().setCustomAnimations(
                            R.anim.trans_right_to_left_in,
                            R.anim.trans_right_to_left_out,
                            R.anim.trans_left_to_right_in,
                            R.anim.trans_left_to_right_out
                        ).replace(
                            R.id.nav_host_fragment,
                            ProgramDetailFragment.newInstance(playProgramId),
                            ProgramDetailFragment().javaClass.simpleName
                        ).commit()
                    }
                } else {
                    parentFragmentManager.beginTransaction().setCustomAnimations(
                        R.anim.trans_right_to_left_in,
                        R.anim.trans_right_to_left_out,
                        R.anim.trans_left_to_right_in,
                        R.anim.trans_left_to_right_out
                    ).replace(
                        R.id.nav_host_fragment,
                        ProgramDetailFragment.newInstance(playProgramId),
                        ProgramDetailFragment().javaClass.simpleName
                    ).commit()
                }
            } else {
                val newAlbumDetailFragment = fragmentList.lastOrNull {
                    it is NewAlbumDetailFragment
                } as NewAlbumDetailFragment?
                if (obj is MusicRepository.Track) {
                    if (newAlbumDetailFragment != null) {
                        val type = newAlbumDetailFragment.type

                        val albumId = newAlbumDetailFragment.albumId
                        val categoryId = newAlbumDetailFragment.categoryId

                        if (type != Constants.TYPE_ALBUM || albumId != obj.album.id || categoryId != obj.album.category_id) {
                            parentFragmentManager.beginTransaction().setCustomAnimations(
                                R.anim.trans_right_to_left_in,
                                R.anim.trans_right_to_left_out,
                                R.anim.trans_left_to_right_in,
                                R.anim.trans_left_to_right_out
                            ).replace(
                                R.id.nav_host_fragment,
                                NewAlbumDetailFragment.newInstance(
                                    obj.album.id,
                                    obj.album.category_id
                                ),
                                NewAlbumDetailFragment().javaClass.simpleName
                            ).commit()
                        }
                    } else {
                        parentFragmentManager.beginTransaction().setCustomAnimations(
                            R.anim.trans_right_to_left_in,
                            R.anim.trans_right_to_left_out,
                            R.anim.trans_left_to_right_in,
                            R.anim.trans_left_to_right_out
                        ).replace(
                            R.id.nav_host_fragment,
                            NewAlbumDetailFragment.newInstance(
                                obj.album.id,
                                obj.album.category_id
                            ),
                            NewAlbumDetailFragment().javaClass.simpleName
                        ).commit()
                    }
                } else if (obj is MusicRepository.Frequency) {
                    if (newAlbumDetailFragment != null) {
                        val type = newAlbumDetailFragment.type
                        val rifeId = newAlbumDetailFragment.rifeId
                        if (type != Constants.TYPE_RIFE || rifeId != obj.rifeId) {
                            parentFragmentManager.beginTransaction().setCustomAnimations(
                                R.anim.trans_right_to_left_in,
                                R.anim.trans_right_to_left_out,
                                R.anim.trans_left_to_right_in,
                                R.anim.trans_left_to_right_out
                            ).replace(
                                R.id.nav_host_fragment,
                                NewAlbumDetailFragment.newInstance(
                                    1,
                                    1,
                                    Constants.TYPE_RIFE,
                                    obj.rifeId
                                ),
                                NewAlbumDetailFragment().javaClass.simpleName
                            ).commit()
                        }
                    } else {
                        parentFragmentManager.beginTransaction().setCustomAnimations(
                            R.anim.trans_right_to_left_in,
                            R.anim.trans_right_to_left_out,
                            R.anim.trans_left_to_right_in,
                            R.anim.trans_left_to_right_out
                        ).replace(
                            R.id.nav_host_fragment,
                            NewAlbumDetailFragment.newInstance(
                                1,
                                1,
                                Constants.TYPE_RIFE,
                                obj.rifeId
                            ),
                            NewAlbumDetailFragment().javaClass.simpleName
                        ).commit()
                    }
                }
            }
        }
        if (playProgramId >= 0) {
            track_title.text = programName
            track_title.visibility = View.VISIBLE
        } else {
            if (obj is MusicRepository.Track) {
                track_title.text = obj.album.name
                track_title.visibility = View.VISIBLE
            } else if (obj is MusicRepository.Frequency) {
                track_title.text = obj.title
                track_title.visibility = View.VISIBLE
            }
        }
    }

    private fun playerInit() {
        if (trackList?.isEmpty() == true) {
            currentPosition.postValue(0)
        }
        player_play.setOnClickListener {
            if (trackList?.isNotEmpty() == true) {
                if (mediaController != null) {
                    if (playing) {
                        isUserPaused = true
                        mediaController?.transportControls?.pause()
                    } else {
                        isUserPaused = false
                        mediaController?.transportControls?.play()
                    }
                    EventBus.getDefault().post(PlayerStatus(isPlaying = true))
                }
            }
        }

        player_play_scalar.setOnClickListener {
            if (mediaScalarController != null && playListScalar.isNotEmpty()) {
                if (playingScalar) {
                    mediaScalarController?.transportControls?.pause()
                } else {
                    mediaScalarController?.transportControls?.play()
                }
            } else {
                (activity as NavigationActivity).onScalarSelect()
            }
        }

        player_next.setOnClickListener {
            if (trackList?.isNotEmpty() == true) {
                if (mediaController != null)
                    mediaController?.transportControls?.skipToNext()
                isMultiPlay = false
            }
        }

        player_previous.setOnClickListener {
            if (mediaController != null)
                mediaController?.transportControls?.skipToPrevious()
        }

        player_shuffle.setOnClickListener {
            if (trackList?.isNotEmpty() == true) {
                if (!shuffle) {
                    shuffle = true
                    player_shuffle.setImageResource(R.drawable.bg_shuffle_new_on)
                } else {
                    shuffle = false
                    player_shuffle.setImageResource(R.drawable.bg_shuffle_new_off)
                }
                EventBus.getDefault().post(PlayerShuffle(shuffle))
            }
        }

        player_repeat.setOnClickListener {
            if (trackList?.isNotEmpty() == true) {
                val orientation = resources.configuration.orientation
                when (repeat) {
                    Player.REPEAT_MODE_OFF -> {
                        repeat = Player.REPEAT_MODE_ONE
                        if (Utils.isTablet(requireContext()) && orientation == Configuration.ORIENTATION_LANDSCAPE && viewPlayerScalar.visibility == View.VISIBLE) {
                            player_repeat.setImageResource(R.drawable.bg_repeat_land_one)
                        } else {
                            player_repeat.setImageResource(R.drawable.bg_repeat_one)
                        }
                    }

                    Player.REPEAT_MODE_ONE -> {
                        repeat = Player.REPEAT_MODE_ALL
                        if (Utils.isTablet(requireContext()) && orientation == Configuration.ORIENTATION_LANDSCAPE && viewPlayerScalar.visibility == View.VISIBLE) {
                            player_repeat.setImageResource(R.drawable.bg_repeat_land_all)
                        } else {
                            player_repeat.setImageResource(R.drawable.bg_repeat_all)
                        }
                    }

                    Player.REPEAT_MODE_ALL -> {
                        repeat = Player.REPEAT_MODE_OFF
                        if (Utils.isTablet(requireContext()) && orientation == Configuration.ORIENTATION_LANDSCAPE && viewPlayerScalar.visibility == View.VISIBLE) {
                            player_repeat.setImageResource(R.drawable.bg_repeat_land_off)
                        } else {
                            player_repeat.setImageResource(R.drawable.bg_repeat_off)
                        }
                    }
                }
                EventBus.getDefault().post(PlayerRepeat(repeat))
            }
        }

        viewPlayerScalar.setOnClickListener {
            if (playListScalar.isEmpty()) {
                (activity as NavigationActivity).onScalarSelect()
            }
        }

        view_album_info.setOnClickListener {
            if (trackList == null || trackList?.isEmpty() == true) {
                (activity as NavigationActivity).onQuantumSelect()
            }
        }

        track_image.setOnClickListener {
            view_album_info.performClick()
        }
    }

    private fun updateViewWhenRotation() {
        isScreenRotation = false
        if (trackList?.isNotEmpty() == true) {
            val orientation = resources.configuration.orientation
            when (repeat) {
                Player.REPEAT_MODE_OFF -> {
                    if (Utils.isTablet(requireContext()) && orientation == Configuration.ORIENTATION_LANDSCAPE && viewPlayerScalar.visibility == View.VISIBLE) {
                        player_repeat.setImageResource(R.drawable.bg_repeat_land_off)
                    } else {
                        player_repeat.setImageResource(R.drawable.bg_repeat_off)
                    }
                }

                Player.REPEAT_MODE_ONE -> {
                    if (Utils.isTablet(requireContext()) && orientation == Configuration.ORIENTATION_LANDSCAPE && viewPlayerScalar.visibility == View.VISIBLE) {
                        player_repeat.setImageResource(R.drawable.bg_repeat_land_one)
                    } else {
                        player_repeat.setImageResource(R.drawable.bg_repeat_one)
                    }
                }

                Player.REPEAT_MODE_ALL -> {
                    if (Utils.isTablet(requireContext()) && orientation == Configuration.ORIENTATION_LANDSCAPE && viewPlayerScalar.visibility == View.VISIBLE) {
                        player_repeat.setImageResource(R.drawable.bg_repeat_land_all)
                    } else {
                        player_repeat.setImageResource(R.drawable.bg_repeat_all)
                    }
                }
            }
            player_next?.setImageResource(R.drawable.bg_next_song_new)
            if (playing) {
                player_play.setImageDrawable(
                    getDrawable(
                        requireActivity().applicationContext,
                        R.drawable.bg_pause_song
                    )
                )
            } else {
                player_play.setImageDrawable(
                    getDrawable(
                        requireActivity().applicationContext,
                        R.drawable.bg_play_song
                    )
                )
            }
            if (!shuffle) {
                player_shuffle.setImageResource(R.drawable.bg_shuffle_new_off)
            } else {
                player_shuffle.setImageResource(R.drawable.bg_shuffle_new_on)
            }
        }

        if (playingScalar) {
            view_scalar_status_stoping.visibility = View.INVISIBLE
            view_scalar_status_playing.visibility = View.VISIBLE
            tv_scalar_play_status.text = ""
            view_album_scalar.visibility = View.VISIBLE
            if (playListScalar.size == 1) {
                track_name_scalar.text = playListScalar.first().name
                loadImageScalar(
                    requireContext(),
                    track_image_scalar,
                    playListScalar.first()
                )
            }
            player_play_scalar.setImageDrawable(
                getDrawable(
                    requireActivity().applicationContext,
                    R.drawable.bg_silent_scalar_on
                )
            )
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setPlayerDefaultDisable() {
        trackList?.clear()
        track_image.setImageResource(R.drawable.ic_album_default_small)
        track_name.text = getString(R.string.tv_please_choose_a_frequency_to_play)
        track_title.visibility = View.GONE
        player_play.setImageResource(R.drawable.ic_play_song_disable)
        player_next.setImageResource(R.drawable.ic_next_song_new_disable)
        player_shuffle.setImageResource(R.drawable.ic_shuffle_new_off_disbale)
        val orientation = resources.configuration.orientation
        if (Utils.isTablet(requireContext()) && orientation == Configuration.ORIENTATION_LANDSCAPE && viewPlayerScalar.visibility == View.VISIBLE) {
            player_repeat.setImageResource(R.drawable.ic_repeat_land_all_disable)
        } else {
            player_repeat.setImageResource(R.drawable.ic_repeat_all_disable)
        }
        track_position.text = "00:00"
        track_duration.text = "00:00"
        seekBar.progress = 0
    }

    private fun updateViewPlayerScalar() {
        if (isAdded) {
            if (playingScalar) {
                tv_scalar_play_status.text = getString(R.string.tv_silent_scalar_turned_on)
                view_scalar_status_stoping.visibility = View.INVISIBLE
                view_scalar_status_playing.visibility = View.VISIBLE
                indexAlbumScalar = -1
                handlerDisplayAlbum.removeCallbacks(runnableDisplayAlbum)
                handlerDisplayAlbum.post(runnableDisplayAlbum)
            } else {
                view_scalar_status_stoping.visibility = View.VISIBLE
                view_scalar_status_playing.visibility = View.INVISIBLE
                handlerDisplayAlbum.removeCallbacks(runnableDisplayAlbum)
                view_album_scalar.visibility = View.GONE
                tv_scalar_play_status.text = getString(R.string.tv_silent_scalar_turned_off)
            }

            if (SharedPreferenceHelper.getInstance().getBool(PREF_SETTING_ADVANCE_SCALAR_ON_OFF)) {
                viewPlayerScalar.visibility = View.VISIBLE
                view_space_when_slient_quantum_gone.visibility = View.GONE
            } else {
                viewPlayerScalar.visibility = View.GONE
                view_space_when_slient_quantum_gone.visibility = View.VISIBLE
                if (playingScalar) {
                    mediaScalarController?.transportControls?.pause()
                    playingScalar = false
                    playListScalar.clear()
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateViewAlbumScalar() {
        if (isAdded) {
            if (indexAlbumScalar == -1) {
                tv_scalar_play_status.text = getString(R.string.tv_silent_scalar_turned_on)
                view_album_scalar.visibility = View.GONE
            } else if (playListScalar.size == 1) {
                tv_scalar_play_status.text = ""
                view_album_scalar.visibility = View.VISIBLE
                track_name_scalar.text = playListScalar.first().name
                loadImageScalar(requireContext(), track_image_scalar, playListScalar.first())
                handlerDisplayAlbum.removeCallbacks(runnableDisplayAlbum)
            } else {
                if (indexAlbumScalar < playListScalar.size) {
                    tv_scalar_play_status.text = ""
                    view_album_scalar.visibility = View.VISIBLE
                    track_name_scalar.text = playListScalar[indexAlbumScalar].name
                    loadImageScalar(
                        requireContext(),
                        track_image_scalar,
                        playListScalar[indexAlbumScalar]
                    )
                    if (indexAlbumScalar == playListScalar.size - 1) {
                        indexAlbumScalar = -1
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaController != null)
            mediaController?.transportControls?.stop()
        if (mediaScalarController != null)
            mediaScalarController?.transportControls?.stop()
        playerServiceBinder = null
        playerServiceBinderScalar = null
        mediaController?.unregisterCallback(callback)
        mediaScalarController?.unregisterCallback(callbackScalar)
        mediaController = null
        mediaScalarController = null
        serviceConnection.let { requireContext().unbindService(it) }
        serviceConnectionScalar.let { requireContext().unbindService(it) }
    }
}