package com.Meditation.Sounds.frequencies.lemeor.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.feature.base.BaseFragment
import com.Meditation.Sounds.frequencies.lemeor.data.api.RetrofitBuilder
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper
import com.Meditation.Sounds.frequencies.lemeor.data.utils.ViewModelFactory
import com.Meditation.Sounds.frequencies.lemeor.ui.main.HomeViewModel
import com.Meditation.Sounds.frequencies.lemeor.ui.main.NavigationActivity
import com.Meditation.Sounds.frequencies.utils.SharedPreferenceHelper
import com.hieupt.android.standalonescrollbar.attachTo
import kotlinx.android.synthetic.main.fragment_home.rcAlbumRecent
import kotlinx.android.synthetic.main.fragment_home.scrollbar
import kotlinx.android.synthetic.main.fragment_home.tvNoDataRecent

class HomeFragment : BaseFragment() {
    private lateinit var mViewModel: HomeViewModel
    private var recentAlbumsAdapter: RecentAlbumsAdapter? = null

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

        scrollbar.attachTo(rcAlbumRecent)

//        rcAlbumRecent.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//                val offset = recyclerView.computeHorizontalScrollOffset()
//                val extent = recyclerView.computeHorizontalScrollExtent()
//                val range = recyclerView.computeHorizontalScrollRange()
//                if (range <= extent) {
//                    scrollbarViewThumb.translationX = 0f
//                } else {
//                    val proportion = offset.toFloat() / (range - extent)
//                    val scrollbarWidth = scrollbarViewBg.width - scrollbarViewThumb.width
//                    val translationX = proportion * scrollbarWidth
//                    scrollbarViewThumb.translationX = translationX
//                }
//            }
//        })
//
//        var initialX = 0f
//        var initialThumbPosition = 0f
//
//        scrollbarViewThumb.setOnTouchListener { v, event ->
//            when (event.action) {
//                MotionEvent.ACTION_DOWN -> {
//                    initialX = event.rawX
//                    initialThumbPosition = scrollbarViewThumb.x
//                    true
//                }
//                MotionEvent.ACTION_MOVE -> {
//                    val delta = event.rawX - initialX
//                    var newX = initialThumbPosition + delta
//
//                    // Limit thumb movement within the background bounds
//                    val maxX = scrollbarViewBg.width - scrollbarViewThumb.width
//                    newX = newX.coerceIn(0f, maxX.toFloat())
//
//                    // Update thumb position
//                    scrollbarViewThumb.x = newX
//
//                    // Calculate scroll position for RecyclerView
//                    val scrollPercentage = newX / maxX
//                    val totalScroll = rcAlbumRecent.computeHorizontalScrollRange()
//                    val scrollTo = (totalScroll * scrollPercentage).toInt()
//
//                    // Smooth scroll RecyclerView
//
//                    Log.e("DKMMMMM", "DKMMMMMMM$scrollTo")
//
//                    rcAlbumRecent.smoothScrollBy(scrollTo - rcAlbumRecent.computeHorizontalScrollOffset(), 0)
//
//                    true
//                }
//                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
//                    // Khi người dùng bỏ tay ra khỏi thumb
//                    true
//                }
//                else -> false
//            }
//        }

        updateView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.isFocusableInTouchMode = true
        view.requestFocus()
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
}