package com.Meditation.Sounds.frequencies.lemeor.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album
import com.Meditation.Sounds.frequencies.utils.SharedPreferenceHelper
import com.hieupt.android.standalonescrollbar.attachTo
import kotlinx.android.synthetic.main.home_my_frequency_item.view.rcMyFrequencies
import kotlinx.android.synthetic.main.home_recent_item.view.rcAlbumRecent
import kotlinx.android.synthetic.main.home_recent_item.view.scrollbar
import kotlinx.android.synthetic.main.home_recent_item.view.tvNoDataRecent

class HomeAdapter(val onClickItem: (Album) -> Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var albumsData = arrayListOf<Album>()

    @SuppressLint("NotifyDataSetChanged")
    fun setData(albums: ArrayList<Album>) {
        albumsData = albums
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.home_recent_item, parent, false)
                RecentViewHolder(view, onClickItem)
            }

            1 -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.home_my_frequency_item, parent, false)
                MyFrequencyViewHolder(view, onClickItem)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is RecentViewHolder -> holder.bind()
            is MyFrequencyViewHolder -> holder.bind(albumsData)
        }
    }

    override fun getItemCount(): Int = 2

    class RecentViewHolder(itemView: View, val onClickItem: (Album) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        fun bind() {
            val recentAlbumsAdapter = RecentAlbumsAdapter(itemView.context) {
                onClickItem.invoke(it)
            }
            itemView.rcAlbumRecent.adapter = recentAlbumsAdapter
            recentAlbumsAdapter.setData(SharedPreferenceHelper.getInstance().recentAlbums)
            itemView.scrollbar.attachTo(itemView.rcAlbumRecent)

            if (SharedPreferenceHelper.getInstance().recentAlbums.isNotEmpty()) {
                itemView.rcAlbumRecent.visibility = View.VISIBLE
                itemView.scrollbar.visibility = View.VISIBLE
                itemView.tvNoDataRecent.visibility = View.GONE
            } else {
                itemView.rcAlbumRecent.visibility = View.GONE
                itemView.scrollbar.visibility = View.GONE
                itemView.tvNoDataRecent.visibility = View.VISIBLE
            }
        }
    }

    class MyFrequencyViewHolder(itemView: View, val onClickItem: (Album) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        fun bind(list: List<Album>) {
            val myFrequenciesAdapter = MyFrequenciesAdapter(itemView.context) {
                onClickItem.invoke(it)
            }
            clearAllItemDecorations(itemView.rcMyFrequencies)
            itemView.rcMyFrequencies.layoutManager = layoutManagerGrid
            itemView.rcMyFrequencies.addItemDecoration(itemDecoration)
            itemView.rcMyFrequencies.adapter = myFrequenciesAdapter
            myFrequenciesAdapter.setData(list as ArrayList<Album>)
        }

        private fun clearAllItemDecorations(recyclerView: RecyclerView) {
            val itemDecorations = recyclerView.itemDecorationCount
            for (i in 0 until itemDecorations) {
                recyclerView.removeItemDecorationAt(0)
            }
        }
    }

    fun changeLayoutManager(context: Context, isPortrait: Boolean, spacing: Int) {
        if (isPortrait) {
            itemDecoration = GridSpacingItemDecoration(5, spacing, true)
            layoutManagerGrid = GridLayoutManager(context, 5)
        } else {
            itemDecoration = GridSpacingItemDecoration(8, spacing, true)
            layoutManagerGrid = GridLayoutManager(context, 8)
        }
    }

    companion object {
        lateinit var itemDecoration: GridSpacingItemDecoration
        lateinit var layoutManagerGrid: GridLayoutManager
    }
}