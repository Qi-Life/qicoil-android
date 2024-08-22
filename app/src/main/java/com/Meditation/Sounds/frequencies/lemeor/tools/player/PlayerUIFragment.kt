package com.Meditation.Sounds.frequencies.lemeor.tools.player

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.*
import com.Meditation.Sounds.frequencies.lemeor.ui.albums.detail.NewAlbumDetailFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.base.NewBaseFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.programs.detail.ProgramDetailFragment
import com.Meditation.Sounds.frequencies.utils.Constants
import com.google.android.exoplayer2.Player
import kotlinx.android.synthetic.main.player_ui_fragment.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class PlayerUIFragment : NewBaseFragment() {
    private var playerServiceBinder: PlayerService.PlayerServiceBinder? = null
    private var mediaController: MediaControllerCompat? = null
    private var callback: MediaControllerCompat.Callback =
        object : MediaControllerCompat.Callback() {
            override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
                if (state == null) return
                playing = state.state == PlaybackStateCompat.STATE_PLAYING
                player_play?.post {
                    if (playing) {
                        EventBus.getDefault().post("play Album")
                        player_play.setImageDrawable(
                            getDrawable(
                                requireActivity().applicationContext,
                                R.drawable.oc_pause_song
                            )
                        )
                    } else {
                        player_play.setImageDrawable(
                            getDrawable(
                                requireActivity().applicationContext,
                                R.drawable.ic_play_song
                            )
                        )
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
                    else mediaController?.transportControls?.play()
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
    private var repeat: Int = Player.REPEAT_MODE_ONE
    private var shuffle: Boolean = false
    private var isSeeking = false
    private var isTrack = true

    //Scalar
    private var playerServiceBinderScalar: ScalarPlayerService.PlayerServiceBinder? = null
    private var playingScalar: Boolean = false
    private var mediaScalarController: MediaControllerCompat? = null
    private var callbackScalar: MediaControllerCompat.Callback =
        object : MediaControllerCompat.Callback() {
            override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
                if (state == null) return
                playingScalar = state.state == PlaybackStateCompat.STATE_PLAYING
                player_play_scalar?.post {
                    if (playingScalar) {
                        player_play_scalar.setImageDrawable(
                            getDrawable(
                                requireActivity().applicationContext,
                                R.drawable.oc_pause_song
                            )
                        )
                    } else {
                        player_play_scalar.setImageDrawable(
                            getDrawable(
                                requireActivity().applicationContext,
                                R.drawable.ic_play_song
                            )
                        )
                    }
                }
            }
        }

    private var serviceConnectionScalar = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            playerServiceBinderScalar = binder as ScalarPlayerService.PlayerServiceBinder
            try {
                mediaScalarController = MediaControllerCompat(
                    requireContext(),
                    playerServiceBinderScalar!!.mediaSessionToken
                )
                mediaScalarController?.registerCallback(callbackScalar)
                callbackScalar.onPlaybackStateChanged(mediaScalarController!!.playbackState)

                if (mediaScalarController != null) {
                    if (playing) mediaScalarController?.transportControls?.pause()
                    else mediaScalarController?.transportControls?.play()
                }
            } catch (e: RemoteException) {
                mediaScalarController = null
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            playerServiceBinder = null
            mediaScalarController?.unregisterCallback(callbackScalar)
            mediaScalarController = null
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.player_ui_fragment, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireContext().bindService(
            Intent(requireContext(), PlayerService::class.java),
            serviceConnection as ServiceConnection,
            AppCompatActivity.BIND_AUTO_CREATE
        )
        requireContext().bindService(
            Intent(requireContext(), ScalarPlayerService::class.java),
            serviceConnectionScalar as ServiceConnection,
            AppCompatActivity.BIND_AUTO_CREATE
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: Any?) {
        if (event is String && event == "play Rife") {
            val rotation: Animation =
                AnimationUtils.loadAnimation(requireContext(), R.anim.clockwise_rotation)
            rotation.repeatCount = Animation.INFINITE
            player_repeat.clearAnimation()
            if (mediaController != null)
                if (playing) {
                    isUserPaused = true
                    mediaController?.transportControls?.pause()
                }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        playerInit()
        setListeners()
    }

    private fun setListeners() {
        currentTrack.observe(viewLifecycleOwner) {
            setUI(it)
            if(it is MusicRepository.Track){
                isTrack = true
                track_name.text = it.title

                loadImage(requireContext(), track_image, it.album)
                Log.i("currenttracl", "t-->" + it.duration)
            }else if(it is MusicRepository.Frequency){
                isTrack = false
                track_name.text = it.frequency.toString()
                track_image.setImageResource(R.drawable.frequency_v2)
                Log.i("currenttracl", "t-->" + it.duration)
            }
        }

        currentPosition.observe(viewLifecycleOwner) {
            track_position.text = getConvertedTime(it)
            if(!isSeeking) {
                seekBar.progress = it.toInt()
            }
        }
        max.observe(viewLifecycleOwner) {
            seekBar.max = it.toInt()
        }

        duration.observe(viewLifecycleOwner) {
            seekBar.isEnabled = it > 0
            track_duration.text = getConvertedTime(it)
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar, p1: Int, p2: Boolean) {}

            override fun onStartTrackingTouch(p0: SeekBar) {
                if (isTrack) {
                    isSeeking = true
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (isTrack) {
                    isSeeking = false
                    EventBus.getDefault().post(PlayerSeek(seekBar.progress))
                }
            }
        })
    }

    private fun setUI(obj: Any) {
        viewAlbum.setOnClickListener {
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
        } else {
            if (obj is MusicRepository.Track) {
                track_title.text = obj.album.name
            } else if (obj is MusicRepository.Frequency) {
                track_title.text = obj.title
            }
        }
    }

    private fun playerInit() {
        player_play.setOnClickListener {
            val rotation: Animation =
                AnimationUtils.loadAnimation(requireContext(), R.anim.clockwise_rotation)
            rotation.repeatCount = Animation.INFINITE
            player_repeat.clearAnimation()
            if (mediaController != null)
                if (playing) {
                    isUserPaused = true
                    mediaController?.transportControls?.pause()
                } else {
                    isUserPaused = false
                    mediaController?.transportControls?.play()
                    if (repeat == Player.REPEAT_MODE_ALL) {
                        player_repeat.startAnimation(rotation)
                    }
                }
        }

        player_play_scalar.setOnClickListener {
            if (mediaScalarController != null)
                if (playingScalar) {
//                    isUserPaused = true
                    mediaScalarController?.transportControls?.pause()
                } else {
//                    isUserPaused = false
                    mediaScalarController?.transportControls?.play()
                }
        }

        player_next.setOnClickListener {
            if (mediaController != null)
                mediaController?.transportControls?.skipToNext()
            isMultiPlay = false
        }

        player_previous.setOnClickListener {
            if (mediaController != null)
                mediaController?.transportControls?.skipToPrevious()
        }

        player_shuffle.setOnClickListener {
            if (!shuffle) {
                shuffle = true
                player_shuffle.setImageResource(R.drawable.ic_shuffle_selected)
            } else {
                shuffle = false
                player_shuffle.setImageResource(R.drawable.ic_shuffle)
            }
            EventBus.getDefault().post(PlayerShuffle(shuffle))
        }

        player_repeat.setOnClickListener {
            val rotation: Animation =
                AnimationUtils.loadAnimation(requireContext(), R.anim.clockwise_rotation)
            rotation.repeatCount = Animation.INFINITE
            player_repeat.clearAnimation()
            when (repeat) {
                Player.REPEAT_MODE_OFF -> {
                    repeat = Player.REPEAT_MODE_ONE
                    player_repeat.setImageResource(R.drawable.ic_loop_one)
                }

                Player.REPEAT_MODE_ONE -> {
                    repeat = Player.REPEAT_MODE_ALL
                    player_repeat.setImageResource(R.drawable.ic_loop_all)
                    if (playing) {
                        player_repeat.startAnimation(rotation)
                    }
                }

                Player.REPEAT_MODE_ALL -> {
                    repeat = Player.REPEAT_MODE_OFF
                    player_repeat.setImageResource(R.drawable.ic_loop_off)
                }
            }
            EventBus.getDefault().post(PlayerRepeat(repeat))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaController != null)
            mediaController?.transportControls?.stop()
        playerServiceBinder = null
        mediaController?.unregisterCallback(callback)
        mediaController = null
        serviceConnection.let { requireContext().unbindService(it) }
        serviceConnectionScalar.let { requireContext().unbindService(it) }
    }
}