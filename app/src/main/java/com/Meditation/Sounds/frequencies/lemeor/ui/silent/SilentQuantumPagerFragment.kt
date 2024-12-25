package com.Meditation.Sounds.frequencies.lemeor.ui.silent

import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.feature.base.BaseFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.main.NavigationActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import kotlinx.android.synthetic.main.fragment_silent_quantum_pager.tiers_tabs
import kotlinx.android.synthetic.main.fragment_silent_quantum_pager.tiers_view_pager

class SilentQuantumPagerFragment : BaseFragment() {
    var tiersPagerAdapter: SilentQuantumPagerAdapter? = null

    override fun initLayout(): Int = R.layout.fragment_silent_quantum_pager

    override fun initComponents() {
        initUI()
    }

    override fun addListener() {
        tiers_tabs.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun initUI() {
        tiersPagerAdapter = SilentQuantumPagerAdapter(
            activity as NavigationActivity, childFragmentManager
        )
        tiers_view_pager.adapter = tiersPagerAdapter
        tiers_tabs.setupWithViewPager(tiers_view_pager)
        val listTitles = arrayListOf(getString(R.string.navigation_lbl_silent_quantum), getString(R.string.navigation_lbl_silent_quantum_pro))
        tiersPagerAdapter?.setData(listTitles)
    }
}