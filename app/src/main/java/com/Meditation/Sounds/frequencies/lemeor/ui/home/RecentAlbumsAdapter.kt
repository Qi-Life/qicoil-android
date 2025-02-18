package com.Meditation.Sounds.frequencies.lemeor.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album
import com.Meditation.Sounds.frequencies.lemeor.loadImage
import kotlinx.android.synthetic.main.recent_album_item.view.image
import kotlinx.android.synthetic.main.recent_album_item.view.image_lock
import kotlinx.android.synthetic.main.recent_album_item.view.title

class RecentAlbumsAdapter(
    private val mContext: Context,
    private val onClickItem:(Album) -> Unit
) : RecyclerView.Adapter<RecentAlbumsAdapter.ViewHolder>() {
    private var mData: ArrayList<Album> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.recent_album_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val album = mData[position]
//        holder.itemView.image.radius = mContext.resources!!.getDimensionPixelOffset(R.dimen.radius_recent_albums)
        if (album.isUnlocked) {
            holder.itemView.image_lock.visibility = View.GONE
        } else {
            holder.itemView.image_lock.visibility = View.VISIBLE
        }
        holder.itemView.title.text = album.name
        loadImage(mContext, holder.itemView.image, album)

        holder.itemView.setOnClickListener { onClickItem.invoke(album) }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    @SuppressLint("NotifyDataSetChanged")
    fun setData(albums: ArrayList<Album>) {
        mData = albums
        notifyDataSetChanged()
    }
}

