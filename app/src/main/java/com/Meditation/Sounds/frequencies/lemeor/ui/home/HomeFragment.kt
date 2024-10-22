package com.Meditation.Sounds.frequencies.lemeor.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.view.View
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.feature.base.BaseFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.main.NavigationActivity
import com.Meditation.Sounds.frequencies.models.event.ScheduleProgramProgressEvent
import com.Meditation.Sounds.frequencies.utils.Constants
import com.Meditation.Sounds.frequencies.utils.QcAlarmManager
import com.Meditation.Sounds.frequencies.utils.SharedPreferenceHelper
import com.jaygoo.widget.OnRangeChangedListener
import com.jaygoo.widget.RangeSeekBar
import kotlinx.android.synthetic.main.fragment_home.btnDeleteProgramName
import kotlinx.android.synthetic.main.fragment_home.btnSwitchSchedule
import kotlinx.android.synthetic.main.fragment_home.rcAlbumRecent
import kotlinx.android.synthetic.main.fragment_home.sbRangeScheduleTimeAm
import kotlinx.android.synthetic.main.fragment_home.sbRangeScheduleTimePm
import kotlinx.android.synthetic.main.fragment_home.tvProgramName
import kotlinx.android.synthetic.main.fragment_home.tvScheduleTimeAm
import kotlinx.android.synthetic.main.fragment_home.tvScheduleTimePm
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class HomeFragment : BaseFragment() {
    private var recentAlbumsAdapter: RecentAlbumsAdapter? = null
    @Suppress("DEPRECATION")
    private var handlerProgress = Handler()
    private var runnableProgress = Runnable {
        EventBus.getDefault().post(ScheduleProgramProgressEvent)
    }

    override fun initLayout(): Int = R.layout.fragment_home

    override fun initComponents() {
        recentAlbumsAdapter = RecentAlbumsAdapter(requireContext()) {
            (requireActivity() as NavigationActivity).onAlbumDetails(it)
        }
        rcAlbumRecent.adapter = recentAlbumsAdapter
        recentAlbumsAdapter?.setData(SharedPreferenceHelper.getInstance().recentAlbums)

        sbRangeScheduleTimeAm.setRange(0f, 719f)
        sbRangeScheduleTimeAm.setOnRangeChangedListener(object : OnRangeChangedListener {
            @SuppressLint("DefaultLocale", "SetTextI18n")
            override fun onRangeChanged(
                rangeSeekBar: RangeSeekBar, leftValue: Float, rightValue: Float, isFromUser: Boolean
            ) {
                val fromHours = leftValue.toInt() / 60
                val fromMinutes = leftValue.toInt() % 60
                val toHours = rightValue.toInt() / 60
                val toMinutes = rightValue.toInt() % 60
                val fromTime = String.format("%02d:%02d", fromHours, fromMinutes)
                val toTime = String.format("%02d:%02d", toHours, toMinutes)
                tvScheduleTimeAm.text = "$fromTime AM to $toTime AM"
                SharedPreferenceHelper.getInstance().setFloat(Constants.PREF_SCHEDULE_START_TIME_AM, leftValue)
                SharedPreferenceHelper.getInstance().setFloat(Constants.PREF_SCHEDULE_END_TIME_AM, rightValue)
                handlerProgress.removeCallbacks(runnableProgress)
                handlerProgress.postDelayed(runnableProgress, 3000)
            }

            override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {

            }

            override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {

            }

        })
        sbRangeScheduleTimeAm.setProgress(SharedPreferenceHelper.getInstance().getFloat(Constants.PREF_SCHEDULE_START_TIME_AM, 0f), SharedPreferenceHelper.getInstance().getFloat(Constants.PREF_SCHEDULE_END_TIME_AM, 180f))

        sbRangeScheduleTimePm.setRange(0f, 719f)
        sbRangeScheduleTimePm.setOnRangeChangedListener(object : OnRangeChangedListener {
            @SuppressLint("DefaultLocale", "SetTextI18n")
            override fun onRangeChanged(
                rangeSeekBar: RangeSeekBar, leftValue: Float, rightValue: Float, isFromUser: Boolean
            ) {
                val fromHours = leftValue.toInt() / 60
                val fromMinutes = leftValue.toInt() % 60
                val toHours = rightValue.toInt() / 60
                val toMinutes = rightValue.toInt() % 60
                val fromTime = String.format("%02d:%02d", fromHours, fromMinutes)
                val toTime = String.format("%02d:%02d", toHours, toMinutes)
                tvScheduleTimePm.text = "$fromTime PM to $toTime PM"

                SharedPreferenceHelper.getInstance().setFloat(Constants.PREF_SCHEDULE_START_TIME_PM, leftValue)
                SharedPreferenceHelper.getInstance().setFloat(Constants.PREF_SCHEDULE_END_TIME_PM, rightValue)
                handlerProgress.removeCallbacks(runnableProgress)
                handlerProgress.postDelayed(runnableProgress, 3000)
            }

            override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {

            }

            override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {

            }
        })
        sbRangeScheduleTimePm.setProgress(SharedPreferenceHelper.getInstance().getFloat(Constants.PREF_SCHEDULE_START_TIME_PM, 540f), SharedPreferenceHelper.getInstance().getFloat(Constants.PREF_SCHEDULE_END_TIME_PM, 719f))
        btnDeleteProgramName.setOnClickListener {
            SharedPreferenceHelper.getInstance().setInt(Constants.PREF_SCHEDULE_PROGRAM_ID, 0)
            SharedPreferenceHelper.getInstance().set(Constants.PREF_SCHEDULE_PROGRAM_NAME, "")
            QcAlarmManager.clearScheduleProgramsAlarms(requireContext())
            updateViewProgram()
        }
    }

    private fun updateViewProgram() {
        if (SharedPreferenceHelper.getInstance().getInt(Constants.PREF_SCHEDULE_PROGRAM_ID) != 0) {
            tvProgramName.text = SharedPreferenceHelper.getInstance().get(Constants.PREF_SCHEDULE_PROGRAM_NAME)
            btnDeleteProgramName.visibility = View.VISIBLE
        } else {
            tvProgramName.text = getString(R.string.you_have_no_program_selected)
            btnDeleteProgramName.visibility = View.GONE
        }
        btnSwitchSchedule.isSelected = SharedPreferenceHelper.getInstance().getBool(Constants.PREF_SCHEDULE_PROGRAM_STATUS)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.isFocusableInTouchMode = true
        view.requestFocus()
        updateViewProgram()
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onScheduleProgramProgressEvent(event: ScheduleProgramProgressEvent?) {
        updateViewProgram()
    }

    override fun addListener() {
        btnSwitchSchedule.setOnClickListener {
            btnSwitchSchedule.isSelected = !btnSwitchSchedule.isSelected
            if (btnSwitchSchedule.isSelected) {
                QcAlarmManager.setScheduleProgramsAlarms(requireContext())
            } else {
                QcAlarmManager.clearScheduleProgramsAlarms(requireContext())
            }
            SharedPreferenceHelper.getInstance().setBool(Constants.PREF_SCHEDULE_PROGRAM_STATUS, btnSwitchSchedule.isSelected)
        }
    }
}