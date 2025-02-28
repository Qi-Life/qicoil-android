package com.Meditation.Sounds.frequencies.lemeor.ui.albums.tabs

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.feature.base.BaseFragment
import com.Meditation.Sounds.frequencies.lemeor.data.api.RetrofitBuilder
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.model.Tier
import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper
import com.Meditation.Sounds.frequencies.lemeor.data.utils.ViewModelFactory
import com.Meditation.Sounds.frequencies.lemeor.tierPosition
import com.Meditation.Sounds.frequencies.lemeor.tierPositionSelected
import com.Meditation.Sounds.frequencies.lemeor.ui.adapter_tab_layout.TabLayoutAdapter
import com.Meditation.Sounds.frequencies.lemeor.ui.main.NavigationActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import kotlinx.android.synthetic.main.fragment_albums_pager.*

class TiersPagerFragment : BaseFragment() {
    private var tiersPagerAdapter: TiersPagerAdapter? = null

    private val tabAdapter by lazy { TabLayoutAdapter() }

    interface OnTiersFragmentListener {
        fun onRefreshTiers()
    }

    private var mListener: OnTiersFragmentListener? = null
    private lateinit var mViewModel: AlbumsViewModel

    override fun onResume() {
        super.onResume()
        mListener?.onRefreshTiers()
    }

    override fun initLayout(): Int = R.layout.fragment_albums_pager

    override fun initComponents() {
        initUI()
    }

    override fun addListener() {
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = if (context is OnTiersFragmentListener) { context }
        else {
            throw RuntimeException(context.toString()
                    + " must implement OnSignInFragmentListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    private fun initUI() {
        mViewModel = ViewModelProvider(this,
            ViewModelFactory(
                ApiHelper(RetrofitBuilder(requireContext()).apiService),
                DataBase.getInstance(requireContext()))
        )[AlbumsViewModel::class.java]

        tiersPagerAdapter = TiersPagerAdapter(
            activity as NavigationActivity,
            childFragmentManager
        )
        tiers_view_pager.adapter = tiersPagerAdapter
        rvTabLayout.adapter = tabAdapter
//        tiers_tabs.setupWithViewPager(tiers_view_pager)

        mViewModel.tiers?.observe(viewLifecycleOwner) {
            tiersPagerAdapter?.setData(it as ArrayList<Tier>)
            tabAdapter.setUpWithViewPager(tiers_view_pager)
        }
    }
}