package com.Meditation.Sounds.frequencies.lemeor.ui.programs

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.feature.base.BaseFragment
import com.Meditation.Sounds.frequencies.lemeor.FAVORITES
import com.Meditation.Sounds.frequencies.lemeor.albumIdBackProgram
import com.Meditation.Sounds.frequencies.lemeor.categoryIdBackProgram
import com.Meditation.Sounds.frequencies.lemeor.data.api.RetrofitBuilder
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album
import com.Meditation.Sounds.frequencies.lemeor.data.model.Program
import com.Meditation.Sounds.frequencies.lemeor.data.model.Scalar
import com.Meditation.Sounds.frequencies.lemeor.data.model.Track
import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper
import com.Meditation.Sounds.frequencies.lemeor.data.utils.ViewModelFactory
import com.Meditation.Sounds.frequencies.lemeor.getPreloadedSaveDir
import com.Meditation.Sounds.frequencies.lemeor.getSaveDir
import com.Meditation.Sounds.frequencies.lemeor.isPlayProgram
import com.Meditation.Sounds.frequencies.lemeor.isTrackAdd
import com.Meditation.Sounds.frequencies.lemeor.playListScalar
import com.Meditation.Sounds.frequencies.lemeor.playProgramId
import com.Meditation.Sounds.frequencies.lemeor.playScalar
import com.Meditation.Sounds.frequencies.lemeor.rifeBackProgram
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper
import com.Meditation.Sounds.frequencies.lemeor.tools.player.ScalarPlayerService
import com.Meditation.Sounds.frequencies.lemeor.trackIdForProgram
import com.Meditation.Sounds.frequencies.lemeor.typeBack
import com.Meditation.Sounds.frequencies.lemeor.ui.albums.detail.NewAlbumDetailFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.main.HomeViewModel
import com.Meditation.Sounds.frequencies.lemeor.ui.main.NavigationActivity
import com.Meditation.Sounds.frequencies.lemeor.ui.main.UpdateTrack
import com.Meditation.Sounds.frequencies.lemeor.ui.programs.detail.ProgramDetailFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.purchase.new_flow.NewPurchaseActivity
import com.Meditation.Sounds.frequencies.lemeor.ui.scalar.ScalarDownloadService
import com.Meditation.Sounds.frequencies.models.ProgramSchedule
import com.Meditation.Sounds.frequencies.models.event.ScheduleProgramProgressEvent
import com.Meditation.Sounds.frequencies.models.event.ScheduleProgramStatusEvent
import com.Meditation.Sounds.frequencies.utils.Constants
import com.Meditation.Sounds.frequencies.utils.PlayerUtils
import com.Meditation.Sounds.frequencies.utils.QcAlarmManager
import com.Meditation.Sounds.frequencies.utils.SharedPreferenceHelper
import com.Meditation.Sounds.frequencies.utils.Utils
import com.Meditation.Sounds.frequencies.utils.isNotString
import com.Meditation.Sounds.frequencies.utils.loadImageWithGif
import com.Meditation.Sounds.frequencies.views.AlertMessageDialog
import com.jaygoo.widget.OnRangeChangedListener
import com.jaygoo.widget.RangeSeekBar
import com.tonyodev.fetch2core.isNetworkAvailable
import kotlinx.android.synthetic.main.fragment_new_program.btnDeleteProgramName
import kotlinx.android.synthetic.main.fragment_new_program.btnSwitchSchedule
import kotlinx.android.synthetic.main.fragment_new_program.imvSaveSchedule
import kotlinx.android.synthetic.main.fragment_new_program.program_back
import kotlinx.android.synthetic.main.fragment_new_program.program_create_new
import kotlinx.android.synthetic.main.fragment_new_program.program_title
import kotlinx.android.synthetic.main.fragment_new_program.programs_recycler_view
import kotlinx.android.synthetic.main.fragment_new_program.sbRangeScheduleTimeAm
import kotlinx.android.synthetic.main.fragment_new_program.sbRangeScheduleTimePm
import kotlinx.android.synthetic.main.fragment_new_program.tvProgramName
import kotlinx.android.synthetic.main.fragment_new_program.tvSaveSchedule
import kotlinx.android.synthetic.main.fragment_new_program.tvScheduleTimeAm
import kotlinx.android.synthetic.main.fragment_new_program.tvScheduleTimePm
import kotlinx.android.synthetic.main.fragment_new_program.tvSwitchSchedule
import kotlinx.android.synthetic.main.fragment_program_page.ivImage
import kotlinx.android.synthetic.main.fragment_program_page.loadingFrame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.util.Date

