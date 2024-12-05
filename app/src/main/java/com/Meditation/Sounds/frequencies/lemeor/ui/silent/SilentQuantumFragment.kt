package com.Meditation.Sounds.frequencies.lemeor.ui.silent

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.feature.base.BaseFragment
import com.Meditation.Sounds.frequencies.lemeor.data.api.RetrofitBuilder
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.model.Scalar
import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper
import com.Meditation.Sounds.frequencies.lemeor.data.utils.Resource
import com.Meditation.Sounds.frequencies.lemeor.data.utils.ViewModelFactory
import com.Meditation.Sounds.frequencies.lemeor.getPreloadedSaveDir
import com.Meditation.Sounds.frequencies.lemeor.getSaveDir
import com.Meditation.Sounds.frequencies.lemeor.playScalar
import com.Meditation.Sounds.frequencies.lemeor.tools.player.ScalarPlayerStatus
import com.Meditation.Sounds.frequencies.lemeor.tools.player.SilentQuantumPlayerService
import com.Meditation.Sounds.frequencies.lemeor.ui.main.NavigationActivity
import com.Meditation.Sounds.frequencies.lemeor.ui.purchase.new_flow.PurchaseScalarWebView
import com.Meditation.Sounds.frequencies.utils.Constants
import com.Meditation.Sounds.frequencies.utils.PlayerUtils
import com.Meditation.Sounds.frequencies.utils.Utils
import com.Meditation.Sounds.frequencies.views.ItemOffsetDecoration
import com.tonyodev.fetch2core.isNetworkAvailable
import kotlinx.android.synthetic.main.fragment_silent_quantum.rcvSilentQuantum
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File

class SilentQuantumFragment : BaseFragment() {
    private lateinit var mViewModel: SilentQuantumViewModel
    private var scalarAlbumsAdapter: SilentQuantumAlbumsAdapter? = null

    val type: String by lazy {
        arguments?.getString(ARG_TYPE)
            ?: Constants.TYPE_SILENT_QT
    }

    override fun initLayout(): Int = R.layout.fragment_silent_quantum

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
        )[SilentQuantumViewModel::class.java]

        initAdapter()

        mViewModel.apply {
            getScalarList().observe(viewLifecycleOwner) { listScalar ->
                when (type) {
                    Constants.TYPE_SILENT_QT -> {
                        scalarAlbumsAdapter?.setData(listScalar as ArrayList<Scalar>)
                    }
                    Constants.TYPE_SILENT_QT_PRO -> {
                        scalarAlbumsAdapter?.setData(arrayListOf())
                    }
                    else -> {
                        scalarAlbumsAdapter?.setData(arrayListOf())
                    }
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: ScalarPlayerStatus) {
        scalarAlbumsAdapter?.notifyDataSetChanged()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            rcvSilentQuantum.layoutManager = GridLayoutManager(context, 4)
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            rcvSilentQuantum.layoutManager = GridLayoutManager(context, 2)
        }
    }

    private fun initAdapter() {
        scalarAlbumsAdapter = SilentQuantumAlbumsAdapter(
            requireContext(), arrayListOf(),
        )
        rcvSilentQuantum.adapter = scalarAlbumsAdapter
        scalarAlbumsAdapter?.setOnClickListener(object : SilentQuantumAlbumsAdapter.Listener {
            override fun onClickItem(album: Scalar) {
                PlayerUtils.checkSchedulePlaying(requireContext()) {
                    if (!it) {
                        playScalar = album
                        playAndDownload(album)
                    } else {
                        EventBus.getDefault().post("clear player")
                        Handler().postDelayed({
                            playScalar = album
                            playAndDownload(album)
                        }, 500L)
                    }
                }
            }

            override fun onLongClickItem(album: Scalar) {

            }

            override fun onScalarSubscription() {
                if (requireActivity().isNetworkAvailable()) {
                    mViewModel.getScalarSubscription().observe(viewLifecycleOwner) { sub ->
                        sub?.let { resource ->
                            when (resource.status) {
                                Resource.Status.SUCCESS -> {
                                    sub.data?.let { u ->
                                        startActivity(
                                            PurchaseScalarWebView.newIntent(
                                                requireContext(), u.data.payment_url
                                            )
                                        )
                                    }

                                }

                                Resource.Status.ERROR -> {
                                }

                                Resource.Status.LOADING -> {
                                }
                            }
                        }
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.err_network_available),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })

        rcvSilentQuantum.setHasFixedSize(true)
        if (activity?.resources?.configuration?.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            rcvSilentQuantum.layoutManager = GridLayoutManager(context, 4)
        } else if (activity?.resources?.configuration?.orientation == Configuration.ORIENTATION_PORTRAIT) {
            rcvSilentQuantum.layoutManager = GridLayoutManager(context, 2)
        }
        val itemDecoration = ItemOffsetDecoration(
            requireContext(),
            if (Utils.isTablet(requireContext())) R.dimen.margin_buttons else R.dimen.item_offset
        )
        rcvSilentQuantum.addItemDecoration(itemDecoration)
    }

    private fun playAndDownload(scalar: Scalar) {
        if (Utils.isConnectedToNetwork(requireContext())) {
            CoroutineScope(Dispatchers.IO).launch {
                val file =
                    File(getSaveDir(requireContext(), scalar.audio_file, scalar.audio_folder))
                val preloaded = File(
                    getPreloadedSaveDir(
                        requireContext(), scalar.audio_file, scalar.audio_folder
                    )
                )
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
                            SilentQuantumDownloadService.startService(
                                context = requireContext(), scalar
                            )
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
                            SilentQuantumDownloadService.startService(
                                context = requireContext(), scalar
                            )
                        } else {
                            ActivityCompat.requestPermissions(
                                requireActivity(),
                                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                1001
                            )
                        }
                    }
                }
            }
        } else {
            Toast.makeText(
                requireContext(), getString(R.string.err_network_available), Toast.LENGTH_SHORT
            ).show()
        }
        playStopScalar()
    }

    private fun playStopScalar() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val playIntent = Intent(context, SilentQuantumPlayerService::class.java).apply {
                    action = "ADD_REMOVE"
                }
                requireActivity().startService(playIntent)
            } catch (_: Exception) {
            }
            CoroutineScope(Dispatchers.Main).launch { (activity as NavigationActivity).showPlayerUI() }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    companion object {
        const val ARG_TYPE = "arg_type"

        @JvmStatic
        fun newInstance(type: String = Constants.TYPE_SILENT_QT): SilentQuantumFragment {
            return SilentQuantumFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TYPE, type)
                }
            }
        }
    }
}