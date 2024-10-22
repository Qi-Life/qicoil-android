package com.Meditation.Sounds.frequencies.services.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.Meditation.Sounds.frequencies.utils.QcAlarmManager

class DailyWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    override fun doWork(): Result {
        QcAlarmManager.setScheduleProgramsAlarms(applicationContext)
        return Result.success()
    }
}