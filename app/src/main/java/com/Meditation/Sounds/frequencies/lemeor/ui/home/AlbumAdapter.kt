package com.Meditation.Sounds.frequencies.lemeor.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album
import com.Meditation.Sounds.frequencies.lemeor.loadImage
import kotlinx.android.synthetic.main.my_frequencies_item.view.image
import kotlinx.android.synthetic.main.my_frequencies_item.view.image_lock

class AlbumAdapter : PagingDataAdapter<Album, AlbumAdapter.AlbumViewHolder>(AlbumComparator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        return AlbumViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.my_frequencies_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        val album = getItem(position)
        album?.let { holder.bind(it) }
    }

    class AlbumViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(album: Album) {
            itemView.image.radius = itemView.context.resources.getDimensionPixelOffset(R.dimen.radius_recent_albums)
            if (album.isUnlocked) {
                itemView.image_lock.visibility = View.GONE
            } else {
                itemView.image_lock.visibility = View.VISIBLE
            }
            loadImage(itemView.context, itemView.image, album)

            itemView.setOnClickListener {
//                onClickItem.invoke(album)
            }
        }
    }

    companion object {
        val AlbumComparator = object : DiffUtil.ItemCallback<Album>() {
            override fun areItemsTheSame(oldItem: Album, newItem: Album): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Album, newItem: Album): Boolean {
                return oldItem == newItem
            }
        }
    }
}