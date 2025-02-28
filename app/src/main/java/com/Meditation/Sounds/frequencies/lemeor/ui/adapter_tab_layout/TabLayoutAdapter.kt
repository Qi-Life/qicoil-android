package com.Meditation.Sounds.frequencies.lemeor.ui.adapter_tab_layout

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.ui.albums.tabs.CategoriesPagerAdapter
import com.Meditation.Sounds.frequencies.lemeor.ui.albums.tabs.TiersPagerAdapter

class TabLayoutAdapter : RecyclerView.Adapter<TabLayoutAdapter.ViewHolder>() {

    private var viewPager: ViewPager? = null
    private var onItemClick: ((Int) -> Unit)? = null
    private var positionSelected: Int? = null

    @SuppressLint("NotifyDataSetChanged")
    fun setUpWithViewPager(viewPager: ViewPager) {
        this.viewPager = viewPager

        if (positionSelected == null) {
            positionSelected = viewPager.currentItem
        } else {
            viewPager.setCurrentItem(positionSelected!!, false)
        }

        onItemClick = {
            positionSelected = it
            viewPager.setCurrentItem(it)
            notifyDataSetChanged()
        }
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                positionSelected = position
                notifyDataSetChanged()
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tab_layout_custom, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        when (viewPager?.adapter) {
            is TiersPagerAdapter -> {
              return viewPager?.adapter?.count ?: 0
            }

            is CategoriesPagerAdapter -> {
                return viewPager?.adapter?.count ?: 0
            }
            null -> return 0
        }
        throw Exception("getItemCount error")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        return holder.bind()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val root: View = itemView.findViewById(R.id.root)
        private val image: View = itemView.findViewById(R.id.image)

        init {
            itemView.setOnClickListener {
                onItemClick?.invoke(layoutPosition)
            }
        }

        fun bind() {

            when (viewPager?.adapter) {
                is TiersPagerAdapter -> {
                    tvTitle.text = viewPager?.adapter?.getPageTitle(layoutPosition) ?: ""
                }

                is CategoriesPagerAdapter -> {
                    tvTitle.text = viewPager?.adapter?.getPageTitle(layoutPosition) ?: ""
                }
            }
            root.isSelected = layoutPosition == positionSelected
            image.isSelected = layoutPosition == positionSelected
        }
    }

}