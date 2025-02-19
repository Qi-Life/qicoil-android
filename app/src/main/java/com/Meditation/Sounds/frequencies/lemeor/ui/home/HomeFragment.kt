package com.Meditation.Sounds.frequencies.lemeor.ui.home

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
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
import com.Meditation.Sounds.frequencies.lemeor.ui.programs.NewProgramViewModel
import com.Meditation.Sounds.frequencies.lemeor.ui.programs.detail.ProgramDetailViewModel
import kotlinx.android.synthetic.main.fragment_home.rvHome

class HomeFragment : BaseFragment() {
    private val homeAdapter: HomeAdapter by lazy {
        HomeAdapter {
            (requireActivity() as NavigationActivity).onAlbumDetails(it)
        }
    }
    private var allAlbums = arrayListOf<Album>()

    private val homeViewModel by lazy {
        ViewModelProvider(
            this, ViewModelFactory(
                ApiHelper(RetrofitBuilder(requireContext()).apiService),
                DataBase.getInstance(requireContext())
            )
        )[HomeViewModel::class.java]
    }

    override fun initLayout(): Int = R.layout.fragment_home

    @SuppressLint("ClickableViewAccessibility")
    override fun initComponents() {

        if (activity?.resources?.configuration?.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            homeAdapter?.changeLayoutManager(requireContext(), false, dpToPx(16))
        } else if (activity?.resources?.configuration?.orientation == Configuration.ORIENTATION_PORTRAIT) {
            homeAdapter?.changeLayoutManager(requireContext(), true, dpToPx(16))
        }

        rvHome.adapter = homeAdapter

        adjustDataForFullRows(arrayListOf())
        homeViewModel.get48AlbumUnlockedLiveData().observeOnce(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                homeAdapter?.setData(it as ArrayList<Album>)
            }
        }
        homeViewModel.getAlbumsUnlockedLiveData().observe(viewLifecycleOwner) {
            allAlbums.clear()
            allAlbums.addAll(it)
            adjustDataForFullRows(allAlbums)
        }

    }

    private fun adjustDataForFullRows(albums: ArrayList<Album>) {
        val isLandscape =
            activity?.resources?.configuration?.orientation == Configuration.ORIENTATION_LANDSCAPE

        val columns = if (isLandscape) 8 else 5

        val albumList = mutableListOf<Album>()

        albumList.addAll(albums)

        val totalItems = albumList.size
        val remainingItems = totalItems % columns
        if (remainingItems != 0) {
            val emptyItemsToAdd = columns - remainingItems
            for (i in 0 until emptyItemsToAdd) {
                albumList.add(
                    Album(
                        id = -1,
                        audio_folder = "",
                        unlock_url = "",
                        benefits_text = ""
                    )
                )
            }
        }
        if (columns == 8) {
            for (i in 1..8) {
                albumList.add(
                    Album(
                        id = -1,
                        audio_folder = "",
                        unlock_url = "",
                        benefits_text = ""
                    )
                )
            }
        } else {
            for (i in 1..5) {
                albumList.add(
                    Album(
                        id = -1,
                        audio_folder = "",
                        unlock_url = "",
                        benefits_text = ""
                    )
                )
            }
        }

        homeAdapter?.setData(albumList as ArrayList<Album>)
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
            homeAdapter.changeLayoutManager(requireContext(), false, dpToPx(16))
        } else if (activity?.resources?.configuration?.orientation == Configuration.ORIENTATION_PORTRAIT) {
            homeAdapter.changeLayoutManager(requireContext(), true, dpToPx(16))
        }
        adjustDataForFullRows(allAlbums)
    }

    override fun addListener() {

    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
}