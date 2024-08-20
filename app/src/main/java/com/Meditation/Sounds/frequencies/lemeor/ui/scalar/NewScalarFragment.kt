package com.Meditation.Sounds.frequencies.lemeor.ui.scalar

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.feature.base.BaseFragment
import com.Meditation.Sounds.frequencies.lemeor.data.api.RetrofitBuilder
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.model.Scalar
import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper
import com.Meditation.Sounds.frequencies.lemeor.data.utils.ViewModelFactory
import com.Meditation.Sounds.frequencies.lemeor.getPreloadedSaveDir
import com.Meditation.Sounds.frequencies.lemeor.getSaveDir
import com.Meditation.Sounds.frequencies.lemeor.scalarFolder
import com.Meditation.Sounds.frequencies.lemeor.tools.player.ScalarPlayerService
import com.Meditation.Sounds.frequencies.lemeor.ui.main.NavigationActivity
import com.Meditation.Sounds.frequencies.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class NewScalarFragment : BaseFragment() {
    private lateinit var mViewModel: NewScalarViewModel
    override fun initLayout(): Int = R.layout.fragment_new_rife

    override fun initComponents() {
        init()
    }

    override fun addListener() {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.isFocusableInTouchMode = true
        view.requestFocus()
    }

    fun init() {
        mViewModel = ViewModelProvider(
            this, ViewModelFactory(
                ApiHelper(RetrofitBuilder(requireContext()).apiService),
                DataBase.getInstance(requireContext())
            )
        )[NewScalarViewModel::class.java]
    }

    private fun playAndDownload(scalar: Scalar) {
        if (Utils.isConnectedToNetwork(requireContext())) {
            CoroutineScope(Dispatchers.IO).launch {
                val file = File(getSaveDir(requireContext(), scalar.silent_url, scalarFolder))
                val preloaded =
                    File(getPreloadedSaveDir(requireContext(), scalar.silent_url, scalarFolder))
                if (!file.exists() && !preloaded.exists()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                requireActivity(), Manifest.permission.READ_MEDIA_IMAGES
                            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                                requireActivity(), Manifest.permission.READ_MEDIA_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                                requireActivity(), Manifest.permission.READ_MEDIA_VIDEO
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            ScalarDownloadService.startService(context = requireContext(), scalar)
                        } else {
                            ActivityCompat.requestPermissions(
                                requireActivity(), arrayOf(
                                    Manifest.permission.READ_MEDIA_IMAGES,
                                    Manifest.permission.READ_MEDIA_AUDIO,
                                    Manifest.permission.READ_MEDIA_VIDEO
                                ), 1001
                            )
                        }
                    } else {
                        if (ContextCompat.checkSelfPermission(
                                requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            ScalarDownloadService.startService(context = requireContext(), scalar)
                        } else {
                            ActivityCompat.requestPermissions(
                                requireActivity(),
                                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                1001
                            )
                        }
                    }
                }
                scalar.play()
            }
        } else {
            Toast.makeText(
                requireContext(), getString(R.string.err_network_available), Toast.LENGTH_SHORT
            ).show()
        }
    }
    fun Scalar.play() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val playIntent = Intent(context, ScalarPlayerService::class.java).apply {
                    action = "ADD"
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    requireActivity().startForegroundService(playIntent)
                } else {
                    requireActivity().startService(playIntent)
                }
            } catch (_: Exception) {
            }
//            CoroutineScope(Dispatchers.Main).launch { activity.showPlayerUI() }
        }
    }
}