class NewProgramFragment : BaseFragment() {
    private lateinit var mViewModel: NewProgramViewModel
    private lateinit var mHomeViewModel: HomeViewModel
    private var mProgramAdapter: ProgramAdapter = ProgramAdapter()
    private var startTimeAm = 0f
    private var stopTimeAm = 0f
    private var startTimePm = 0f
    private var stopTimePm = 0f

    @Suppress("DEPRECATION")
    private var handlerProgress = Handler()
    private var runnableProgress = Runnable {
        EventBus.getDefault().post(ScheduleProgramProgressEvent)
        setDataTime()
        updateViewSaveSchedule()
        if (!QcAlarmManager.isCurrentTimeInRange() && isPlayProgram && playProgramId == PreferenceHelper.getScheduleProgram(requireContext())?.id) {
            EventBus.getDefault().post(ScheduleProgramStatusEvent(isPlay = false, isHidePlayer = true))
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onScheduleProgramProgressEvent(event: ScheduleProgramProgressEvent?) {
        updateViewProgram()
    }

    override fun initLayout() = R.layout.fragment_new_program

    override fun initComponents() {
        init()
    }

    override fun addListener() {
        mViewModel.getPrograms(viewLifecycleOwner, onChange = {
            mProgramAdapter.setData(it)
        }, showLoading = {
            loadingFrame.isVisible = it
        })

        program_back.setOnClickListener { onBackPressed() }

        program_create_new.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(requireContext()).create()
            val inflater = this.layoutInflater
            val dialogView: View = inflater.inflate(R.layout.dialog_add_edit_playlist, null)
            dialogBuilder.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val programName = dialogView.findViewById<View>(R.id.edtPlayListName) as EditText
            val btnAdd: Button = dialogView.findViewById<View>(R.id.btnSubmit) as Button

            btnAdd.setOnClickListener {
                val name = programName.text.trim()
                if (name.isNotEmpty()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        //call api createProgram
                        try {
                            val result = withContext(Dispatchers.Default) {
                                mViewModel.createProgram(name.toString())
                            }
                            val program = result.data
                            mViewModel.insert(program)
                        } catch (_: Exception) {
                            mViewModel.insert(
                                Program(
                                    0,
                                    name.toString(),
                                    "",
                                    0,
                                    Date().time,
                                    ArrayList(),
                                    isMy = true,
                                    false,
                                    is_dirty = false
                                )
                            )
                        }
                    }
                    dialogBuilder.dismiss()
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.tv_error_playlist_name),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            dialogBuilder.setView(dialogView)
            dialogBuilder.show()
        }

        mProgramAdapter.setOnClickListener(object : ProgramAdapter.Listener {
            override fun onClickItem(program: Program, i: Int) {
                if (program.isUnlocked) {
                    if (isTrackAdd && trackIdForProgram != (Constants.defaultHz - 1).toString()) {
                        val db = DataBase.getInstance(requireContext())
                        val programDao = db.programDao()
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val programRoom = programDao.getProgramById(program.id)
                                programRoom?.let { p ->
                                    p.records.add(trackIdForProgram)
                                    programDao.updateProgram(p.copy(updated_at = Date().time))
                                    if (p.user_id.isNotEmpty()) {
                                        try {
                                            mViewModel.updateTrackToProgram(
                                                UpdateTrack(
                                                    track_id = listOf(trackIdForProgram),
                                                    id = p.id,
                                                    track_type = if (trackIdForProgram.isNotString()) "mp3" else "rife",
                                                    request_type = "add",
                                                    is_favorite = (p.name.uppercase() == FAVORITES.uppercase() && p.favorited)
                                                )
                                            )
                                        } catch (_: Exception) {
                                        }
                                    }
                                }
                            } catch (_: Exception) {
                            }
                        }
                    }

                    parentFragmentManager.beginTransaction().setCustomAnimations(
                        R.anim.trans_right_to_left_in,
                        R.anim.trans_right_to_left_out,
                        R.anim.trans_left_to_right_in,
                        R.anim.trans_left_to_right_out
                    ).replace(
                        R.id.nav_host_fragment,
                        ProgramDetailFragment.newInstance(program.id),
                        ProgramDetailFragment().javaClass.simpleName
                    ).commit()
                } else {
                    var album: Album? = null
                    val tracks: ArrayList<Track> = ArrayList()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            program.records.forEach { r ->
                                if (r.isNotString()) {
                                    mViewModel.getTrackById(r.toDouble().toInt())
                                        ?.let { track -> tracks.add(track) }
                                }
                            }
                            tracks.forEach { t ->
                                val temp_album = mViewModel.getAlbumById(t.albumId, t.category_id)
                                if (temp_album?.isUnlocked == false && album == null) {
                                    album = temp_album
                                    CoroutineScope(Dispatchers.Main).launch {
                                        startActivity(
                                            NewPurchaseActivity.newIntent(
                                                requireContext(),
                                                temp_album.category_id,
                                                temp_album.tier_id,
                                                temp_album.id
                                            )
                                        )
                                    }
                                }
                            }
                        } catch (_: Exception) {
                        }
                    }
                }
            }

