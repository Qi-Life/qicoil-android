package com.Meditation.Sounds.frequencies.lemeor.ui.home

import GridSpacingItemDecoration2
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.HORIZONTAL
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album
import com.Meditation.Sounds.frequencies.lemeor.data.model.Search
import com.Meditation.Sounds.frequencies.utils.SharedPreferenceHelper
import com.Meditation.Sounds.frequencies.utils.extensions.dpToPx
import com.hieupt.android.standalonescrollbar.StandaloneScrollBar
import com.hieupt.android.standalonescrollbar.attachTo
import kotlinx.android.synthetic.main.home_my_favorites.view.rcvFavorites
import kotlinx.android.synthetic.main.home_my_frequency_item.view.rcMyFrequencies
import kotlinx.android.synthetic.main.home_recent_item.view.rcAlbumRecent
import kotlinx.android.synthetic.main.home_recent_item.view.scrollbar
import kotlinx.android.synthetic.main.home_recent_item.view.tvNoDataRecent

class HomeAdapter(val onClickItem: (Album) -> Unit, val onClickFavorites: (Search) -> Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var albumsData = arrayListOf<Album>()


    @SuppressLint("NotifyDataSetChanged")
    var favoritesData = listOf<Search>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }


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

            2 -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.home_my_frequency_item, parent, false)
                MyFrequencyViewHolder(view, onClickItem)
            }

            1 -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.home_my_favorites, parent, false)
                FavoriteViewHolder(view, onClickFavorites)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is RecentViewHolder -> holder.bind()
            is MyFrequencyViewHolder -> holder.bind(albumsData)
            is FavoriteViewHolder -> holder.bind(favoritesData)
        }
    }

    override fun getItemCount(): Int = 3

    val recentAlbumsAdapter = RecentAlbumsAdapter() {
        onClickItem.invoke(it)
    }

    inner class RecentViewHolder(itemView: View, val onClickItem: (Album) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        fun bind() {
            if (itemView.rcAlbumRecent.adapter == null) {
                itemView.rcAlbumRecent.adapter = recentAlbumsAdapter
            }

            val recentAlbums = SharedPreferenceHelper.getInstance().recentAlbums
            recentAlbumsAdapter.setData(recentAlbums)
            var spanCount = 1
            if (recentAlbums.size > 4) {
                spanCount = 2
            }
            itemView.rcAlbumRecent.layoutManager =
                GridLayoutManager(itemView.context, spanCount, HORIZONTAL, false)
            itemView.scrollbar.attachTo(itemView.rcAlbumRecent)

            if (SharedPreferenceHelper.getInstance().recentAlbums.isNotEmpty()) {
                itemView.rcAlbumRecent.visibility = View.VISIBLE
//                itemView.scrollbar.visibility = View.VISIBLE
                itemView.tvNoDataRecent.visibility = View.GONE
            } else {
                itemView.rcAlbumRecent.visibility = View.GONE
//                itemView.scrollbar.visibility = View.GONE
                itemView.tvNoDataRecent.visibility = View.VISIBLE
            }
        }
    }


    inner class MyFrequencyViewHolder(itemView: View, val onClickItem: (Album) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        fun bind(list: List<Album>) {
            val myFrequenciesAdapter = NewMyFrequenciesAdapter {
                onClickItem.invoke(it)
            }
            itemView.rcMyFrequencies.adapter = myFrequenciesAdapter

            clearAllItemDecorations(itemView.rcMyFrequencies)
            itemView.rcMyFrequencies.layoutManager = layoutManagerGrid
            itemView.rcMyFrequencies.addItemDecoration(itemDecoration)
            myFrequenciesAdapter.submitList(list)
        }
    }

    inner class FavoriteViewHolder(itemView: View, val onClickItem: (Search) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        fun bind(list: List<Search>) {
            val favoritesAdapter = FavoritesAdapter(itemView.context) {
                onClickItem.invoke(it)
            }
//            itemView.rcvFavorites.layoutManager = layoutManagerGrid
            clearAllItemDecorations(itemView.rcvFavorites)
            itemView.rcvFavorites.layoutManager =
                GridLayoutManager(itemView.context, 1, HORIZONTAL, false)
            itemView.rcvFavorites.adapter = favoritesAdapter
            itemView.rcvFavorites.addItemDecoration(
                GridSpacingItemDecoration2(
                    1,
                    itemView.context.dpToPx(16),
                    false
                )
            )
            favoritesAdapter.submitList(list)
        }

    }

    fun clearAllItemDecorations(recyclerView: RecyclerView) {
        val itemDecorations = recyclerView.itemDecorationCount
        for (i in 0 until itemDecorations) {
            recyclerView.removeItemDecorationAt(0)
        }
    }

    fun changeLayoutManager(context: Context, isPortrait: Boolean, spacing: Int) {
        if (isPortrait) {
            itemDecoration = GridSpacingItemDecoration2(1, spacing, false)
            layoutManagerGrid = GridLayoutManager(context, 1, HORIZONTAL, false)
        } else {
            itemDecoration = GridSpacingItemDecoration2(1, spacing, false)
            layoutManagerGrid = GridLayoutManager(context, 1, HORIZONTAL, false)
        }
    }

    companion object {
        lateinit var itemDecoration: RecyclerView.ItemDecoration
        lateinit var layoutManagerGrid: GridLayoutManager
    }
}