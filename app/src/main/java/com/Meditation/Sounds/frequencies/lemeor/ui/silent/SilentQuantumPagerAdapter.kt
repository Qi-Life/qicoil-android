package com.Meditation.Sounds.frequencies.lemeor.ui.silent

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.Meditation.Sounds.frequencies.lemeor.ui.main.NavigationActivity
import com.Meditation.Sounds.frequencies.utils.Constants

class SilentQuantumPagerAdapter(
    private val activity: NavigationActivity, fm: FragmentManager
) : FragmentPagerAdapter(fm) {

    private val tiersList: ArrayList<String> = arrayListOf()

    override fun getItem(position: Int): Fragment {
        if (position == 0) {
            return SilentQuantumFragment.newInstance(type = Constants.TYPE_SILENT_QT)
        }
        return SilentQuantumFragment.newInstance(type = Constants.TYPE_SILENT_QT_PRO)
    }

    override fun getPageTitle(position: Int): CharSequence {
        return tiersList[position]
    }

    override fun getCount(): Int {
        return tiersList.size
    }

    fun setData(data: ArrayList<String>) {
        tiersList.clear()
        tiersList.addAll(data)
        notifyDataSetChanged()
    }
}