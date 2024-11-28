package com.Meditation.Sounds.frequencies.lemeor.tools.player

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.data.model.Scalar
import com.Meditation.Sounds.frequencies.lemeor.getPreloadedSaveDir
import com.Meditation.Sounds.frequencies.lemeor.getSaveDir
import com.Meditation.Sounds.frequencies.lemeor.getTrackUrlScalar
import com.Meditation.Sounds.frequencies.lemeor.isPlayProgram
import com.Meditation.Sounds.frequencies.lemeor.noChangedList
import com.Meditation.Sounds.frequencies.lemeor.playListScalar
import com.Meditation.Sounds.frequencies.lemeor.playScalar
import com.Meditation.Sounds.frequencies.lemeor.trackList
import com.Meditation.Sounds.frequencies.models.ScalarMediaSource
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import java.io.File

class ScalarPlayerService : Service() {
    private val scalarSources = mutableListOf<ScalarMediaSource>()

    private val stateBuilder = PlaybackStateCompat.Builder().setActions(
        PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_STOP or PlaybackStateCompat.ACTION_PAUSE or PlaybackStateCompat.ACTION_PLAY_PAUSE
    )
    private val mediaSession: MediaSessionCompat by lazy {
        MediaSessionCompat(this, "ScalarPlayerService").apply {
            setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
            setCallback(mediaSessionCallback)
            isActive = true
        }
    }

    val exoPlayer: SimpleExoPlayer by lazy {
        SimpleExoPlayer.Builder(
            this,
            DefaultRenderersFactory(this),
        ).build()
    }
    var currentState = PlaybackStateCompat.STATE_STOPPED

    private val mediaSessionCallback: MediaSessionCompat.Callback =
        object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                exoPlayer.play()
                mediaSession.setPlaybackState(
                    stateBuilder.setState(
                        PlaybackStateCompat.STATE_PLAYING,
                        PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                        1f
                    ).build()
                )
                currentState = PlaybackStateCompat.STATE_PLAYING
            }

            override fun onPause() {
                exoPlayer.pause()
                mediaSession.setPlaybackState(
                    stateBuilder.setState(
                        PlaybackStateCompat.STATE_PAUSED,
                        PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                        1f
                    ).build()
                )
                currentState = PlaybackStateCompat.STATE_PAUSED
            }
        }

    override fun onBind(intent: Intent?): IBinder {
        return PlayerServiceBinder(mediaSession)
    }

    class PlayerServiceBinder(private val mediaSession: MediaSessionCompat) : Binder() {
        val mediaSessionToken: MediaSessionCompat.Token
            get() = mediaSession.sessionToken
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                "ADD_REMOVE" -> addMusic()
                "CLEAR" -> stopMusic()
            }
        }
        return START_STICKY
    }

    private fun addMusic() {
        playScalar?.let { scalar ->
            if (playListScalar.contains(scalar)) {
                removeMusic()
            } else {
                getUriScalar(scalar)?.let { uri ->
                    playListScalar.add(scalar)
                    scalarSources.add(ScalarMediaSource(scalar.name ,buildMediaSource(uri)))
                    exoPlayer.setMediaSources(scalarSources.map { it.mediaSource!! })
                    exoPlayer.prepare()
                    exoPlayer.playWhenReady = true

                    mediaSession.isActive = true
                    mediaSession.setPlaybackState(
                        stateBuilder.setState(
                            PlaybackStateCompat.STATE_PLAYING,
                            PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                            1f
                        ).build()
                    )
                    currentState = PlaybackStateCompat.STATE_PLAYING
                    if (isPlayProgram){
                        noChangedList.postValue(null)
                    }
                }
            }
        }
    }

    private fun removeMusic() {
        playScalar?.let { scalar ->
            getUriScalar(scalar)?.let { uri ->
                playListScalar.remove(scalar)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    scalarSources.removeIf {it.tag == scalar.name}
                } else {
                    val iterator: MutableIterator<ScalarMediaSource> = scalarSources.iterator()
                    while (iterator.hasNext()) {
                        val item: ScalarMediaSource = iterator.next()
                        if (item.tag === scalar.name) {
                            iterator.remove()
                        }
                    }
                }
                exoPlayer.setMediaSources(scalarSources.map { it.mediaSource!! })
                exoPlayer.prepare()

                if (scalarSources.isNotEmpty()) {
                    mediaSession.isActive = true
                    mediaSession.setPlaybackState(
                        stateBuilder.setState(
                            PlaybackStateCompat.STATE_PLAYING,
                            PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                            1f
                        ).build()
                    )
                    currentState = PlaybackStateCompat.STATE_PLAYING
                } else {
                    mediaSession.isActive = false
                    mediaSession.setPlaybackState(
                        stateBuilder.setState(
                            PlaybackStateCompat.STATE_STOPPED,
                            PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                            1f
                        ).build()
                    )
                    currentState = PlaybackStateCompat.STATE_STOPPED
                }
            }
        }
    }

    private fun stopMusic() {
        playScalar = null
        playListScalar.clear()
        scalarSources.clear()
        mediaSession.isActive = false
        mediaSession.setPlaybackState(
            stateBuilder.setState(
                PlaybackStateCompat.STATE_STOPPED,
                PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                1f
            ).build()
        )
        currentState = PlaybackStateCompat.STATE_STOPPED
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
            applicationContext, Util.getUserAgent(applicationContext, getString(R.string.app_name))
        )
        return ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
    }

    private fun getUriScalar(scalar: Scalar): Uri? {
        val file = File(
            getSaveDir(
                applicationContext, scalar.audio_file, scalar.audio_folder
            )
        )
        val preloaded = File(
            getPreloadedSaveDir(
                applicationContext, scalar.audio_file, scalar.audio_folder
            )
        )

        var uri: Uri? = null
        if (file.exists()) {
            uri = Uri.fromFile(file)
        }

        if (preloaded.exists()) {
            uri = Uri.fromFile(preloaded)
        }
        if (uri == null) {
            uri = Uri.parse(
                getTrackUrlScalar(scalar)
            )
        }
        return uri
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
        mediaSession.release()
    }
}