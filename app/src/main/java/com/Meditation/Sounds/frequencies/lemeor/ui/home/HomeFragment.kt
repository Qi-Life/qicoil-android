package com.Meditation.Sounds.frequencies.lemeor.ui.home

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.feature.base.BaseFragment
import com.Meditation.Sounds.frequencies.lemeor.data.api.RetrofitBuilder
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album
import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper
import com.Meditation.Sounds.frequencies.lemeor.data.utils.ViewModelFactory
import com.Meditation.Sounds.frequencies.lemeor.observeOnce
import com.Meditation.Sounds.frequencies.lemeor.ui.main.HomeViewModel
import com.Meditation.Sounds.frequencies.lemeor.ui.main.NavigationActivity
import com.Meditation.Sounds.frequencies.utils.SharedPreferenceHelper
import com.Meditation.Sounds.frequencies.utils.loadImageWithGif
import com.hieupt.android.standalonescrollbar.attachTo
import kotlinx.android.synthetic.main.fragment_home.ivImage
import kotlinx.android.synthetic.main.fragment_home.loadingFrame
import kotlinx.android.synthetic.main.fragment_home.rcAlbumRecent
import kotlinx.android.synthetic.main.fragment_home.rcMyFrequencies
import kotlinx.android.synthetic.main.fragment_home.scrollbar
import kotlinx.android.synthetic.main.fragment_home.tvNoDataRecent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeFragment : BaseFragment() {
    private lateinit var mViewModel: HomeViewModel
    private var recentAlbumsAdapter: RecentAlbumsAdapter? = null
    private var myFrequenciesAdapter: NewMyFrequenciesAdapter? = null
    private lateinit var itemDecoration: GridSpacingItemDecoration

    override fun initLayout(): Int = R.layout.fragment_home

    @SuppressLint("ClickableViewAccessibility")
    override fun initComponents() {
        mViewModel = ViewModelProvider(
            this, ViewModelFactory(
                ApiHelper(RetrofitBuilder(requireContext()).apiService),
                DataBase.getInstance(requireContext())
            )
        )[HomeViewModel::class.java]

        recentAlbumsAdapter = RecentAlbumsAdapter(requireContext()) {
            (requireActivity() as NavigationActivity).onAlbumDetails(it)
        }
        rcAlbumRecent.adapter = recentAlbumsAdapter
        recentAlbumsAdapter?.setData(SharedPreferenceHelper.getInstance().recentAlbums)

        if (activity?.resources?.configuration?.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            itemDecoration = GridSpacingItemDecoration(8, dpToPx(16), true)
            rcMyFrequencies.layoutManager = GridLayoutManager(requireContext(), 8)
        } else if (activity?.resources?.configuration?.orientation == Configuration.ORIENTATION_PORTRAIT) {
            itemDecoration = GridSpacingItemDecoration(5, dpToPx(16), true)
            rcMyFrequencies.layoutManager = GridLayoutManager(requireContext(), 5)
        }
        rcMyFrequencies.addItemDecoration(itemDecoration)

        myFrequenciesAdapter = NewMyFrequenciesAdapter(requireContext()) {
            (requireActivity() as NavigationActivity).onAlbumDetails(it)
        }
        rcMyFrequencies.adapter = myFrequenciesAdapter

        scrollbar.attachTo(rcAlbumRecent)

        loadingFrame.visibility = View.VISIBLE
        loadImageWithGif(ivImage, R.raw.loading_grey)

        mViewModel.get48AlbumUnlockedLiveData().observeOnce(viewLifecycleOwner) {
            loadData(it as ArrayList<Album>)
        }

        mViewModel.getAlbumsUnlockedLiveData().observe(viewLifecycleOwner) {
            loadData(it as ArrayList<Album>)
        }
        updateView()
    }

    private fun loadData(data: ArrayList<Album>) {
        loadingFrame.visibility = View.GONE
        myFrequenciesAdapter?.submitList(data)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.isFocusableInTouchMode = true
        view.requestFocus()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        rcMyFrequencies.removeItemDecoration(itemDecoration)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            itemDecoration = GridSpacingItemDecoration(8, dpToPx(16), true)
            rcMyFrequencies.layoutManager = GridLayoutManager(requireContext(), 8)
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            itemDecoration = GridSpacingItemDecoration(5, dpToPx(16), true)
            rcMyFrequencies.layoutManager = GridLayoutManager(requireContext(), 5)
        }
        rcMyFrequencies.addItemDecoration(itemDecoration)
    }

    override fun addListener() {

    }

    private fun updateView() {
        if (SharedPreferenceHelper.getInstance().recentAlbums.isNotEmpty()) {
            rcAlbumRecent.visibility = View.VISIBLE
            scrollbar.visibility = View.VISIBLE
            tvNoDataRecent.visibility = View.GONE
        } else {
            rcAlbumRecent.visibility = View.GONE
            scrollbar.visibility = View.GONE
            tvNoDataRecent.visibility = View.VISIBLE
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
}