            override fun onDeleteItem(program: Program, i: Int) {
                val alertDialog = AlertMessageDialog(requireContext(),
                    object : AlertMessageDialog.IOnSubmitListener {
                        override fun submit() {
                            CoroutineScope(Dispatchers.IO).launch {
                                //call api delete program
                                try {
                                    mViewModel.deleteProgram(program.id.toString())
                                    mViewModel.delete(program)
                                } catch (_: Exception) {
                                    mViewModel.udpate(
                                        program.copy(
                                            deleted = true, updated_at = Date().time
                                        )
                                    )
                                }
                            }

                            if (program.id == PreferenceHelper.getScheduleProgram(requireContext())?.id) {
                                if (isPlayProgram && playProgramId == PreferenceHelper.getScheduleProgram(requireContext())?.id) {
                                    EventBus.getDefault().post("clear player")
                                    //clear scalar
                                    if (playListScalar.isNotEmpty()) {
                                        PlayerUtils.clearPlayerSilentQuantum(requireContext())
                                    }
                                }
                                PreferenceHelper.saveScheduleProgram(requireContext(), null)
                                updateViewProgram()
                            }

                            Toast.makeText(
                                requireContext(),
                                requireContext().getString(R.string.txt_delete_playlist_name_success),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        override fun cancel() {}
                    })
                alertDialog.show()
                alertDialog.setWarningMessage(getString(R.string.txt_warning_delete_playlist))
            }
        })

        btnSwitchSchedule.setOnClickListener {
            btnSwitchSchedule.isSelected = !btnSwitchSchedule.isSelected
            SharedPreferenceHelper.getInstance().setBool(Constants.PREF_SCHEDULE_PROGRAM_STATUS, btnSwitchSchedule.isSelected)
            if (btnSwitchSchedule.isSelected) {
                QcAlarmManager.setScheduleProgramsAlarms(requireContext())
            } else {
                EventBus.getDefault().post(ScheduleProgramStatusEvent(isPlay = false, isHidePlayer = true))
                QcAlarmManager.clearScheduleProgramsAlarms(requireContext())
            }
            updateViewProgram()
        }

        tvSwitchSchedule.setOnClickListener {
            btnSwitchSchedule.performClick()
        }

