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
import kotlinx.android.synthetic.main.fragment_home.rvHome

class HomeFragment : BaseFragment() {
    private lateinit var mViewModel: HomeViewModel
    private var recentAlbumsAdapter: RecentAlbumsAdapter? = null
    private var myFrequenciesAdapter: NewMyFrequenciesAdapter? = null
    private lateinit var itemDecoration: GridSpacingItemDecoration
    private var homeAdapter: HomeAdapter? = null

    override fun initLayout(): Int = R.layout.fragment_home

    @SuppressLint("ClickableViewAccessibility")
    override fun initComponents() {
        mViewModel = ViewModelProvider(
            this, ViewModelFactory(
                ApiHelper(RetrofitBuilder(requireContext()).apiService),
                DataBase.getInstance(requireContext())
            )
        )[HomeViewModel::class.java]

        homeAdapter = HomeAdapter {
            (requireActivity() as NavigationActivity).onAlbumDetails(it)
        }

        if (activity?.resources?.configuration?.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            homeAdapter?.changeLayoutManager(requireContext(), false , dpToPx(16))
        } else if (activity?.resources?.configuration?.orientation == Configuration.ORIENTATION_PORTRAIT) {
            homeAdapter?.changeLayoutManager(requireContext(), true , dpToPx(16))
        }

        rvHome.adapter = homeAdapter
        mViewModel.get48AlbumUnlockedLiveData().observeOnce(viewLifecycleOwner) {
            homeAdapter?.setData(it as ArrayList<Album>)
        }
        mViewModel.getAlbumsUnlockedLiveData().observe(viewLifecycleOwner) {
            homeAdapter?.setData(it as ArrayList<Album>)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.isFocusableInTouchMode = true
        view.requestFocus()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (activity?.resources?.configuration?.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            homeAdapter?.changeLayoutManager(requireContext(), false , dpToPx(16))
        } else if (activity?.resources?.configuration?.orientation == Configuration.ORIENTATION_PORTRAIT) {
            homeAdapter?.changeLayoutManager(requireContext(), true , dpToPx(16))
        }
        homeAdapter?.notifyDataSetChanged()
    }

    override fun addListener() {

    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
}