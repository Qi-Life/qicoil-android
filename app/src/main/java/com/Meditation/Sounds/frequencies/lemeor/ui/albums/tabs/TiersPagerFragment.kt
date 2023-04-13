package com.Meditation.Sounds.frequencies.lemeor.ui.albums.tabs

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.data.api.RetrofitBuilder
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.model.Tier
import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper
import com.Meditation.Sounds.frequencies.lemeor.data.utils.ViewModelFactory
import com.Meditation.Sounds.frequencies.lemeor.tierPosition
import com.Meditation.Sounds.frequencies.lemeor.ui.main.NavigationActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import kotlinx.android.synthetic.main.fragment_albums_pager.*

class TiersPagerFragment : Fragment() {

    interface OnTiersFragmentListener {
        fun onRefreshTiers()
    }

    private var mListener: OnTiersFragmentListener? = null
    private lateinit var mViewModel: AlbumsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_albums_pager, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initUI()
    }

    override fun onResume() {
        super.onResume()

        mListener?.onRefreshTiers()
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
        ).get(AlbumsViewModel::class.java)

        mViewModel.tiers?.observe(viewLifecycleOwner) {
            val tiersPagerAdapter = TiersPagerAdapter(
                activity as NavigationActivity,
                childFragmentManager,
                it as ArrayList<Tier>
            )
            tiers_view_pager.adapter = tiersPagerAdapter

            tiers_view_pager.setCurrentItem(tierPosition, true)
            tiers_tabs.setupWithViewPager(tiers_view_pager)
        }

        tiers_tabs.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) { tierPosition = tab.position }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }
}