        tvProgramName.setOnClickListener {
            if (PreferenceHelper.getScheduleProgram(requireContext()) != null) {
                parentFragmentManager.beginTransaction().setCustomAnimations(
                    R.anim.trans_right_to_left_in,
                    R.anim.trans_right_to_left_out,
                    R.anim.trans_left_to_right_in,
                    R.anim.trans_left_to_right_out
                ).replace(
                    R.id.nav_host_fragment,
                    ProgramDetailFragment.newInstance(PreferenceHelper.getScheduleProgram(requireContext())?.id ?: 0),
                    ProgramDetailFragment().javaClass.simpleName
                ).commit()
            }
        }
    }

    private fun onBackPressed() {
        parentFragmentManager.beginTransaction().setCustomAnimations(
            R.anim.trans_left_to_right_in,
            R.anim.trans_left_to_right_out,
            R.anim.trans_right_to_left_in,
            R.anim.trans_right_to_left_out
        ).replace(
            R.id.nav_host_fragment, NewAlbumDetailFragment.newInstance(
                albumIdBackProgram!!, categoryIdBackProgram!!, typeBack, rifeBackProgram?.id ?: -1
            ), NewAlbumDetailFragment().javaClass.simpleName
        ).commit()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.isFocusableInTouchMode = true
        view.requestFocus()
        updateViewProgram()
    }

    fun init() {
        loadingFrame.visibility = View.VISIBLE
        loadImageWithGif(ivImage, R.raw.loading_grey)
        if (isTrackAdd) {
            program_title.text = getString(R.string.txt_my_playlists)
            program_back.visibility = View.VISIBLE
        } else {
            program_title.text = getString(R.string.navigation_lbl_programs)
            program_back.visibility = View.INVISIBLE
        }

        mViewModel = ViewModelProvider(
            this, ViewModelFactory(
                ApiHelper(RetrofitBuilder(requireContext()).apiService),
                DataBase.getInstance(requireContext())
            )
        )[NewProgramViewModel::class.java]

        mHomeViewModel = ViewModelProvider(
            this, ViewModelFactory(
                ApiHelper(RetrofitBuilder(requireContext()).apiService),
                DataBase.getInstance(requireContext())
            )
        )[HomeViewModel::class.java]

        programs_recycler_view.adapter = mProgramAdapter

        updateViewSaveSchedule()
        setDataTime()

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
                tvScheduleTimeAm.text = String.format(getString(R.string.time_schedule_am), fromTime, toTime)
                SharedPreferenceHelper.getInstance()
                    .setFloat(Constants.PREF_SCHEDULE_START_TIME_AM, leftValue)
                SharedPreferenceHelper.getInstance()
                    .setFloat(Constants.PREF_SCHEDULE_END_TIME_AM, rightValue)
                if (startTimeAm != leftValue || stopTimeAm != rightValue) {
                    handlerProgress.removeCallbacks(runnableProgress)
                    handlerProgress.postDelayed(runnableProgress, 1000)
                }
                updateViewSaveSchedule()
            }

            override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {

            }

            override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {

            }

        })
        sbRangeScheduleTimeAm.setProgress(
            SharedPreferenceHelper.getInstance()
                .getFloat(Constants.PREF_SCHEDULE_START_TIME_AM, 0f),
            SharedPreferenceHelper.getInstance().getFloat(Constants.PREF_SCHEDULE_END_TIME_AM, 180f)
        )

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
                tvScheduleTimePm.text = String.format(getString(R.string.time_schedule_pm), fromTime, toTime)

                SharedPreferenceHelper.getInstance()
                    .setFloat(Constants.PREF_SCHEDULE_START_TIME_PM, leftValue)
                SharedPreferenceHelper.getInstance()
                    .setFloat(Constants.PREF_SCHEDULE_END_TIME_PM, rightValue)
                if (startTimePm != leftValue || stopTimePm != rightValue) {
                    handlerProgress.removeCallbacks(runnableProgress)
                    handlerProgress.postDelayed(runnableProgress, 1000)
                }
                updateViewSaveSchedule()
            }

            override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {

            }

            override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {

            }
        })
        sbRangeScheduleTimePm.setProgress(
            SharedPreferenceHelper.getInstance()
                .getFloat(Constants.PREF_SCHEDULE_START_TIME_PM, 540f),
            SharedPreferenceHelper.getInstance().getFloat(Constants.PREF_SCHEDULE_END_TIME_PM, 719f)
        )
        btnDeleteProgramName.setOnClickListener {
            PreferenceHelper.saveScheduleProgram(requireContext(), null)
            QcAlarmManager.clearScheduleProgramsAlarms(requireContext())
            updateViewProgram()
        }

        imvSaveSchedule.setOnClickListener {
            AlertDialog.Builder(mContext).setTitle(R.string.app_name)
                .setMessage(R.string.txt_confirm_save_schedule)
                .setPositiveButton(R.string.txt_yes) { _, _ ->
                    syncScheduleTime()
                }.setNegativeButton(R.string.txt_no) { _, _ ->

                }.show()
        }

        tvSaveSchedule.setOnClickListener {
            imvSaveSchedule.performClick()
        }
    }

    private fun setDataTime() {
        startTimeAm =
            SharedPreferenceHelper.getInstance().getFloat(Constants.PREF_SCHEDULE_START_TIME_AM, 0f)

        stopTimeAm =
            SharedPreferenceHelper.getInstance().getFloat(Constants.PREF_SCHEDULE_END_TIME_AM, 180f)

        startTimePm = SharedPreferenceHelper.getInstance()
            .getFloat(Constants.PREF_SCHEDULE_START_TIME_PM, 540f)

        stopTimePm =
            SharedPreferenceHelper.getInstance().getFloat(Constants.PREF_SCHEDULE_END_TIME_PM, 719f)
    }

    private fun syncScheduleTime() {
        if (requireActivity().isNetworkAvailable()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val input = ProgramSchedule(
                        startTimeAm = Math.round(
                            SharedPreferenceHelper.getInstance()
                                .getFloat(Constants.PREF_SCHEDULE_START_TIME_AM, 0f) * 10
                        ) / 10.0f, stopTimeAm = Math.round(
                            SharedPreferenceHelper.getInstance()
                                .getFloat(Constants.PREF_SCHEDULE_END_TIME_AM, 180f) * 10
                        ) / 10.0f, startTimePm = Math.round(
                            SharedPreferenceHelper.getInstance()
                                .getFloat(Constants.PREF_SCHEDULE_START_TIME_PM, 540f) * 10
                        ) / 10.0f, stopTimePm = Math.round(
                            SharedPreferenceHelper.getInstance()
                                .getFloat(Constants.PREF_SCHEDULE_END_TIME_PM, 719f) * 10
                        ) / 10.0f
                    )
                    mHomeViewModel.updateProgramScheduleTime(input)

                    //server
                    SharedPreferenceHelper.getInstance().setFloat(
                        Constants.PREF_SCHEDULE_START_TIME_AM_API,
                        SharedPreferenceHelper.getInstance()
                            .getFloat(Constants.PREF_SCHEDULE_START_TIME_AM, 0f)
                    )
                    SharedPreferenceHelper.getInstance().setFloat(
                        Constants.PREF_SCHEDULE_END_TIME_AM_API,
                        SharedPreferenceHelper.getInstance()
                            .getFloat(Constants.PREF_SCHEDULE_END_TIME_AM, 180f)
                    )

                    SharedPreferenceHelper.getInstance().setFloat(
                        Constants.PREF_SCHEDULE_START_TIME_PM_API,
                        SharedPreferenceHelper.getInstance()
                            .getFloat(Constants.PREF_SCHEDULE_START_TIME_PM, 540f)
                    )
                    SharedPreferenceHelper.getInstance().setFloat(
                        Constants.PREF_SCHEDULE_END_TIME_PM_API,
                        SharedPreferenceHelper.getInstance()
                            .getFloat(Constants.PREF_SCHEDULE_END_TIME_PM, 719f)
                    )
                    updateViewSaveSchedule()
                } catch (_: Exception) {

                }
            }
        } else {
            Toast.makeText(
                requireContext(), getString(R.string.err_network_available), Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateViewProgram() {
        if (PreferenceHelper.getScheduleProgram(requireContext()) != null) {
            tvProgramName.text = PreferenceHelper.getScheduleProgram(requireContext())?.name
            btnDeleteProgramName.visibility = View.VISIBLE
        } else {
            tvProgramName.text = getString(R.string.you_have_no_program_selected)
            btnDeleteProgramName.visibility = View.GONE
        }
        btnSwitchSchedule.isSelected =
            SharedPreferenceHelper.getInstance().getBool(Constants.PREF_SCHEDULE_PROGRAM_STATUS)

        if (btnSwitchSchedule.isSelected) {
            sbRangeScheduleTimeAm.isEnabled = true
            sbRangeScheduleTimePm.isEnabled = true
            sbRangeScheduleTimeAm.progressDrawableId = R.drawable.progress
            sbRangeScheduleTimePm.progressDrawableId = R.drawable.progress
        } else {
            sbRangeScheduleTimeAm.isEnabled = false
            sbRangeScheduleTimePm.isEnabled = false
            sbRangeScheduleTimeAm.progressDrawableId = R.drawable.progress_disable
            sbRangeScheduleTimePm.progressDrawableId = R.drawable.progress_disable
        }
    }

    private fun updateViewSaveSchedule() {
        val isSaveEnable = SharedPreferenceHelper.getInstance().getFloat(
            Constants.PREF_SCHEDULE_START_TIME_AM, 0f
        ) != SharedPreferenceHelper.getInstance().getFloat(
            Constants.PREF_SCHEDULE_START_TIME_AM_API,
            0f
        ) || SharedPreferenceHelper.getInstance().getFloat(
            Constants.PREF_SCHEDULE_END_TIME_AM, 180f
        ) != SharedPreferenceHelper.getInstance().getFloat(
            Constants.PREF_SCHEDULE_END_TIME_AM_API,
            0f
        ) || SharedPreferenceHelper.getInstance().getFloat(
            Constants.PREF_SCHEDULE_START_TIME_PM, 540f
        ) != SharedPreferenceHelper.getInstance().getFloat(
            Constants.PREF_SCHEDULE_START_TIME_PM_API, 0f
        ) || SharedPreferenceHelper.getInstance().getFloat(
            Constants.PREF_SCHEDULE_END_TIME_PM, 719f
        ) != SharedPreferenceHelper.getInstance()
            .getFloat(Constants.PREF_SCHEDULE_END_TIME_PM_API, 0f)

        tvSaveSchedule?.isEnabled = isSaveEnable
        imvSaveSchedule?.isEnabled = isSaveEnable
    }
}