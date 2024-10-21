package com.Meditation.Sounds.frequencies.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import android.util.Log
import com.Meditation.Sounds.frequencies.api.models.GetFlashSaleOutput
import com.Meditation.Sounds.frequencies.services.AlarmReceiver
import com.Meditation.Sounds.frequencies.services.AlarmsScheduleProgramReceiver
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class QcAlarmManager {
    companion object {
        var countAlarm = 0

        @JvmStatic
        fun createAlarms(context: Context) {
            if (SharedPreferenceHelper.getInstance()
                    .getBool(Constants.KEY_PURCHASED) && SharedPreferenceHelper.getInstance()
                    .getBool(Constants.KEY_PURCHASED_ADVANCED)
            ) {
                clearAlarms(context)
                return
            }
            var jsonFlashSale = SharedPreferenceHelper.getInstance().get(Constants.PREF_FLASH_SALE)
            var flashsale = Gson().fromJson(jsonFlashSale, GetFlashSaleOutput::class.java)
            clearAlarms(context)
            if (flashsale.flashSale != null) {
                if (flashsale.flashSale.enable!!) {

                    var initDelay = flashsale.flashSale.initDelay
                    var duration = flashsale.flashSale.duration
                    var interval = flashsale.flashSale.interval
//                var interval = flashsale.flashSale.initDelay

                    var currentCal = Calendar.getInstance()
                    var fistIntallerAppTime = SharedPreferenceHelper.getInstance()
                        .getLong(Constants.ETRAX_FIRST_INSTALLER_APP_TIME)
                    var initFSTime = fistIntallerAppTime + (initDelay!! * 60 * 60 * 1000).toLong()
                    var calInitFSTime = Calendar.getInstance()
                    calInitFSTime.timeInMillis = initFSTime
                    createNewAlarms(
                        context,
                        currentCal,
                        calInitFSTime,
                        interval!!,
                        Constants.ETRAX_FLASH_SALE_INIT
                    )

                    if (flashsale.flashSale.ntf != null) {
                        if (flashsale.flashSale.ntf!!.first != null) {
                            var firstFlashSale =
                                initFSTime + (flashsale.flashSale.ntf!!.first!!.delay!! * 60 * 60 * 1000).toLong()
//                        var firstFlashSale = initFSTime + (60 * 1000).toLong()
                            var calFirstFS = Calendar.getInstance()
                            calFirstFS.timeInMillis = firstFlashSale
                            createNewAlarms(
                                context,
                                currentCal,
                                calFirstFS,
                                interval!!,
                                Constants.ETRAX_FLASH_SALE_FIRST_NOTIFICATION
                            )
                        }

                        if (flashsale.flashSale.ntf!!.second != null) {
                            var secondFlashSale =
                                initFSTime + (flashsale.flashSale.ntf!!.second!!.delay!! * 60 * 60 * 1000).toLong()
//                        var secondFlashSale = initFSTime + (2 * 60 * 1000).toLong()
                            var calSecondFS = Calendar.getInstance()
                            calSecondFS.timeInMillis = secondFlashSale
                            createNewAlarms(
                                context,
                                currentCal,
                                calSecondFS,
                                interval!!,
                                Constants.ETRAX_FLASH_SALE_SECOND_NOTIFICATION
                            )
                        }

                        if (flashsale.flashSale.ntf!!.third != null) {
                            var thirdFlashSale =
                                initFSTime + (flashsale.flashSale.ntf!!.third!!.delay!! * 60 * 60 * 1000).toLong()
//                        var thirdFlashSale = initFSTime + (3 * 60 * 1000).toLong()
                            var calThirdFS = Calendar.getInstance()
                            calThirdFS.timeInMillis = thirdFlashSale
                            createNewAlarms(
                                context,
                                currentCal,
                                calThirdFS,
                                interval!!,
                                Constants.ETRAX_FLASH_SALE_THIRD_NOTIFICATION
                            )
                        }
                    }
                } else {
                    QcAlarmManager.clearAlarms(context)
                }
            } else {
                QcAlarmManager.clearAlarms(context)
            }
        }

        @JvmStatic
        fun createNewAlarms(
            context: Context,
            currentCal: Calendar,
            alarmCal: Calendar,
            interval: Float,
            flashSaleType: Int
        ) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            while (alarmCal.before(currentCal)) {
                alarmCal.add(Calendar.SECOND, (interval!! * 24 * 60 * 60).toInt())
//            alarmCal.add(Calendar.SECOND, (5 * 60).toInt())
            }

            val intent = Intent(context, AlarmReceiver::class.java)
            intent.putExtra(Constants.ETRAX_FLASH_SALE_TYPE, flashSaleType)
            if (countAlarm > 20) {
                countAlarm = 0
            }
            countAlarm++

            var dateFormat = SimpleDateFormat("hh::mm:ss")
            Log.d("MENDATE", "" + flashSaleType + "-" + dateFormat.format(alarmCal.time))
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                countAlarm,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.set(AlarmManager.RTC_WAKEUP, alarmCal.timeInMillis, pendingIntent)
        }

        @JvmStatic
        fun clearAlarms(context: Context) {
            for (i in 0..21) {
                val intent = Intent(context, AlarmReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    i,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.cancel(pendingIntent)
            }
        }

        @JvmStatic
        fun clearReminderAlarm(context: Context) {

        }

        @SuppressLint("UnspecifiedImmutableFlag")
        @JvmStatic
        fun createReminderAlarm(context: Context) {
            //Remove
            val intent = Intent(context, AlarmReceiver::class.java)
            intent.putExtra(Constants.ETRAX_FLASH_SALE_TYPE, Constants.ETRAX_REMINDER_NOTIFICATION)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                Constants.REMINDER_NOTIFICATION_ID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
            //Re-create reminder
            val jsonFlashSale = SharedPreferenceHelper.getInstance().get(Constants.PREF_FLASH_SALE)
            if (jsonFlashSale != null) {
                var flashsale = Gson().fromJson(jsonFlashSale, GetFlashSaleOutput::class.java)
                if (flashsale?.reminder != null && flashsale.reminder.messages != null && flashsale.reminder.messages!!.size > 0) {
                    val hourFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val date = hourFormat.parse(flashsale.reminder.launchTime)

                    val calendar = Calendar.getInstance()
                    calendar.set(Calendar.HOUR_OF_DAY, date.hours)
                    calendar.set(Calendar.MINUTE, date.minutes)
                    calendar.set(Calendar.SECOND, 0)
                    val currentCalender = Calendar.getInstance()
                    if (calendar.timeInMillis < currentCalender.timeInMillis) {
                        calendar.add(Calendar.DATE, 1)
                        calendar.set(Calendar.HOUR_OF_DAY, date.hours)
                        calendar.set(Calendar.MINUTE, date.minutes)
                        calendar.set(Calendar.SECOND, 0)
                    }
//                    intent.putExtra(Constants.ETRAX_FLASH_SALE_TYPE, Constants.ETRAX_REMINDER_NOTIFICATION)

                    val dateFormat = SimpleDateFormat("dd:MM:yyyy HH:mm:ss")
                    Log.d("MENDATE", "Reminder-" + dateFormat.format(calendar.time))

                    alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        (flashsale.reminder.interval!! * 60 * 60 * 1000).toLong(),
                        pendingIntent
                    )
                }
            }
        }


        fun setScheduleProgramsAlarms(context: Context) {
            //clear schedule programs
            clearScheduleProgramsAlarms(context)
            if (SharedPreferenceHelper.getInstance()
                    .getBool(Constants.PREF_SCHEDULE_PROGRAM_STATUS)
            ) {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

//                val hourStartAm = SharedPreferenceHelper.getInstance()
//                    .getFloat(Constants.PREF_SCHEDULE_START_TIME_AM, 0f).toInt() / 60
//                val mintStartAm = SharedPreferenceHelper.getInstance()
//                    .getFloat(Constants.PREF_SCHEDULE_START_TIME_AM, 0f).toInt() % 60
//
//                val hourEndAm = SharedPreferenceHelper.getInstance()
//                    .getFloat(Constants.PREF_SCHEDULE_END_TIME_AM, 0f).toInt() / 60
//                val mintEndAm = SharedPreferenceHelper.getInstance()
//                    .getFloat(Constants.PREF_SCHEDULE_END_TIME_AM, 0f).toInt() % 60
//
//                val playIntentMorning =
//                    Intent(context, AlarmsScheduleProgramReceiver::class.java).apply {
//                        action = Constants.PREF_SCHEDULE_PROGRAM_PLAY
//                    }
//                val playPendingIntentMorning = PendingIntent.getBroadcast(
//                    context,
//                    1000,
//                    playIntentMorning,
//                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//                )
//                val calendarMorningStart = Calendar.getInstance().apply {
//                    set(Calendar.HOUR_OF_DAY, hourStartAm)
//                    set(Calendar.MINUTE, mintStartAm)
//                    set(Calendar.SECOND, 0)
//                }
//                alarmManager.setRepeating(
//                    AlarmManager.RTC_WAKEUP,
//                    calendarMorningStart.timeInMillis,
//                    AlarmManager.INTERVAL_DAY,
//                    playPendingIntentMorning
//                )
//
//                val stopIntentMorning =
//                    Intent(context, AlarmsScheduleProgramReceiver::class.java).apply {
//                        action = Constants.PREF_SCHEDULE_PROGRAM_STOP
//                    }
//                val stopPendingIntentMorning = PendingIntent.getBroadcast(
//                    context,
//                    1001,
//                    stopIntentMorning,
//                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//                )
//                val calendarMorningEnd = Calendar.getInstance().apply {
//                    set(Calendar.HOUR_OF_DAY, hourEndAm)
//                    set(Calendar.MINUTE, mintEndAm)
//                    set(Calendar.SECOND, 0)
//                }
//                alarmManager.setRepeating(
//                    AlarmManager.RTC_WAKEUP,
//                    calendarMorningEnd.timeInMillis,
//                    AlarmManager.INTERVAL_DAY,
//                    stopPendingIntentMorning
//                )

                val hourStartPm = SharedPreferenceHelper.getInstance()
                    .getFloat(Constants.PREF_SCHEDULE_START_TIME_PM, 0f).toInt() / 60
                val mintStartPm = SharedPreferenceHelper.getInstance()
                    .getFloat(Constants.PREF_SCHEDULE_START_TIME_PM, 0f).toInt() % 60

//                val hourEndPm = SharedPreferenceHelper.getInstance()
//                    .getFloat(Constants.PREF_SCHEDULE_END_TIME_PM, 0f).toInt() / 60
//                val mintEndPm = SharedPreferenceHelper.getInstance()
//                    .getFloat(Constants.PREF_SCHEDULE_END_TIME_PM, 0f).toInt() % 60

                val playIntentAfternoon =
                    Intent(context, AlarmsScheduleProgramReceiver::class.java).apply {
                        action = Constants.PREF_SCHEDULE_PROGRAM_PLAY
                    }
                val playPendingIntentAfternoon = PendingIntent.getBroadcast(
                    context,
                    1002,
                    playIntentAfternoon,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val calendarAfternoonStart = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, if (is24HourFormat(context)) (hourStartPm + 12) else hourStartPm)
                    set(Calendar.MINUTE, mintStartPm)
                    set(Calendar.SECOND, 0)
                }
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendarAfternoonStart.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    playPendingIntentAfternoon
                )

//                val stopIntentAfternoon =
//                    Intent(context, AlarmsScheduleProgramReceiver::class.java).apply {
//                        action = Constants.PREF_SCHEDULE_PROGRAM_STOP
//                    }
//                val stopPendingIntentAfternoon = PendingIntent.getBroadcast(
//                    context,
//                    1003,
//                    stopIntentAfternoon,
//                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//                )
//                val calendarAfternoonEnd = Calendar.getInstance().apply {
//                    set(Calendar.HOUR_OF_DAY, if (is24HourFormat(context)) (hourEndPm + 12) else hourEndPm)
//                    set(Calendar.MINUTE, mintEndPm)
//                    set(Calendar.SECOND, 0)
//                }
//                alarmManager.setRepeating(
//                    AlarmManager.RTC_WAKEUP,
//                    calendarAfternoonEnd.timeInMillis,
//                    AlarmManager.INTERVAL_DAY,
//                    stopPendingIntentAfternoon
//                )
            }
        }

        fun clearScheduleProgramsAlarms(context: Context) {
            for (i in 1000..1003) {
                val intent = Intent(context, AlarmReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    i,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.cancel(pendingIntent)
            }
        }

        fun isAlarmSet(context: Context, requestCode: Int, intent: Intent): Boolean {
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            return pendingIntent != null
        }

        private fun is24HourFormat(context: Context): Boolean {
            return DateFormat.is24HourFormat(context)
        }
    }

}