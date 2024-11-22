package com.Meditation.Sounds.frequencies.utils

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.isPlayProgram
import com.Meditation.Sounds.frequencies.lemeor.playProgramId
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper
import com.Meditation.Sounds.frequencies.lemeor.tools.player.ScalarPlayerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object PlayerUtils {
    fun checkSchedulePlaying(context: Context, onResult: (Boolean) -> Unit) {
        if (isPlayProgram
            && playProgramId == PreferenceHelper.getScheduleProgram(context)?.id
            && SharedPreferenceHelper.getInstance().getBool(Constants.PREF_SCHEDULE_PROGRAM_STATUS)
            && QcAlarmManager.isCurrentTimeInRange()) {
            val dialogBuilder = AlertDialog.Builder(context)
            dialogBuilder.setMessage(context.getString(R.string.txt_confirm_turn_off_before_switching_on_another))
                .setCancelable(false).setNegativeButton(context.getString(R.string.txt_no), null)
                .setPositiveButton(context.getString(R.string.txt_yes)) { _, _ ->
                    isPlayProgram = false
                    SharedPreferenceHelper.getInstance()
                        .setBool(Constants.PREF_SCHEDULE_PROGRAM_STATUS, false)
                    clearPlayerSilentQuantum(context)
                    onResult.invoke(true)
                }.show()
        } else {
            onResult.invoke(false)
        }
    }

    fun clearPlayerSilentQuantum(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val playIntent = Intent(context, ScalarPlayerService::class.java).apply {
                    action = "CLEAR"
                }
                context.startService(playIntent)
            } catch (_: Exception) {
            }
        }
    }

}