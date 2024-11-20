package com.Meditation.Sounds.frequencies.lemeor.ui.main


import android.Manifest
import android.Manifest.permission.READ_MEDIA_AUDIO
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.Meditation.Sounds.frequencies.BuildConfig
import com.Meditation.Sounds.frequencies.QApplication
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.api.ApiListener
import com.Meditation.Sounds.frequencies.api.models.GetFlashSaleOutput
import com.Meditation.Sounds.frequencies.feature.chatbot.ChatBotViewModel
import com.Meditation.Sounds.frequencies.feature.chatbot.MessageChatBot
import com.Meditation.Sounds.frequencies.feature.chatbot.MessageChatBotAdapter
import com.Meditation.Sounds.frequencies.feature.discover.DiscoverFragment
import com.Meditation.Sounds.frequencies.lemeor.currentPosition
import com.Meditation.Sounds.frequencies.lemeor.currentTrack
import com.Meditation.Sounds.frequencies.lemeor.currentTrackIndex
import com.Meditation.Sounds.frequencies.lemeor.data.api.RetrofitBuilder
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album
import com.Meditation.Sounds.frequencies.lemeor.data.model.Program
import com.Meditation.Sounds.frequencies.lemeor.data.model.Rife
import com.Meditation.Sounds.frequencies.lemeor.data.model.Scalar
import com.Meditation.Sounds.frequencies.lemeor.data.model.Search
import com.Meditation.Sounds.frequencies.lemeor.data.model.Track
import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper
import com.Meditation.Sounds.frequencies.lemeor.data.utils.Resource
import com.Meditation.Sounds.frequencies.lemeor.data.utils.ViewModelFactory
import com.Meditation.Sounds.frequencies.lemeor.duration
import com.Meditation.Sounds.frequencies.lemeor.getPreloadedSaveDir
import com.Meditation.Sounds.frequencies.lemeor.getSaveDir
import com.Meditation.Sounds.frequencies.lemeor.hideKeyboard
import com.Meditation.Sounds.frequencies.lemeor.isPlayAlbum
import com.Meditation.Sounds.frequencies.lemeor.isPlayProgram
import com.Meditation.Sounds.frequencies.lemeor.isTrackAdd
import com.Meditation.Sounds.frequencies.lemeor.isUserPaused
import com.Meditation.Sounds.frequencies.lemeor.max
import com.Meditation.Sounds.frequencies.lemeor.playAlbumId
import com.Meditation.Sounds.frequencies.lemeor.playListScalar
import com.Meditation.Sounds.frequencies.lemeor.playProgramId
import com.Meditation.Sounds.frequencies.lemeor.playRife
import com.Meditation.Sounds.frequencies.lemeor.playScalar
import com.Meditation.Sounds.frequencies.lemeor.playingScalar
import com.Meditation.Sounds.frequencies.lemeor.programName
import com.Meditation.Sounds.frequencies.lemeor.selectedNaviFragment
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper.isFirstSync
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper.isHighQuantum
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper.isInnerCircle
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper.isLogged
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper.isShowDisclaimer
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper.preference
import com.Meditation.Sounds.frequencies.lemeor.tools.downloader.DownloadErrorEvent
import com.Meditation.Sounds.frequencies.lemeor.tools.downloader.DownloadInfo
import com.Meditation.Sounds.frequencies.lemeor.tools.downloader.DownloadService
import com.Meditation.Sounds.frequencies.lemeor.tools.downloader.DownloadTrackErrorEvent
import com.Meditation.Sounds.frequencies.lemeor.tools.downloader.DownloaderActivity
import com.Meditation.Sounds.frequencies.lemeor.tools.player.MusicRepository
import com.Meditation.Sounds.frequencies.lemeor.tools.player.PlayerSelected
import com.Meditation.Sounds.frequencies.lemeor.tools.player.PlayerService
import com.Meditation.Sounds.frequencies.lemeor.tools.player.PlayerUIFragment
import com.Meditation.Sounds.frequencies.lemeor.tools.player.ScalarPlayerService
import com.Meditation.Sounds.frequencies.lemeor.trackList
import com.Meditation.Sounds.frequencies.lemeor.ui.albums.detail.NewAlbumDetailFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.albums.search.SearchAdapter
import com.Meditation.Sounds.frequencies.lemeor.ui.albums.tabs.CategoriesPagerFragment.CategoriesPagerListener
import com.Meditation.Sounds.frequencies.lemeor.ui.albums.tabs.TiersPagerFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.albums.tabs.TiersPagerFragment.OnTiersFragmentListener
import com.Meditation.Sounds.frequencies.lemeor.ui.auth.AuthActivity
import com.Meditation.Sounds.frequencies.lemeor.ui.auth.updateTier
import com.Meditation.Sounds.frequencies.lemeor.ui.home.HomeFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.options.NewOptionsFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.programs.NewProgramFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.programs.NewProgramViewModel
import com.Meditation.Sounds.frequencies.lemeor.ui.programs.detail.ProgramDetailFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.programs.detail.ProgramDetailViewModel
import com.Meditation.Sounds.frequencies.lemeor.ui.purchase.new_flow.NewPurchaseActivity
import com.Meditation.Sounds.frequencies.lemeor.ui.purchase.new_flow.PurchaseItemAlbumWebView
import com.Meditation.Sounds.frequencies.lemeor.ui.rife.NewRifeFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.rife.NewRifeViewModel
import com.Meditation.Sounds.frequencies.lemeor.ui.scalar.NewScalarFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.scalar.NewScalarViewModel
import com.Meditation.Sounds.frequencies.lemeor.ui.scalar.ScalarDownloadService
import com.Meditation.Sounds.frequencies.lemeor.ui.videos.NewVideosFragment
import com.Meditation.Sounds.frequencies.models.event.ScheduleProgramProgressEvent
import com.Meditation.Sounds.frequencies.models.event.ScheduleProgramStatusEvent
import com.Meditation.Sounds.frequencies.models.event.SyncDataEvent
import com.Meditation.Sounds.frequencies.models.event.UpdateViewSilentQuantumEvent
import com.Meditation.Sounds.frequencies.services.worker.DailyWorker
import com.Meditation.Sounds.frequencies.tasks.BaseTask
import com.Meditation.Sounds.frequencies.tasks.GetFlashSaleTask
import com.Meditation.Sounds.frequencies.utils.Combined5LiveData
import com.Meditation.Sounds.frequencies.utils.Constants
import com.Meditation.Sounds.frequencies.utils.Constants.Companion.PREF_SCHEDULE_PROGRAM_NAME
import com.Meditation.Sounds.frequencies.utils.Constants.Companion.PREF_SETTING_ADVANCE_SCALAR_ON_OFF
import com.Meditation.Sounds.frequencies.utils.Constants.Companion.PREF_SETTING_CHATBOT_ON_OFF
import com.Meditation.Sounds.frequencies.utils.CopyAssets.copyAssetFolder
import com.Meditation.Sounds.frequencies.utils.FlowSearch
import com.Meditation.Sounds.frequencies.utils.QcAlarmManager
import com.Meditation.Sounds.frequencies.utils.SharedPreferenceHelper
import com.Meditation.Sounds.frequencies.utils.Utils
import com.Meditation.Sounds.frequencies.utils.extensions.showViewWithFadeIn
import com.Meditation.Sounds.frequencies.views.DisclaimerDialog
import com.google.gson.Gson
import com.tonyodev.fetch2core.isNetworkAvailable
import kotlinx.android.synthetic.main.activity_navigation.album_search
import kotlinx.android.synthetic.main.activity_navigation.album_search_clear
import kotlinx.android.synthetic.main.activity_navigation.bg_mode
import kotlinx.android.synthetic.main.activity_navigation.btnAddProgram
import kotlinx.android.synthetic.main.activity_navigation.btnHideChatBot
import kotlinx.android.synthetic.main.activity_navigation.btnStartChatBot
import kotlinx.android.synthetic.main.activity_navigation.flash_sale
import kotlinx.android.synthetic.main.activity_navigation.flash_sale_hours
import kotlinx.android.synthetic.main.activity_navigation.flash_sale_minutes
import kotlinx.android.synthetic.main.activity_navigation.flash_sale_seconds
import kotlinx.android.synthetic.main.activity_navigation.lblnoresult
import kotlinx.android.synthetic.main.activity_navigation.mTvDownloadPercent
import kotlinx.android.synthetic.main.activity_navigation.navigation_albums
import kotlinx.android.synthetic.main.activity_navigation.navigation_discover
import kotlinx.android.synthetic.main.activity_navigation.navigation_home
import kotlinx.android.synthetic.main.activity_navigation.navigation_options
import kotlinx.android.synthetic.main.activity_navigation.navigation_programs
import kotlinx.android.synthetic.main.activity_navigation.navigation_rife
import kotlinx.android.synthetic.main.activity_navigation.navigation_scalar
import kotlinx.android.synthetic.main.activity_navigation.navigation_videos
import kotlinx.android.synthetic.main.activity_navigation.search_categories_recycler
import kotlinx.android.synthetic.main.activity_navigation.search_divider
import kotlinx.android.synthetic.main.activity_navigation.search_layout
import kotlinx.android.synthetic.main.activity_navigation.view.txt_mode
import kotlinx.android.synthetic.main.activity_navigation.viewGroupDownload
import kotlinx.android.synthetic.main.activity_navigation.viewIntroChatBot
import kotlinx.android.synthetic.main.activity_navigation.view_data
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.HttpException
import java.io.File
import java.util.Calendar
import java.util.concurrent.TimeUnit


