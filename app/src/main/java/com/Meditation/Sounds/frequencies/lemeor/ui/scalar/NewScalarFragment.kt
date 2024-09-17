package com.Meditation.Sounds.frequencies.lemeor.ui.scalar

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
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
import com.Meditation.Sounds.frequencies.lemeor.tools.player.ScalarPlayerService
import com.Meditation.Sounds.frequencies.lemeor.tools.player.ScalarPlayerStatus
import com.Meditation.Sounds.frequencies.lemeor.ui.main.NavigationActivity
import com.Meditation.Sounds.frequencies.lemeor.ui.purchase.new_flow.PurchaseScalarWebView
import com.Meditation.Sounds.frequencies.utils.Utils
import com.Meditation.Sounds.frequencies.views.ItemOffsetDecoration
import com.tonyodev.fetch2core.isNetworkAvailable
import kotlinx.android.synthetic.main.fragment_new_scalar.rcvScalar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File

class NewScalarFragment : BaseFragment() {
    private lateinit var mViewModel: NewScalarViewModel
    private var scalarAlbumsAdapter: ScalarAlbumsAdapter? = null

    override fun initLayout(): Int = R.layout.fragment_new_scalar

    override fun initComponents() {
        init()
    }

    override fun addListener() {
        mViewModel.apply {
            getScalarList().observe(viewLifecycleOwner) { listScalar ->
                scalarAlbumsAdapter?.setData(listScalar as ArrayList<Scalar>)
            }
        }
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

        initAdapter()
    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: ScalarPlayerStatus) {
       scalarAlbumsAdapter?.notifyDataSetChanged()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            rcvScalar.layoutManager = GridLayoutManager(context, 4)
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            rcvScalar.layoutManager = GridLayoutManager(context, 2)
        }
    }

    private fun initAdapter() {
        scalarAlbumsAdapter = ScalarAlbumsAdapter(
            requireContext(), arrayListOf(),
        )
        rcvScalar.adapter = scalarAlbumsAdapter
        scalarAlbumsAdapter?.setOnClickListener(object : ScalarAlbumsAdapter.Listener {
            override fun onClickItem(album: Scalar) {
                playScalar = album
                playAndDownload(album)
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
                                            PurchaseScalarWebView.newIntent(requireContext(),u.data.payment_url)
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
                    Toast.makeText(requireContext(), getString(R.string.err_network_available), Toast.LENGTH_SHORT).show()
                }
            }
        })

        rcvScalar.setHasFixedSize(true)
        if (activity?.resources?.configuration?.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            rcvScalar.layoutManager = GridLayoutManager(context, 4)
        } else if (activity?.resources?.configuration?.orientation == Configuration.ORIENTATION_PORTRAIT) {
            rcvScalar.layoutManager = GridLayoutManager(context, 2)
        }
        val itemDecoration = ItemOffsetDecoration(requireContext(),
            if (Utils.isTablet(requireContext())) R.dimen.margin_buttons else R.dimen.item_offset)
        rcvScalar.addItemDecoration(itemDecoration)
    }

    private fun playAndDownload(scalar: Scalar) {
        if (Utils.isConnectedToNetwork(requireContext())) {
            CoroutineScope(Dispatchers.IO).launch {
                val file = File(getSaveDir(requireContext(), scalar.audio_file, scalar.audio_folder))
                val preloaded =
                    File(getPreloadedSaveDir(requireContext(), scalar.audio_file, scalar.audio_folder))
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
            }
        } else {
            Toast.makeText(
                requireContext(), getString(R.string.err_network_available), Toast.LENGTH_SHORT
            ).show()
        }
        playStopScalar("ADD_REMOVE")
    }

    private fun playStopScalar(actionScalar: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val playIntent = Intent(context, ScalarPlayerService::class.java).apply {
                    action = actionScalar
                }
                requireActivity().startService(playIntent)
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    requireActivity().startForegroundService(playIntent)
//                } else {
//
//                }
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
}