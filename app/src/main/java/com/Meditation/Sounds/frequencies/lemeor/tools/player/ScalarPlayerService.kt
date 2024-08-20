package com.Meditation.Sounds.frequencies.lemeor.tools.player

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.data.model.Scalar
import com.Meditation.Sounds.frequencies.lemeor.getPreloadedSaveDir
import com.Meditation.Sounds.frequencies.lemeor.getSaveDir
import com.Meditation.Sounds.frequencies.lemeor.getTrackUrlScalar
import com.Meditation.Sounds.frequencies.lemeor.playListScalar
import com.Meditation.Sounds.frequencies.lemeor.playScalar
import com.Meditation.Sounds.frequencies.lemeor.scalarFolder
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import java.io.File

class ScalarPlayerService : Service() {
    private val playerMap = mutableMapOf<Uri, ExoPlayer>()
    private val binder = MusicBinder()

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                "STOP" -> stopMusic()
                "PAUSE" -> pauseMusic()
                "REPLAY" -> resumeMusic()
                "ADD" -> addMusic()
                "REMOVE" -> removeMusic()
            }
        }
        return START_STICKY
    }

    private fun stopMusic() {
        playerMap.values.forEach { player ->
            player.stop()
            player.release()
        }
        playerMap.clear()
    }

    private fun addMusic() {
        playScalar?.let { scalar ->
            getUriScalar(scalar)?.let { uri ->
                if (!playerMap.containsKey(uri)) {
                    playListScalar.add(scalar)
                    val player =
                        SimpleExoPlayer.Builder(this, DefaultRenderersFactory(this)).build().apply {
                            volume = 0F
                            Thread.sleep(100)
                            setMediaSource(buildMediaSource(uri))
                            prepare()
                            Thread.sleep(100)
                            volume = 1F
                            playWhenReady = true
                        }
                    playerMap[uri] = player
                }
            }
        }
    }

    private fun removeMusic() {
        playScalar?.let { scalar ->
            getUriScalar(scalar)?.let { uri ->
                playListScalar.remove(scalar)
                playerMap[uri]?.let { player ->
                    player.stop()
                    player.release()
                    playerMap.remove(uri)
                }
            }
        }
    }

    private fun pauseMusic() {
        playerMap.values.forEach { player ->
            if (player.isPlaying) {
                player.playWhenReady = false
            }
        }
    }

    private fun resumeMusic() {
        playerMap.values.forEach { player ->
            if (!player.isPlaying) {
                player.playWhenReady = true
            }
        }
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
                applicationContext, scalar.silent_url, scalarFolder
            )
        )
        val preloaded = File(
            getPreloadedSaveDir(
                applicationContext, scalar.silent_url, scalarFolder
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
                getTrackUrlScalar(scalar.silent_url)
            )
        }
        return uri
    }

    override fun onDestroy() {
        super.onDestroy()
        playListScalar.clear()
        stopMusic()
    }

    inner class MusicBinder : Binder() {
        fun getService(): ScalarPlayerService = this@ScalarPlayerService
    }
}