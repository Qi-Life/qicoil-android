package com.Meditation.Sounds.frequencies.work


import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album
import com.Meditation.Sounds.frequencies.lemeor.data.model.Scalar
import com.Meditation.Sounds.frequencies.lemeor.data.model.Track
import com.Meditation.Sounds.frequencies.lemeor.getSaveDir
import com.Meditation.Sounds.frequencies.lemeor.getTempFile
import com.Meditation.Sounds.frequencies.lemeor.getTrackUrl
import com.Meditation.Sounds.frequencies.lemeor.getTrackUrlScalar
import com.Meditation.Sounds.frequencies.lemeor.scalarFolder
import com.Meditation.Sounds.frequencies.util.FileDownloader
import java.io.File

class ScalarDownLoadCourseAudioWorkManager(
    val context: Context,
    workerParams: WorkerParameters
) :
    CoroutineWorker(context, workerParams) {


    override suspend fun doWork(): Result {
        val url = inputData.getString(URL)
        val fileName = inputData.getString(FILE_NAME) ?: ""
        val trackId = inputData.getInt(TRACK_ID, 0)
        val tmpFile = File(getTempFile(context, fileName, scalarFolder))
        try {
            if (url != null) {
                val targetFile = File(getSaveDir(context, fileName, scalarFolder))
                var percentage = 0L
                if (!targetFile.exists()) {
                    FileDownloader.download(url, tmpFile) { downloaded, total ->
                        if ((downloaded * 100 / total) - percentage >= 1 || (downloaded * 100 / total) == 100L) {
                            percentage = downloaded * 100 / total
                            setProgressAsync(
                                workDataOf(
                                    TRACK_ID to trackId,
                                    DOWNLOADED to downloaded,
                                    TOTAL to total,
                                )
                            )
                        }

                    }

                    tmpFile.renameTo(targetFile)
                    val outputData = workDataOf(
                        TRACK_ID to trackId
                    )
                    return Result.success(outputData)
                } else {
                    return Result.success(
                        workDataOf(
                            TRACK_ID to trackId
                        )
                    )
                }

            } else {
                tmpFile.delete()
                return Result.failure(
                    workDataOf(
                        TRACK_ID to trackId
                    )
                )
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            return Result.failure(
                workDataOf(
                    ERROR to e,
                    TRACK_ID to trackId
                )
            )
        }

    }


    companion object {
        const val URL = "url"
        const val TRACK_ID = "track_id"
        const val FILE_NAME = "file_name"
        const val DOWNLOADED = "downloaded"
        const val TOTAL = "total"
        const val ERROR = "error"
        const val TAG = "DownLoadCourseAudioWorkManager"

        private fun getTag(trackId: Int) = "scalar_$trackId"

        fun start(context: Context, scalar: Scalar): OneTimeWorkRequest {
            val inputData = Data.Builder()
                .putInt(TRACK_ID, scalar.id.toInt())
                .putString(FILE_NAME, scalar.silent_url)
                .putString(URL, getTrackUrlScalar(scalar.silent_url))

            val oneTimeWorkRequest =
                OneTimeWorkRequestBuilder<DownLoadCourseAudioWorkManager>()
                    .setInputData(inputData.build())
                    .addTag(getTag(trackId = scalar.id.toInt()))
                    .addTag(TAG)
                    .build()

            WorkManager
                .getInstance(context)
                .enqueue(oneTimeWorkRequest)

            return oneTimeWorkRequest
        }
    }
}