const val REQUEST_CODE_PERMISSION = 1111

class NavigationActivity : AppCompatActivity(), CategoriesPagerListener, OnTiersFragmentListener,
    ApiListener<Any> {
    private var mViewGroupCurrent: View? = null
    private lateinit var mViewModel: HomeViewModel
    private lateinit var mNewProgramViewModel: NewProgramViewModel
    private lateinit var mNewRifeViewModel: NewRifeViewModel
    private lateinit var mNewScalarViewModel: NewScalarViewModel
    private lateinit var mChatBotViewModel: ChatBotViewModel
    private lateinit var mProgramDetailViewModel: ProgramDetailViewModel

    private var playerUI: PlayerUIFragment? = null

    private var mLocalApkPath: String? = null
    private var refId: Long = 0

    //chat bot
    private var chatPopupWindow: PopupWindow? = null
    private var msgChatAdapter: MessageChatBotAdapter? = null
    private var chatMessages = arrayListOf<MessageChatBot>()
    private var rvChatBot: RecyclerView? = null
    private var edtMessageChat: AppCompatEditText? = null
    private var btnSendChat: AppCompatImageView? = null
    private var isStartedChat = false

    //alarm play programs
    private var isStartScheduleProgram = true

    //search
    private val searchAdapter by lazy {
        SearchAdapter { item, i ->
            hideKeyboard(this@NavigationActivity, album_search)
            view_data.visibility = View.GONE
            if (item.obj is Album) {
                val album = item.obj as Album
                startAlbumDetails(album)
            } else if (item.obj is Track) {
                val track = item.obj as Track
                CoroutineScope(Dispatchers.IO).launch {
                    val album = mViewModel.getAlbumById(track.albumId, track.category_id)
                    withContext(Dispatchers.Main) {
                        view_data.visibility = View.GONE
                        album?.let { startAlbumDetails(it) }
                    }
                }
            } else if (item.obj is Program) {
                val program = item.obj as Program
                if (program.isUnlocked) {
                    supportFragmentManager.beginTransaction().setCustomAnimations(
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
                    startActivity(
                        NewPurchaseActivity.newIntent(
                            this@NavigationActivity,
                            NewPurchaseActivity.QUANTUM_TIER_ID,
                            NewPurchaseActivity.QUANTUM_TIER_ID,
                            1
                        )
                    )
                }
            } else if (item.obj is Rife) {
                val rife = item.obj as Rife
                supportFragmentManager.beginTransaction().setCustomAnimations(
                    R.anim.trans_right_to_left_in,
                    R.anim.trans_right_to_left_out,
                    R.anim.trans_left_to_right_in,
                    R.anim.trans_left_to_right_out
                ).replace(
                    R.id.nav_host_fragment,
                    NewAlbumDetailFragment.newInstance(0, 0, Constants.TYPE_RIFE, rife.id),
                    NewAlbumDetailFragment().javaClass.simpleName
                ).commit()
            } else if (item.obj is Scalar) {
                onScalarSelect()
            }
        }
    }

    private var albumsSearch = MutableLiveData<List<Album>>()
    private var tracksSearch = MutableLiveData<List<Track>>()
    private var programsSearch = MutableLiveData<List<Program>>()

    private val mDisclaimerDialog by lazy {
        DisclaimerDialog(this@NavigationActivity,
            true,
            object : DisclaimerDialog.IOnSubmitListener {
                override fun submit(isCheck: Boolean) {
                    if (isCheck) {
                        preference(applicationContext).isShowDisclaimer = false
                    }
                }
            })
    }
    private var apkUrlDialog: String = ""
    private val mUpdateApkDialog by lazy {
        AlertDialog.Builder(this).setTitle(R.string.txt_warning_update_newversion_title)
            .setMessage(R.string.txt_warning_update_newversion).setCancelable(false)
            .setPositiveButton(R.string.txt_agree) { _, _ ->
                downloadAPK(apkUrlDialog)
            }.setNegativeButton(R.string.txt_disagree) { _, _ -> }.create()
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEvent(event: Any?) {
        runOnUiThread {
            if (event is DownloadInfo && event.total > 0) {
                mTvDownloadPercent.text = getString(
                    R.string.downloader_quantity_collapse, event.completed, event.total
                )
                if (event.completed < event.total) {
                    viewGroupDownload.visibility = View.VISIBLE
                }
            }

            if (event is DownloadErrorEvent && !QApplication.isActivityDownloadStarted) {
                AlertDialog.Builder(this@NavigationActivity).setTitle(R.string.download_error)
                    .setMessage(getString(R.string.download_error_message))
                    .setPositiveButton(R.string.txt_ok, null).show()
            }

            if (event == DownloadService.DOWNLOAD_FINISH) {
                findViewById<View>(R.id.viewGroupDownload).visibility = View.GONE
            }

            if (event is DownloadTrackErrorEvent) {
                if (isNetworkAvailable()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            mViewModel.reportTrack(event.id, event.url)
                        } catch (_: HttpException) {
                        }

                    }
                }
            }

            if (event is SyncDataEvent) {
                if (event.isSyncScalar) {
                    val user = PreferenceHelper.getUser(this)
                    if (user?.id != null) {
                        mViewModel.getScalar().observe(this) {

                        }
                    }
                } else {
                    syncData()
                }

            }
            if (event is String && event == "showDisclaimer") {
                if (preference(applicationContext).isShowDisclaimer && preference(applicationContext).isLogged && !mDisclaimerDialog.isShowing) {
                    mDisclaimerDialog.show()
                }
            }

//            if (event?.javaClass == PlayerRepeat::class.java) {
//                val repeat = event as PlayerRepeat
//                when (repeat.type) {
//                    Player.REPEAT_MODE_ALL -> showMode("Repeat All")
//                    Player.REPEAT_MODE_OFF -> showMode("Repeat Off")
//                    Player.REPEAT_MODE_ONE -> showMode("Repeat One")
//                }
//            }
//            if (event?.javaClass == PlayerShuffle::class.java) {
//                val shuffle = event as PlayerShuffle
//                if (shuffle.it) {
//                    showMode("Shuffle On")
//                } else {
//                    showMode("Shuffle Off")
//                }
//            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onAlarmsScheduleProgramEvent(event: ScheduleProgramStatusEvent?) {
        if (event?.isPlay == true && SharedPreferenceHelper.getInstance().getInt(Constants.PREF_SCHEDULE_PROGRAM_ID) != 0) {
            if ((isPlayAlbum || (playProgramId != SharedPreferenceHelper.getInstance().getInt(Constants.PREF_SCHEDULE_PROGRAM_ID) && isPlayProgram && !event.isSkipQuestion)) && !isUserPaused) {
                val dialogBuilder =
                    androidx.appcompat.app.AlertDialog.Builder(this@NavigationActivity)
                dialogBuilder.setMessage(getString(R.string.the_schedule_frequency_is_coming_up))
                    .setCancelable(false)
                    .setNegativeButton(getString(R.string.txt_no), null)
                    .setPositiveButton(getString(R.string.txt_yes)) { _, _ ->
                        playListScalar.clear()
                        fetchAndPlayProgram()
                    }.show()
            } else {
//                if (playProgramId == SharedPreferenceHelper.getInstance().getInt(Constants.PREF_SCHEDULE_PROGRAM_ID) && isPlayProgram) {
//                    if (isUserPaused) {
//                        clearDataPlayer()
//                    }
//                }
                if (!isPlayProgram || playProgramId != SharedPreferenceHelper.getInstance().getInt(Constants.PREF_SCHEDULE_PROGRAM_ID)) {
                    fetchAndPlayProgram()
                } else {
                    EventBus.getDefault().post("play player")
                }
            }
        } else {
            if (!isPlayAlbum && (playProgramId == SharedPreferenceHelper.getInstance().getInt(Constants.PREF_SCHEDULE_PROGRAM_ID) || event?.isClearScheduleProgram == true)) {
                EventBus.getDefault().post("pause player")
                if (event?.isHidePlayer == true && playListScalar.isEmpty()) {
                    clearDataPlayer()
                    isUserPaused = false
                    hidePlayerUI()
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onScheduleProgramProgressEvent(event: ScheduleProgramProgressEvent?) {
        QcAlarmManager.setScheduleProgramsAlarms(this@NavigationActivity)
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onUpdateViewSilentQuantumEvent(event: UpdateViewSilentQuantumEvent) {
        updateTabScalarQuantum()
    }

    private fun fetchAndPlayProgram() {
        var isPlaySync = true
        isPlayProgram = false
        isUserPaused = false
//        val program = PreferenceHelper.getScheduleProgram(this@NavigationActivity)
        mProgramDetailViewModel.program(SharedPreferenceHelper.getInstance().getInt(Constants.PREF_SCHEDULE_PROGRAM_ID)).observe(this@NavigationActivity) {
            if (isPlaySync) {
                isPlaySync = false
                programName = SharedPreferenceHelper.getInstance().get(PREF_SCHEDULE_PROGRAM_NAME)
                if (it != null && it.id != 0 && !isPlayProgram) {
                    val tracks: ArrayList<Search> = ArrayList()
                    mProgramDetailViewModel.convertData(it) { list ->
                        tracks.clear()
                        tracks.addAll(list)
                        if (currentTrack.value != null) {
                            val track = currentTrack.value
                            if (track is MusicRepository.Track) {
                                tracks.firstOrNull {
                                    (it.obj is Track) && it.id == track.trackId
                                }
                            }
                        }

                        //play program
                        val allPrograms = tracks.filter { it.obj !is Scalar }
                        if (allPrograms.isNotEmpty()) {
                            playPrograms(allPrograms.map { it.obj } as ArrayList<Any>, it.id)
                            EventBus.getDefault().post(PlayerSelected(0))
                        }

                        //play scalar
                        val listScalars =
                            tracks.filter { it.obj is Scalar }.map { it.obj as Scalar } as ArrayList<Scalar>
                        if (listScalars.isNotEmpty()) {
                            val lastScalar = listScalars.last()
                            playScalar = lastScalar
                            listScalars.removeLast()
                            playListScalar.clear()
                            playListScalar.addAll(listScalars)
                            playAndDownloadScalar(lastScalar)
                        } else {
                            //clear scalar
                            if (playListScalar.isNotEmpty()) {
                                val lastScalar = playListScalar.last()
                                playScalar = lastScalar
                                playAndDownloadScalar(lastScalar)
                            }
                        }
                    }
                }
            }
        }
    }


    private fun showMode(title: String) {
        bg_mode.isVisible = true
        bg_mode.txt_mode.text = title
        val timer = object : CountDownTimer(1300, 300) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                bg_mode.isVisible = false
            }
        }
        timer.start()
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun quantumOnCreate() {
        if (preference(applicationContext).isFirstSync) {
            createFolder()
        } else {
            hideFolder()
        }

        if (isNetworkAvailable() && preference(applicationContext).isLogged) {
            try {
                CoroutineScope(Dispatchers.IO).launch {
                    var apkList = listOf<String>()
                    try {
                        apkList = mViewModel.getApkList()
                    } catch (_: Exception) {
                    }
                    if (apkList.isNotEmpty()) {
                        val currentVer = BuildConfig.VERSION_NAME
                        val apkUrl = apkList.last()

                        val pathSplit = apkUrl.split("/")

                        if (pathSplit.isNotEmpty()) {
                            val fileName = pathSplit[pathSplit.size - 1]
                            val newVersion =
                                fileName.replace("Resonant_Console_", "").replace(".apk", "")
                            val newVs = newVersion.split(".")
                            val currentVs = currentVer.split(".")

                            if (newVs.size == 3 && currentVs.size == 3) {
                                if ((newVs[0].toInt() * 100 + newVs[1].toInt() * 10 + newVs[2].toInt()) > currentVs[0].toInt() * 100 + currentVs[1].toInt() * 10 + currentVs[2].toInt()) {
                                    checkDownloadApk {
                                        CoroutineScope(Dispatchers.Main).launch {
                                            if (!mUpdateApkDialog.isShowing) {
                                                apkUrlDialog = apkUrl
                                                mUpdateApkDialog.show()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (_: Exception) {
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(
                    downloadNewApkReceiver,
                    IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                    RECEIVER_EXPORTED
                )
            } else {
                registerReceiver(
                    downloadNewApkReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
                )
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    this, READ_MEDIA_AUDIO
                ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    this, READ_MEDIA_VIDEO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(READ_MEDIA_IMAGES, READ_MEDIA_AUDIO, READ_MEDIA_VIDEO),
                    REQUEST_CODE_PERMISSION
                )
            } else {
                deleteOldFiles()
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this, WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(WRITE_EXTERNAL_STORAGE), REQUEST_CODE_PERMISSION
                )
            } else {
                deleteOldFiles()
            }
        }
    }

    private fun deleteOldFiles() {
        //delete old files
        val oldFolder = File("/storage/emulated/0/.QuantumConsoleFrequenciesV3")
        val oldFolder1 = File("/storage/emulated/0/.QuantumConsoleFrequenciesHigherV3")
        val oldFolder2 = File("/storage/emulated/0/.QuantumConsoleFrequenciesInnerV3")

        oldFolder.deleteRecursively()
        oldFolder1.deleteRecursively()
        oldFolder2.deleteRecursively()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                if (preference(applicationContext).isLogged) {
                    Toast.makeText(applicationContext, "denied", Toast.LENGTH_SHORT).show()
                }
            } else {
                deleteOldFiles()
            }
        }
    }

    private fun createFolder() {
        if (BuildConfig.IS_FREE) {
            val root: String = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString()
            val myDir = File("$root/tracks")
            myDir.mkdir()
        } else {
            val root: String = getExternalFilesDir(null).toString()
            val myDir = File("$root/tracks")
            myDir.mkdir()
        }
    }

    private fun hideFolder() {
        if (BuildConfig.IS_FREE) {
            val root: String = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString()
            val oldFolder = File("$root/tracks")
            if (oldFolder.exists()) {
                val newFolder = File("$root/.tracks")
                oldFolder.renameTo(newFolder)
            }
        } else {
            val root: String = getExternalFilesDir(null).toString()
            val oldFolder = File("$root/tracks")
            if (oldFolder.exists()) {
                val newFolder = File("$root/.tracks")
                oldFolder.renameTo(newFolder)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (BuildConfig.IS_FREE) {
            quantumOnCreate()
        }
    }

    @OptIn(FlowPreview::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)

        EventBus.getDefault().register(this)

        checkPermissions()

        init()

        syncData()

        initSearch()

        getAllCategories()

        observeChatViewModel()

        initChatAdapter()

        updateTabScalarQuantum()

        if (BuildConfig.IS_FREE) {
            copyAssetsFiles()
        }

        if (!preference(applicationContext).isLogged) {
            startActivity(Intent(applicationContext, AuthActivity::class.java))
        }
        if (preference(applicationContext).isShowDisclaimer && preference(applicationContext).isLogged) {
            if (!mDisclaimerDialog.isShowing) {
                mDisclaimerDialog.show()
            }
        }

        FlowSearch.fromSearchView(album_search).debounce(500).map { text -> text.trim() }
            .distinctUntilChanged().asLiveData().observe(this) {
                if (it.isNotEmpty()) {
                    album_search_clear.visibility = View.VISIBLE
                    view_data.visibility = View.VISIBLE
                    search(it)
                } else {
                    clearSearch()
                    album_search_clear.visibility = View.GONE
                    view_data.visibility = View.GONE
                    hideKeyboard(applicationContext, album_search)
                }
            }

//        album_search.onFocusChangeListener = View.OnFocusChangeListener { _, b ->
//            if (b) {
//                view_data.visibility = View.VISIBLE
//            } else {
//                view_data.visibility = View.GONE
//                hideKeyboard(applicationContext, album_search)
//            }
//        }

        album_search_clear.setOnClickListener { closeSearch() }

        btnStartChatBot.setOnClickListener {
            isStartedChat = true
            viewIntroChatBot.clearAnimation()
            viewIntroChatBot.visibility = View.GONE
            btnStartChatBot.setImageResource(R.drawable.ic_avatar_chatboting)
            showChatPopup()
        }

        btnHideChatBot.setOnClickListener {
            viewIntroChatBot.clearAnimation()
            btnStartChatBot.visibility = View.GONE
            viewIntroChatBot.visibility = View.GONE
            btnHideChatBot.visibility = View.GONE
            SharedPreferenceHelper.getInstance().setBool(PREF_SETTING_CHATBOT_ON_OFF, false)
        }

        btnAddProgram.setOnClickListener {
            navigation_programs.performClick()
        }

        orientationChangesUI(resources.configuration.orientation)

        mNewProgramViewModel.getPrograms().observe(this@NavigationActivity) {
            if (it.isNotEmpty() && isStartScheduleProgram) {
                isStartScheduleProgram = false
                QcAlarmManager.setScheduleProgramsAlarms(
                    this@NavigationActivity
                )
            }
        }

        scheduleDailyWork()
    }

    private fun copyAssetsFiles() {
        CoroutineScope(Dispatchers.IO).launch {
            assets.copyAssetFolder(
                "tracks",
                getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString() + File.separator + "tracks"
            )
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        orientationChangesUI(newConfig.orientation)
        chatPopupWindow?.dismiss()
    }

    private fun orientationChangesUI(orientation: Int) {
        val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(0, 0)
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            params.weight = 2.0f
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            params.weight = 0.0f
        }

        search_divider.layoutParams = params
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        DownloadService.stopService(this)
        ScalarDownloadService.stopService(this)
        stopService(Intent(this, PlayerService::class.java))
        stopService(Intent(this, ScalarPlayerService::class.java))
        clearDataPlayer()
        QcAlarmManager.clearScheduleProgramsAlarms(this@NavigationActivity)
        WorkManager.getInstance(this).cancelUniqueWork("DailyWorker")
    }

    private fun init() {
        mViewModel = ViewModelProvider(
            this, ViewModelFactory(
                ApiHelper(RetrofitBuilder(applicationContext).apiService),
                DataBase.getInstance(applicationContext)
            )
        )[HomeViewModel::class.java]

        mNewProgramViewModel = ViewModelProvider(
            this, ViewModelFactory(
                ApiHelper(RetrofitBuilder(this).apiService), DataBase.getInstance(this)
            )
        )[NewProgramViewModel::class.java]

        mProgramDetailViewModel = ViewModelProvider(
            this, ViewModelFactory(
                ApiHelper(RetrofitBuilder(this).apiService), DataBase.getInstance(this)
            )
        )[ProgramDetailViewModel::class.java]

        mNewRifeViewModel = ViewModelProvider(
            this, ViewModelFactory(
                ApiHelper(RetrofitBuilder(this).apiService), DataBase.getInstance(this)
            )
        )[NewRifeViewModel::class.java]
        mNewScalarViewModel = ViewModelProvider(
            this, ViewModelFactory(
                ApiHelper(RetrofitBuilder(this).apiService), DataBase.getInstance(this)
            )
        )[NewScalarViewModel::class.java]

        mChatBotViewModel = ViewModelProvider(this)[ChatBotViewModel::class.java]

        navigation_home.onSelected {
            closeSearch()
            search_layout.visibility = View.VISIBLE
            setFragment(HomeFragment())
        }

        flash_sale.visibility = View.GONE //At the request of the client


        viewGroupDownload.setOnClickListener {
            startActivity(
                DownloaderActivity.newIntent(
                    applicationContext,
                )
            )
        }
        onButtonNavigationSelected()
    }


    private fun syncData() {
        if (isNetworkAvailable()) {
            mChatBotViewModel.createThreadChatBot()

            try {
                val user = PreferenceHelper.getUser(this)
                if (user?.id != null) {
                    mViewModel.syncProgramsToServer()
                }
                mViewModel.getHome("" + user?.id).observe(this) {
                    when (it.status) {
                        Resource.Status.SUCCESS -> {
                            if (preference(applicationContext).isFirstSync) {
                                preference(applicationContext).isFirstSync = true
                                if (it.data == null && !BuildConfig.IS_FREE) {
                                    try {
                                        mViewModel.loadFromCache(applicationContext)
                                    } catch (_: Exception) {
                                    }
                                }
                            }
                        }

                        Resource.Status.ERROR -> {
//                        if (BuildConfig.IS_FREE) {
//                            mViewModel.loadDataLastHomeResponse(this@NavigationActivity)
//                        }
                        }

                        Resource.Status.LOADING -> {
                        }
                    }
                }
                if (user?.id != null) {
                    mViewModel.getRife().observe(this) {
                        mNewRifeViewModel.getRifeLocal {

                        }
                    }
                }
                if (user?.id != null) {
                    mViewModel.getScalar().observe(this) {
                        mNewScalarViewModel.getScalarLocal {

                        }
                    }
                }
            } catch (_: Exception) {
            }
        } else {
//            if (BuildConfig.IS_FREE) {
//                mViewModel.loadDataLastHomeResponse(this@NavigationActivity)
//            }
        }
    }

    private fun setFragment(fragment: Fragment) {
        selectedNaviFragment = fragment
        album_search.clearFocus()
        supportFragmentManager.beginTransaction().replace(
            R.id.nav_host_fragment, fragment, fragment.javaClass.simpleName
        ).commit()
    }

    private fun setFragmentBackAnimation(fragment: Fragment) {
        selectedNaviFragment = fragment
        album_search.clearFocus()
        supportFragmentManager.beginTransaction().setCustomAnimations(
            R.anim.trans_right_to_left_in,
            R.anim.trans_right_to_left_out,
            R.anim.trans_left_to_right_in,
            R.anim.trans_left_to_right_out
        ).replace(R.id.nav_host_fragment, fragment, fragment.javaClass.simpleName).commit()
    }

    @Suppress("UNSAFE_CALL_ON_PARTIALLY_DEFINED_RESOURCE")
    private fun onButtonNavigationSelected() {
//        askRating()
//        hideKeyboard(applicationContext, album_search)

        navigation_home.setOnClickListener {
            navigation_home.onSelected {
                closeSearch()
                search_layout.visibility = View.VISIBLE
                setFragment(HomeFragment())
            }
        }
        navigation_scalar.setOnClickListener {
            navigation_scalar.onSelected {
                closeSearch()
                search_layout.visibility = View.VISIBLE
                setFragment(NewScalarFragment())
            }
        }
        navigation_albums.setOnClickListener {
            navigation_albums.onSelected {
                closeSearch()
                search_layout.visibility = View.VISIBLE
                setFragment(TiersPagerFragment())
            }
        }
        navigation_rife.setOnClickListener {
            navigation_rife.onSelected {
                closeSearch()
                isTrackAdd = false
                if (Utils.isTablet(this)) {
                    search_layout.visibility = View.VISIBLE
                } else {
                    search_layout.visibility = View.GONE
                }
                setFragment(NewRifeFragment())
            }
        }
        navigation_programs.setOnClickListener {
            navigation_programs.onSelected {
                closeSearch()
                isTrackAdd = false
                search_layout.visibility = View.VISIBLE
                setFragment(NewProgramFragment())
            }
        }
        navigation_videos.setOnClickListener {
            navigation_videos.onSelected {
                closeSearch()
                search_layout.visibility = View.VISIBLE
                setFragment(NewVideosFragment())
            }
        }
        navigation_discover.setOnClickListener {
            navigation_discover.onSelected {
                closeSearch()
                search_layout.visibility = View.VISIBLE
                setFragment(DiscoverFragment())
            }
        }
        navigation_options.setOnClickListener {
            navigation_options.onSelected {
                closeSearch()
                search_layout.visibility = View.VISIBLE
                setFragment(NewOptionsFragment())
            }
        }
    }

    fun showPlayerUI() {
        if (playerUI == null) {
            val fragmentList = supportFragmentManager.fragments
            val playerUIFragment = fragmentList.lastOrNull {
                it is PlayerUIFragment
            } as PlayerUIFragment?
            if (playerUIFragment == null) {
                playerUI = PlayerUIFragment()
                supportFragmentManager.beginTransaction()
                    .add(R.id.player_ui_container, playerUI!!, playerUI!!.javaClass.simpleName)
                    .commitNow()
            } else {
                playerUI = playerUIFragment
            }
        }
    }

    fun hidePlayerUI() {
        playerUI = try {
            val fragmentList = supportFragmentManager.fragments
            fragmentList.forEach { fragment ->
                if (fragment is PlayerUIFragment) {
                    supportFragmentManager.beginTransaction().remove(fragment).commitNow()
                }
            }
            null
        } catch (_: Exception) {
            if (playerUI != null) {
                supportFragmentManager.beginTransaction().remove(playerUI!!).commitNow()
            }
            null
        }
    }

    private fun updateTabScalarQuantum() {
        if (SharedPreferenceHelper.getInstance().getBool(PREF_SETTING_ADVANCE_SCALAR_ON_OFF)) {
            navigation_scalar.visibility = View.VISIBLE
        } else {
            navigation_scalar.visibility = View.GONE
        }
    }

    fun onScalarSelect() {
        if (mViewGroupCurrent == navigation_scalar && supportFragmentManager.fragments.lastOrNull() is NewAlbumDetailFragment) {
            var fragment = selectedNaviFragment
            if (fragment == null) {
                fragment = NewScalarFragment()
            }
            supportFragmentManager.beginTransaction().setCustomAnimations(
                R.anim.trans_left_to_right_in,
                R.anim.trans_left_to_right_out,
                R.anim.trans_right_to_left_in,
                R.anim.trans_right_to_left_out
            ).replace(R.id.nav_host_fragment, fragment, fragment.javaClass.simpleName).commit()
        }
        navigation_scalar.onSelected {
            closeSearch()
            search_layout.visibility = View.VISIBLE
            setFragmentBackAnimation(NewScalarFragment())
        }
    }

    fun onQuantumSelect() {
        if (mViewGroupCurrent == navigation_albums && supportFragmentManager.fragments.lastOrNull() is NewAlbumDetailFragment) {
            var fragment = selectedNaviFragment
            if (fragment == null) {
                fragment = TiersPagerFragment()
            }
            supportFragmentManager.beginTransaction().setCustomAnimations(
                R.anim.trans_left_to_right_in,
                R.anim.trans_left_to_right_out,
                R.anim.trans_right_to_left_in,
                R.anim.trans_right_to_left_out
            ).replace(R.id.nav_host_fragment, fragment, fragment.javaClass.simpleName).commit()
        }
        navigation_albums.onSelected {
            closeSearch()
            search_layout.visibility = View.VISIBLE
            setFragmentBackAnimation(TiersPagerFragment())
        }
    }

    private fun View.onSelected(listener: () -> Unit) {
//        if (mViewGroupCurrent != this) {
        mViewGroupCurrent?.isSelected = false
        mViewGroupCurrent = this
        mViewGroupCurrent?.isSelected = true
        listener.invoke()
//        }
    }

    //region SEARCH
    private fun initSearch() {
        search_categories_recycler.apply {
            adapter = searchAdapter
            itemAnimator = null
        }
        Combined5LiveData(albumsSearch,
            tracksSearch,
            programsSearch,
            mNewRifeViewModel.result,
            mNewScalarViewModel.result,
            combine = { data1, data2, data3, data4, data5 ->
                val search = mutableListOf<Search>()
                var i = 0
//              Frequencies
                data2?.let {
                    val converted = ArrayList<Track>()
                    it.forEach { track ->
                        if (track.tier_id == 1 || track.tier_id == 2) {
                            converted.add(track)
                            search.add(Search(i, track))
                            i++
                        } else {
                            if (track.tier_id == 3 && (preference(applicationContext).isHighQuantum || BuildConfig.IS_FREE)) {
                                converted.add(track)
                                search.add(Search(i, track))
                                i++
                            }
                            if (track.tier_id == 4 && (preference(applicationContext).isInnerCircle || BuildConfig.IS_FREE)) {
                                converted.add(track)
                                search.add(Search(i, track))
                                i++
                            }
                        }
                    }
                }
//              Albums
                data1?.let {
                    val converted = ArrayList<Album>()
                    if (searchAdapter.getCategories().isEmpty()) {
                        getAllCategories()
                    }
                    if (BuildConfig.IS_FREE) {
                        it.forEach { album ->
                            search.add(Search(i, album))
                            i++
                        }
                        converted.addAll(it)
                    } else {
                        it.forEach { album ->
                            if (album.tier_id == 1 || album.tier_id == 2) {
                                converted.add(album)
                                search.add(Search(i, album))
                                i++
                            } else {
                                if (album.tier_id == 3 && (preference(applicationContext).isHighQuantum)) {
                                    converted.add(album)
                                    search.add(Search(i, album))
                                    i++
                                }
                                if (album.tier_id == 4 && (preference(applicationContext).isInnerCircle)) {
                                    converted.add(album)
                                    search.add(Search(i, album))
                                    i++
                                }
                            }
                        }
                    }
                    val groupAlbum = converted.groupingBy { a -> a.name }.eachCount()
                    searchAdapter.setGroupAlbum(groupAlbum)
                }
//              Programs
                data3?.let {
                    it.forEach { program ->
                        search.add(Search(i, program))
                        i++
                    }
                }
//              Rife
                data4?.let {
                    it.forEach { rife ->
                        search.add(Search(i, rife))
                        i++
                    }
                }

//                Scalar
                data5?.let {
                    it.forEach { scalar ->
                        search.add(Search(i, scalar))
                        i++
                    }
                }

                return@Combined5LiveData search
            }).observe(this) {
            if (it.isEmpty()) {
                lblnoresult.visibility = View.VISIBLE
            } else {
                search_categories_recycler.scrollToPosition(0)
                lblnoresult.visibility = View.GONE
            }
            searchAdapter.submitList(it)
        }
    }

    private fun search(s: CharSequence) {
        CoroutineScope(Dispatchers.IO).launch {
            val albums = mViewModel.searchAlbum("%$s%")
            val tracks = mViewModel.searchTrack("%$s%")
            val programs = mViewModel.searchProgram("%$s%")
            withContext(Dispatchers.Main) {
                mNewRifeViewModel.searchMain(s.toString())
            }
            withContext(Dispatchers.Main) {
                mNewScalarViewModel.searchMain(s.toString())
            }
            CoroutineScope(Dispatchers.Main).launch {
                albumsSearch.value = albums
                tracksSearch.value = tracks
                programsSearch.value = programs
            }
        }
    }

    private fun getAllCategories() {
        val categoryDao = DataBase.getInstance(applicationContext).categoryDao()
        CoroutineScope(Dispatchers.IO).launch {
            val categories = categoryDao.getData()
            withContext(Dispatchers.Main) {
                searchAdapter.setCategories(categories)
            }
        }
    }

    private fun closeSearch() {
        album_search.text.clear()
        album_search.clearFocus()
        hideKeyboard(applicationContext, album_search)
    }

    private fun clearSearch() {
        searchAdapter.submitList(arrayListOf())
    }

    private fun startAlbumDetails(album: Album) {
        if (!album.isUnlocked && album.unlock_url != null && album.unlock_url!!.isNotEmpty()) {
            startActivity(
                PurchaseItemAlbumWebView.newIntent(this, album.unlock_url!!)
            )
        } else if (album.isUnlocked) {
            supportFragmentManager.beginTransaction().setCustomAnimations(
                R.anim.trans_right_to_left_in,
                R.anim.trans_right_to_left_out,
                R.anim.trans_left_to_right_in,
                R.anim.trans_left_to_right_out
            ).replace(
                R.id.nav_host_fragment,
                NewAlbumDetailFragment.newInstance(id = album.id, categoryId = album.category_id),
                NewAlbumDetailFragment().javaClass.simpleName
            ).commit()
        } else {
            startActivity(
                NewPurchaseActivity.newIntent(
                    applicationContext, album.category_id, album.tier_id, album.id
                )
            )
        }
    }

    override fun onAlbumDetails(album: Album) {
        startAlbumDetails(album)
    }

    override fun onLongAlbumDetails(album: Album) {
        val user = PreferenceHelper.getUser(this@NavigationActivity)
        if (user != null && (user.email == "kristenaizalapina@gmail.com" || user.email == "tester02@yopmail.com" || user.email == "manufacturing@qilifestore.com")) {
            val track = album.tracks.first()
            androidx.appcompat.app.AlertDialog.Builder(this@NavigationActivity)
                .setTitle(R.string.app_name).setMessage(
                    getSaveDir(
                        this@NavigationActivity, track.filename, track.album?.audio_folder ?: ""
                    )
                ).setPositiveButton(R.string.txt_ok, null).show()
        }
    }
//endregion

    private fun dialogConfirmUpdateApk(apkUrl: String) {
        try {
            AlertDialog.Builder(this).setTitle(R.string.txt_warning_update_newversion_title)
                .setMessage(R.string.txt_warning_update_newversion).setCancelable(false)
                .setPositiveButton(R.string.txt_agree) { _, _ ->
                    downloadAPK(apkUrl)
                }.setNegativeButton(R.string.txt_disagree) { _, _ -> }.show()
        } catch (_: Throwable) {
        }
    }

    private fun deleteAPKFolder() {
        val apkFile = File(getExternalFilesDir(null), ".cache/cache.apk")
        if (apkFile.exists()) {
            apkFile.delete()
        }
    }

    private fun checkDownloadApk(onDownload: (DownloadManager) -> Unit) {
        var isFindApk = true
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query()
        val cursor = downloadManager.query(query)
        while (cursor.moveToNext()) {

            val cStatus = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            val cTitle = cursor.getColumnIndex(DownloadManager.COLUMN_TITLE)
            val cIndex = cursor.getColumnIndex(DownloadManager.COLUMN_ID)
            val cUri = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)

            val status = cursor.getInt(cStatus)
            val fileName = cursor.getString(cTitle)
            val downloadId = cursor.getLong(cIndex)
            val uri = cursor.getString(cUri)


            if (fileName == "[New APK] Quantum Frequencies") {
                when (status) {
                    DownloadManager.STATUS_PENDING -> {
                        isFindApk = false
                        refId = downloadId
                        mLocalApkPath = try {
                            Uri.parse(uri).path
                        } catch (_: Exception) {
                            null
                        }
                    }

                    DownloadManager.STATUS_RUNNING -> {
                        isFindApk = false
                        refId = downloadId
                        mLocalApkPath = try {
                            Uri.parse(uri).path
                        } catch (_: Exception) {
                            null
                        }
                    }

                    DownloadManager.STATUS_PAUSED -> {}

                    DownloadManager.STATUS_SUCCESSFUL -> {
                        refId = downloadId
                        mLocalApkPath = try {
                            Uri.parse(uri).path
                        } catch (_: Exception) {
                            null
                        }
                    }

                    DownloadManager.STATUS_FAILED -> {}
                }
                break
            }
        }
        cursor.close()
        if (isFindApk) {
            onDownload.invoke(downloadManager)
        }
    }

    private fun downloadAPK(apkUrl: String) {
        checkDownloadApk { downloadManager ->
            try {
                val apkFile = File(getExternalFilesDir(null), ".cache/cache.apk")

                if (apkFile.exists()) {
                    apkFile.delete()
                }
                mLocalApkPath = apkFile.path

                val request = DownloadManager.Request(Uri.parse(apkUrl))
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                request.setAllowedOverRoaming(false)
                request.setTitle("[New APK] Quantum Frequencies")
                val uri = Uri.fromFile(apkFile)
                request.setDestinationUri(uri)
                refId = downloadManager.enqueue(request)
                Toast.makeText(
                    applicationContext, getString(R.string.txt_downloading_dot), Toast.LENGTH_LONG
                ).show()

            } catch (e: Exception) {
                Log.d("downloadAPK", "downloadAPK: $e");
            }
        }

    }

    private val downloadNewApkReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctxt: Context, intent: Intent) {
            val referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (refId == referenceId) {
                Toast.makeText(
                    applicationContext,
                    getString(R.string.toast_download_complete),
                    Toast.LENGTH_LONG
                ).show()
                autoInstallNewAPK()
                unregisterReceiver(this)
            }
        }
    }

    private fun autoInstallNewAPK() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canInstallFromUnknownSources = packageManager.canRequestPackageInstalls()
            if (!canInstallFromUnknownSources) {
                val intentNew = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).setData(
                    Uri.parse(String.format("package:%s", packageName))
                )
                startActivityForResult(intentNew, REQUEST_CODE_BEFORE_INSTALL)
                return
            }
        }
        checkDownloadApk {
            mLocalApkPath?.let { path ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val install = Intent(Intent.ACTION_INSTALL_PACKAGE)
                    install.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    val apkUri =
                        FileProvider.getUriForFile(
                            this,
                            BuildConfig.APPLICATION_ID + ".provider",
                            File(path)
                        )
                    install.data = apkUri
                    startActivityForResult(install, REQUEST_CODE_AFTER_INSTALL)
                } else {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(
                        FileProvider.getUriForFile(
                            applicationContext, BuildConfig.APPLICATION_ID + ".provider", File(path)
                        ), "application/vnd.android.package-archive"
                    )
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivityForResult(intent, REQUEST_CODE_AFTER_INSTALL)
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_BEFORE_INSTALL -> autoInstallNewAPK()
            REQUEST_CODE_AFTER_INSTALL -> deleteAPKFolder()
        }
    }

    companion object {
        private const val REQUEST_CODE_BEFORE_INSTALL: Int = 1234
        private const val REQUEST_CODE_AFTER_INSTALL: Int = 5678
    }
//endregion

    override fun onRefreshTiers() {
        if (BuildConfig.IS_FREE) {
            if (isNetworkAvailable()) {
                mViewModel.getProfile().observe(this) { user ->
                    user?.let { resource ->
                        when (resource.status) {
                            Resource.Status.SUCCESS -> {
                                user.data?.let { u -> updateTier(applicationContext, u) }
                            }

                            Resource.Status.ERROR -> {
                            }

                            Resource.Status.LOADING -> {
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onConnectionOpen(task: BaseTask<*>?) {

    }

    override fun onConnectionSuccess(task: BaseTask<*>?, data: Any?) {
        if (task is GetFlashSaleTask) {
            val jsonFlashSale = data as String
            if (jsonFlashSale.isNotEmpty()) {
                val jsonCurrent = Gson().fromJson(jsonFlashSale, GetFlashSaleOutput::class.java)

//                Luon hien flash sale
//                jsonCurrent.flashSale.enable = true
//                jsonFlashSale = Gson().toJson(jsonCurrent)

                var flashsaleCurrentString = ""
                if (jsonCurrent?.flashSale != null) {
                    flashsaleCurrentString = Gson().toJson(jsonCurrent.flashSale)
                }
                val jsonOrgrialString =
                    SharedPreferenceHelper.getInstance().get(Constants.PREF_FLASH_SALE)
                var flashsaleOrgrialString = ""
                if (jsonOrgrialString != null) {
                    val jsonOrgrial =
                        Gson().fromJson(jsonOrgrialString, GetFlashSaleOutput::class.java)
                    if (jsonOrgrial?.flashSale != null) {
                        flashsaleOrgrialString = Gson().toJson(jsonOrgrial.flashSale)
                    }
                }

                SharedPreferenceHelper.getInstance().set(Constants.PREF_FLASH_SALE, jsonFlashSale)

                if (!flashsaleCurrentString.equals(flashsaleOrgrialString, ignoreCase = true)) {
                    SharedPreferenceHelper.getInstance()
                        .setInt(Constants.PREF_FLASH_SALE_COUNTERED, 0)
                }

                if (SharedPreferenceHelper.getInstance()
                        .getInt(Constants.PREF_FLASH_SALE_COUNTERED) <= jsonCurrent.flashSale.proposalsCount!!
                ) {
                    QcAlarmManager.createAlarms(this)
                } else {
                    QcAlarmManager.clearAlarms(this)
                }

                //Create reminder
                QcAlarmManager.createReminderAlarm(this)

                loadCountdownTime()
            } else {
                QcAlarmManager.clearAlarms(this)
            }
        }
    }

    override fun onConnectionError(task: BaseTask<*>?, exception: Exception?) {

    }

    fun loadCountdownTime() {
        val flashSaleRemainTimeGloble = Utils.getFlashSaleRemainTime()
        if (flashSaleRemainTimeGloble > 0) {
            setCountdownTimer(flashSaleRemainTimeGloble)
        } else {
            flash_sale.visibility = View.GONE
        }
    }

    private var mCountDownTimer: CountDownTimer? = null

    private fun setCountdownTimer(totalTime: Long) {
        if (mCountDownTimer != null) {
            mCountDownTimer!!.cancel()
        }
        mCountDownTimer = object : CountDownTimer(totalTime, 1000) {
            override fun onTick(l: Long) {
                val totalSeconds = (l / 1000).toInt()
                val days = totalSeconds / (24 * 3600)
                var remainder = totalSeconds - (days * 24 * 3600)
                val hours = remainder / 3600
                remainder -= (hours * 3600)
                val mins = remainder / 60
                remainder -= mins * 60
                val secs = remainder
                val hour: String = if (hours > 9) "" + hours else "0$hours"
                val min: String = if (mins > 9) "" + mins else "0$mins"
                val second: String = if (secs > 9) "" + secs else "0$secs"
                if (SharedPreferenceHelper.getInstance()
                        .getBool(Constants.KEY_PURCHASED) && SharedPreferenceHelper.getInstance()
                        .getBool(Constants.KEY_PURCHASED_ADVANCED) && SharedPreferenceHelper.getInstance()
                        .getBool(Constants.KEY_PURCHASED_HIGH_ABUNDANCE) && SharedPreferenceHelper.getInstance()
                        .getBool(Constants.KEY_PURCHASED_HIGH_QUANTUM)
                ) {
                    flash_sale.visibility = View.GONE
                } else {
                    flash_sale.visibility = View.GONE
                }
                flash_sale_hours.text = hour
                flash_sale_minutes.text = min
                flash_sale_seconds.text = second
            }

            override fun onFinish() {
                flash_sale.visibility = View.GONE
//                initComponents()
            }
        }
        mCountDownTimer!!.start()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun resetChatBot() {
        SharedPreferenceHelper.getInstance().set(Constants.PREF_CHATBOT_THREAD_ID, null)
        SharedPreferenceHelper.getInstance().set(Constants.PREF_CHAT_MESSAGES, null)
        isStartedChat = false
        chatMessages.clear()
        msgChatAdapter?.notifyDataSetChanged()
        mChatBotViewModel.createThreadChatBot()
        Handler(Looper.getMainLooper()).postDelayed({
            btnStartChatBot.setImageResource(R.drawable.ic_avatar_chatbot)
        }, 2000)
    }

    fun updateViewChat() {
        val fragment = supportFragmentManager.fragments.lastOrNull()
        var isHide = false
        if (fragment is NewProgramFragment && isTrackAdd) {
            isHide = true
        }
        if (SharedPreferenceHelper.getInstance()
                .getBool(PREF_SETTING_CHATBOT_ON_OFF) && !isHide && (fragment is NewScalarFragment
                    || fragment is TiersPagerFragment
                    || fragment is NewRifeFragment
                    || fragment is NewProgramFragment
                    || fragment is NewVideosFragment
                    || fragment is DiscoverFragment)
        ) {
            if (btnStartChatBot.visibility == View.GONE) {
                btnStartChatBot.visibility = View.VISIBLE
                btnHideChatBot.visibility = View.VISIBLE
                if (!isStartedChat) {
                    viewIntroChatBot.showViewWithFadeIn()
                }
            }
        } else {
            btnStartChatBot.visibility = View.GONE
            viewIntroChatBot.clearAnimation()
            viewIntroChatBot.visibility = View.GONE
            btnHideChatBot.visibility = View.GONE
        }

        if (fragment is HomeFragment) {
            btnAddProgram.visibility = View.VISIBLE
        } else {
            btnAddProgram.visibility = View.GONE
        }
    }

    private fun initChatAdapter() {
        chatMessages = SharedPreferenceHelper.getInstance().chatMessages
        msgChatAdapter = MessageChatBotAdapter(chatMessages, onAlbumClick = { albumName ->
            CoroutineScope(Dispatchers.IO).launch {
                val album = mViewModel.getAlbumNameOne(albumName)
                withContext(Dispatchers.Main) {
                    album?.let {
                        chatPopupWindow?.dismiss()
                        if (!album.isUnlocked && album.unlock_url != null && album.unlock_url!!.isNotEmpty()) {
                            startActivity(
                                PurchaseItemAlbumWebView.newIntent(
                                    this@NavigationActivity,
                                    album.unlock_url!!
                                )
                            )
                        } else if (album.isUnlocked) {
                            startAlbumDetails(album)
                        } else {
                            startActivity(
                                NewPurchaseActivity.newIntent(
                                    this@NavigationActivity,
                                    album.category_id,
                                    album.tier_id,
                                    album.id
                                )
                            )
                        }
                    }
                }
            }
        }, onUpdateTextTyping = {
            scrollToBottomTyping()
        }, onUpdateTextTypingComplete = {
            btnSendChat?.isEnabled = edtMessageChat?.text.toString().trim { it <= ' ' }.isNotEmpty()
        })
        viewIntroChatBot.showViewWithFadeIn()
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    private fun showChatPopup() {
        getHeightPlayer { heightPlayer ->
            val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popupView: View = inflater.inflate(R.layout.popup_chat_bot, null)
            chatPopupWindow = PopupWindow(
                popupView,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            ).apply {
                isFocusable = true
                isTouchable = true
                isOutsideTouchable = true
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
            chatPopupWindow?.showAtLocation(btnStartChatBot, Gravity.BOTTOM, 0, 0)
            val btnCloseChatBot = popupView.findViewById<View>(R.id.btnCloseChatBot)
            if (Utils.isTablet(this@NavigationActivity)) {
                val viewChatContent = popupView.findViewById<CardView>(R.id.viewChatContent)
                val btnCloseChatBotInvisible =
                    popupView.findViewById<View>(R.id.btnCloseChatBotInvisible)
                val layoutParams = viewChatContent.layoutParams as ViewGroup.MarginLayoutParams
                layoutParams.bottomMargin =
                    heightPlayer + resources.getDimensionPixelSize(R.dimen.margin_bottom_chat)
                viewChatContent.layoutParams = layoutParams
                btnCloseChatBotInvisible.setOnClickListener {
                    btnCloseChatBot.performClick()
                }
            }
            btnCloseChatBot.setOnClickListener {
                msgChatAdapter?.isCancelWrite = true
                msgChatAdapter?.notifyDataSetChanged()
                chatPopupWindow?.dismiss()
            }
            rvChatBot = popupView.findViewById(R.id.rvChatBot)
            edtMessageChat = popupView.findViewById(R.id.edtMessageChat)
            btnSendChat = popupView.findViewById(R.id.btnSendMessageChat)
            btnSendChat?.isEnabled = false

            val linearLayoutManager = LinearLayoutManager(this)
            rvChatBot?.setLayoutManager(linearLayoutManager)
            msgChatAdapter?.isCancelWrite = false
            msgChatAdapter?.isTextAnimation = true
            rvChatBot?.adapter = msgChatAdapter
            rvChatBot?.viewTreeObserver?.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    rvChatBot?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                    rvChatBot?.scrollToPosition(chatMessages.size - 1)
                }
            })

            scrollToBottomWithOffset()

            edtMessageChat?.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    btnSendChat?.isEnabled = s.toString().trim { it <= ' ' }
                        .isNotEmpty() && chatMessages.last().message != "Typing" && chatMessages.last().statusTyping == false
                }

                override fun beforeTextChanged(
                    s: CharSequence, start: Int, count: Int,
                    after: Int,
                ) {

                }

                override fun afterTextChanged(s: Editable) {

                }
            })
            btnSendChat?.setOnClickListener {
                val question: String = edtMessageChat?.getText().toString().trim { it <= ' ' }
                if (question.isNotEmpty()) {
                    addToChat(question, MessageChatBot.SEND_BY_ME)
                    edtMessageChat?.setText("")
                    mChatBotViewModel.sendMessageChat(question)
                }
            }
        }
    }

    private fun getHeightPlayer(onHeight: (Int) -> Unit) {
        val playerFragment = supportFragmentManager.fragments.firstOrNull { it is PlayerUIFragment }
        if (playerFragment?.isVisible == true) {
            playerFragment.view?.post {
                onHeight.invoke((playerFragment.view?.height ?: 0))
            }
        } else {
            onHeight.invoke(0)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeChatViewModel() {
        mChatBotViewModel.apply {
            typingMessage.observe(this@NavigationActivity) {
                chatMessages.add(it)
                msgChatAdapter?.notifyDataSetChanged()
                scrollToBottomWithOffset()
            }
            bodyMessage.observe(this@NavigationActivity) {
                if (chatMessages.isNotEmpty()) {
                    chatMessages.removeAt(chatMessages.size - 1)
                }
                msgChatAdapter?.isTextAnimation = false
                addToChat(it, MessageChatBot.SEND_BY_BOT)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun addToChat(message: String, sendBy: String) {
        runOnUiThread {
            chatMessages.add(MessageChatBot(message, sendBy))
            msgChatAdapter?.notifyDataSetChanged()
            SharedPreferenceHelper.getInstance().saveChatMessages(chatMessages)
            scrollToBottomWithOffset()
        }
    }


    private fun scrollToBottomWithOffset() {
        rvChatBot?.post {
            val totalItemCount = rvChatBot?.adapter?.itemCount ?: 0
            if (totalItemCount > 0) {
                val positionToScroll = totalItemCount - 1
                rvChatBot?.scrollToPosition(positionToScroll)
                rvChatBot?.postDelayed({
                    rvChatBot?.scrollBy(0, 1000)
                }, 10)
            }
        }
    }

    private fun scrollToBottomTyping() {
        rvChatBot?.post {
            rvChatBot?.scrollBy(0, 1000)
        }
    }

    fun clearDataPlayer() {
        playProgramId = -1
        isPlayProgram = false
        isPlayAlbum = false
        playListScalar.clear()
        playingScalar = false
        trackList?.clear()
        currentPosition.postValue(0)
        currentTrack.value = null
        currentTrackIndex.value = 0
        playRife = null
        max.value = 0
        duration.value = 0
    }

    private fun playPrograms(tracks: ArrayList<Any>, programId: Int) {
        playRife = null
        if (isPlayAlbum || playProgramId != programId) {
            hidePlayerUI()
        }
        isPlayAlbum = false
        playAlbumId = -1
        isPlayProgram = true
        playProgramId = programId
        CoroutineScope(Dispatchers.IO).launch {
            val data = tracks.mapNotNull { t ->
                when (t) {
                    is Track -> {
                        MusicRepository.Track(
                            t.id,
                            t.name,
                            t.album?.name ?: "",
                            t.albumId,
                            t.album!!,
                            R.drawable.launcher,
                            t.duration,
                            0,
                            t.filename
                        )
                    }

                    is MusicRepository.Frequency -> {
                        t
                    }

                    else -> null
                }
            } as ArrayList<MusicRepository.Music>

            trackList?.clear()
            trackList = data
            val mIntent = Intent(applicationContext, PlayerService::class.java).apply {
                putParcelableArrayListExtra("playlist", arrayListOf<MusicRepository.Music>())
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    stopService(mIntent)
                    startForegroundService(mIntent)
                } else {
                    stopService(mIntent)
                    startService(mIntent)
                }
            } catch (_: Exception) {
            }
            CoroutineScope(Dispatchers.Main).launch {
                showPlayerUI()
            }
        }
    }

    private fun playAndDownloadScalar(scalar: Scalar) {
        if (Utils.isConnectedToNetwork(this@NavigationActivity)) {
            CoroutineScope(Dispatchers.IO).launch {
                val file =
                    File(getSaveDir(this@NavigationActivity, scalar.audio_file, scalar.audio_folder))
                val preloaded =
                    File(
                        getPreloadedSaveDir(
                            this@NavigationActivity,
                            scalar.audio_file,
                            scalar.audio_folder
                        )
                    )
                if (!file.exists() && !preloaded.exists()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                this@NavigationActivity, Manifest.permission.READ_MEDIA_IMAGES
                            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                                this@NavigationActivity, Manifest.permission.READ_MEDIA_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                                this@NavigationActivity, Manifest.permission.READ_MEDIA_VIDEO
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            ScalarDownloadService.startService(context = this@NavigationActivity, scalar)
                        } else {
                            ActivityCompat.requestPermissions(
                                this@NavigationActivity, arrayOf(
                                    Manifest.permission.READ_MEDIA_IMAGES,
                                    Manifest.permission.READ_MEDIA_AUDIO,
                                    Manifest.permission.READ_MEDIA_VIDEO
                                ), 1001
                            )
                        }
                    } else {
                        if (ContextCompat.checkSelfPermission(
                                this@NavigationActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            ScalarDownloadService.startService(context = this@NavigationActivity, scalar)
                        } else {
                            ActivityCompat.requestPermissions(
                                this@NavigationActivity,
                                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                1001
                            )
                        }
                    }
                }
            }
        } else {
            Toast.makeText(
                this@NavigationActivity, getString(R.string.err_network_available), Toast.LENGTH_SHORT
            ).show()
        }
        playStopScalar("ADD_REMOVE")
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun playStopScalar(actionScalar: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val playIntent = Intent(this@NavigationActivity, ScalarPlayerService::class.java).apply {
                    action = actionScalar
                }
                this@NavigationActivity.startService(playIntent)
            } catch (_: Exception) {
            }
            CoroutineScope(Dispatchers.Main).launch { showPlayerUI() }
        }
    }

    private fun scheduleDailyWork() {
        val currentDateTime = Calendar.getInstance()
        val midnight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 1)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_MONTH, 1)
        }

        val initialDelay = midnight.timeInMillis - currentDateTime.timeInMillis
        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(applicationContext)
            .enqueueUniquePeriodicWork(
                "DailyWorker",
                ExistingPeriodicWorkPolicy.REPLACE,
                dailyWorkRequest
            )
    }


//    fun askRating() {
//        AppRating.Builder(this)
//            .setMinimumLaunchTimes(9)
//            .setMinimumDays(3)
//            .setMinimumLaunchTimesToShowAgain(9)
//            .setMinimumDaysToShowAgain(3)
//            .setRatingThreshold(RatingThreshold.FOUR)
//            .setConfirmButtonClickListener(ConfirmButtonClickListener {
//                AppRating.openPlayStoreListing(
//                    this@NavigationActivity
//                )
//            })
//            .showIfMeetsConditions()
//    }

}