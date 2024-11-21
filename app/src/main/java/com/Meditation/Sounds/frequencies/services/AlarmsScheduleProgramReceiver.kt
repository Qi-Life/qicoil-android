package com.Meditation.Sounds.frequencies.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.Meditation.Sounds.frequencies.models.event.ScheduleProgramStatusEvent
import com.Meditation.Sounds.frequencies.utils.Constants
import com.Meditation.Sounds.frequencies.utils.SharedPreferenceHelper
import org.greenrobot.eventbus.EventBus

class AlarmsScheduleProgramReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null && SharedPreferenceHelper.getInstance().getBool(Constants.PREF_SCHEDULE_PROGRAM_STATUS)) {
//            val data = intent.getStringExtra("data")
            when (intent.action) {
                Constants.PREF_SCHEDULE_PROGRAM_PLAY -> playMusic(context)
                Constants.PREF_SCHEDULE_PROGRAM_STOP -> stopMusic(context)
            }
        }
    }

    private fun playMusic(context: Context?) {
        EventBus.getDefault().post(ScheduleProgramStatusEvent(isPlay = true))
    }

    private fun stopMusic(context: Context?) {
        EventBus.getDefault().post(ScheduleProgramStatusEvent(isPlay = false))
    }
}