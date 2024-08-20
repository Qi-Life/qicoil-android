package com.Meditation.Sounds.frequencies.lemeor.tools.player

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import com.Meditation.Sounds.frequencies.R
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

class ScalarPlayerService : Service() {
    private val playerMap = mutableMapOf<Uri, ExoPlayer>()
    private val binder = MusicBinder()

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
//                "PLAY" -> playMusic(it.getStringArrayExtra("MP3_FILES") ?: emptyArray())
//                "STOP" -> stopMusic()
//                "ADD" -> addMusic(it.getStringExtra("MP3_FILE"))
//                "REMOVE" -> removeMusic(it.getStringExtra("MP3_FILE"))
            }
        }
        return START_STICKY
    }

    private fun playMusic(mp3Files: Array<Uri>) {
        mp3Files.forEach { uri ->
            if (!playerMap.containsKey(uri)) {
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

    private fun stopMusic() {
        playerMap.values.forEach { player ->
            player.stop()
            player.release()
        }
        playerMap.clear()
    }

    private fun addMusic(mp3File: Uri?) {
        mp3File?.let { uri ->
            if (!playerMap.containsKey(uri)) {
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

    private fun removeMusic(mp3File: Uri?) {
        mp3File?.let {
            playerMap[it]?.let { player ->
                player.stop()
                player.release()
                playerMap.remove(it)
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
            applicationContext,
            Util.getUserAgent(applicationContext, getString(R.string.app_name))
        )
        return ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopMusic()
    }

    inner class MusicBinder : Binder() {
        fun getService(): ScalarPlayerService = this@ScalarPlayerService
    }
}

//val playIntent = Intent(context, MusicService::class.java).apply {
//    action = "PLAY"
//    putExtra("MP3_FILES", arrayOf("file1.mp3", "file2.mp3"))
//}
//context.startService(playIntent)
//
//// Add an MP3 to the playlist
//val addIntent = Intent(context, MusicService::class.java).apply {
//    action = "ADD"
//    putExtra("MP3_FILE", "file3.mp3")
//}
//context.startService(addIntent)
//
//// Remove an MP3 from the playlist
//val removeIntent = Intent(context, MusicService::class.java).apply {
//    action = "REMOVE"
//    putExtra("MP3_FILE", "file1.mp3")
//}
//context.startService(removeIntent)
//
//// Stop all music
//val stopIntent = Intent(context, MusicService::class.java).apply {
//    action = "STOP"
//}
//context.startService(stopIntent)