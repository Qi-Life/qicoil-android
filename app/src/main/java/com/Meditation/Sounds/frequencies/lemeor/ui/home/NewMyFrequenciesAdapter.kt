package com.Meditation.Sounds.frequencies.lemeor.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album
import com.Meditation.Sounds.frequencies.lemeor.loadImage
import kotlinx.android.synthetic.main.my_frequencies_item.view.image
import kotlinx.android.synthetic.main.my_frequencies_item.view.image_lock
import kotlinx.android.synthetic.main.my_frequencies_item.view.tvName
import kotlinx.android.synthetic.main.my_frequencies_item.view.tvTitle
import kotlinx.android.synthetic.main.my_frequencies_item.view.view_album_info

class NewMyFrequenciesAdapter(
    private val mContext: Context, private val onClickItem: (Album) -> Unit
) : ListAdapter<Album, NewMyFrequenciesAdapter.ViewHolder>(TrackDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): NewMyFrequenciesAdapter.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.my_frequencies_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: NewMyFrequenciesAdapter.ViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
        fun bind(album: Album, position: Int) {
//            itemView.view_album_info.visibility = View.GONE
            if (album.id != -1) {
                itemView.view_album_info.visibility = View.VISIBLE
                itemView.image.radius =
                    mContext.resources.getDimensionPixelOffset(R.dimen.radius_recent_albums)
                if (album.isUnlocked) {
                    itemView.image_lock.visibility = View.GONE
                } else {
                    itemView.image_lock.visibility = View.VISIBLE
                }
                loadImage(mContext, itemView.image, album)

                itemView.setOnClickListener {
                    onClickItem.invoke(album)
                }
            }

//            itemView.tvTitle.text = album.name
            itemView.tvName.text = album.name
        }
    }


    private class TrackDiffCallback : DiffUtil.ItemCallback<Album>() {
        override fun areItemsTheSame(oldItem: Album, newItem: Album): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Album, newItem: Album): Boolean {
            return oldItem == newItem
        }
    }
}