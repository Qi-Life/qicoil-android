package com.Meditation.Sounds.frequencies.lemeor.ui.scalar

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.Meditation.Sounds.frequencies.lemeor.data.model.Scalar
import com.Meditation.Sounds.frequencies.lemeor.getSaveDir
import com.Meditation.Sounds.frequencies.lemeor.scalarFolder
import com.Meditation.Sounds.frequencies.utils.Utils
import com.Meditation.Sounds.frequencies.work.ScalarDownLoadCourseAudioWorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


class ScalarDownloadService : LifecycleService() {

    companion object {
        private const val EXTRA_SCALAR = "extra_scalar"

        fun startService(context: Context, scalar: Scalar) {
            val startIntent = Intent(context, ScalarDownloadService::class.java)
            startIntent.putExtra(EXTRA_SCALAR, scalar)
            ContextCompat.startForegroundService(context, startIntent)
        }

        fun stopService(context: Context) {
            val stopIntent = Intent(context, ScalarDownloadService::class.java)
            context.stopService(stopIntent)
        }
    }

    var scalars = mutableListOf<Scalar>()
        private set

    var downloadErrorScalars = HashSet<String>()
        private set

    private val binder = DownloadServiceBinder()

    private var hasNetwork = true

    private val networkChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            val isNetworkAvailable = Utils.isConnectedToNetwork(context)
            if (hasNetwork != isNetworkAvailable) {
                hasNetwork = isNetworkAvailable
                if (hasNetwork) {
                    downloadErrorScalars.clear()
                }
            }
        }

    }

    override fun onCreate() {
        super.onCreate()
        WorkManager.getInstance(application)
            .cancelAllWorkByTag(ScalarDownLoadCourseAudioWorkManager.TAG)
        hasNetwork = Utils.isConnectedToNetwork(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                networkChangeReceiver,
                IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION),
                Context.RECEIVER_EXPORTED
            )
        } else {
            registerReceiver(
                networkChangeReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            )
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val scalar: Scalar = intent?.getParcelableExtra(EXTRA_SCALAR)!!
        this.scalars.add(scalar)
        val needToDownloadTrack =
            this.scalars.filter { downloadErrorScalars.contains(it.id) }.toMutableList()
        this.scalars = this.scalars.filter { !downloadErrorScalars.contains(it.id) }.toMutableList()
        scalars.forEach {
            if (!this.scalars.any { track -> track.id == it.id } && !downloadErrorScalars.contains(it.id)) {
                needToDownloadTrack.add(it)
            }
        }
        downloadErrorScalars.clear()
        if (getCompletedFileCount() == this.scalars.size) {
            this.scalars.clear()
        }
        this.scalars.addAll(needToDownloadTrack)

        CoroutineScope(Dispatchers.IO).launch {
            CoroutineScope(Dispatchers.Main).launch {
                if (this@ScalarDownloadService.scalars.isEmpty()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        stopForeground(STOP_FOREGROUND_REMOVE)
                    } else {
                        stopForeground(true)
                    }
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun downloadNext(redownload: Boolean = false) {
        WorkManager.getInstance(application)
            .cancelAllWorkByTag(ScalarDownLoadCourseAudioWorkManager.TAG)
        scalars.firstOrNull {
            (redownload || !downloadErrorScalars.contains(it.id))
        }?.let {
            downloadErrorScalars.remove(it.id)
            val request = ScalarDownLoadCourseAudioWorkManager.start(applicationContext, it)
            observeWorker(request)
        } ?: run {

        }

    }

    private fun observeWorker(request: OneTimeWorkRequest) {
        WorkManager.getInstance(applicationContext).getWorkInfoByIdLiveData(request.id)
            .observe(this) { workInfo ->
                CoroutineScope(Dispatchers.Main).launch {
                    val wasSuccess = workInfo.state == WorkInfo.State.SUCCEEDED
                    if (wasSuccess) {
                        val trackId = workInfo.outputData.getInt(
                            ScalarDownLoadCourseAudioWorkManager.TRACK_ID, 0
                        )
                        downloadErrorScalars.remove(trackId.toString())
                        downloadNext(false)
                    } else if (workInfo.state == WorkInfo.State.FAILED) {
                        workInfo.tags.firstOrNull { it.startsWith("track") }?.let { tag ->
                            val trackId = tag.replace("track_", "").toInt()
                            scalars.firstOrNull { trackId.toString() == it.id }?.let {
                                downloadErrorScalars.add(it.id)
                            }
                        }
                        downloadNext()
                    }
                }
            }
    }

    override fun onDestroy() {
        downloadErrorScalars.clear()
        WorkManager.getInstance(application)
            .cancelAllWorkByTag(ScalarDownLoadCourseAudioWorkManager.TAG)
        unregisterReceiver(networkChangeReceiver)
        super.onDestroy()
    }

    private fun getCompletedFileCount(): Int {
        return scalars.filter {
            File(getSaveDir(applicationContext, it.silent_url, scalarFolder)).exists()
        }.size
    }

    inner class DownloadServiceBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods.
        fun getService(): ScalarDownloadService = this@ScalarDownloadService
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }
}
