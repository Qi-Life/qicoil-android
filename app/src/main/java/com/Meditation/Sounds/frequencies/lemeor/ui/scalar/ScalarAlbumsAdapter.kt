package com.Meditation.Sounds.frequencies.lemeor.ui.scalar

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.data.model.Scalar
import com.Meditation.Sounds.frequencies.lemeor.loadImageScalar
import com.Meditation.Sounds.frequencies.lemeor.playListScalar
import kotlinx.android.synthetic.main.scalar_album_item.view.image
import kotlinx.android.synthetic.main.scalar_album_item.view.image_lock
import kotlinx.android.synthetic.main.scalar_album_item.view.lock
import kotlinx.android.synthetic.main.scalar_album_item.view.viewPlayingAnimation

class ScalarAlbumsAdapter(
    private val mContext: Context,
    private var mData: ArrayList<Scalar>,
) : RecyclerView.Adapter<ScalarAlbumsAdapter.ViewHolder>() {

    interface Listener {
        fun onClickItem(album: Scalar)
        fun onLongClickItem(album: Scalar)
        fun onScalarSubscription()
    }

    private var mListener: Listener? = null

    fun setOnClickListener(listener: Listener) {
        mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.scalar_album_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val scalar = mData[position]

        holder.itemView.image.radius =
            mContext.resources!!.getDimensionPixelOffset(R.dimen.corner_radius_album)

        if (scalar.is_free == 1) {
            holder.itemView.image_lock.visibility = View.GONE
        } else {
            holder.itemView.image_lock.visibility = View.VISIBLE
            holder.itemView.lock.visibility = View.GONE
        }

        if (playListScalar.contains(scalar) && scalar.is_free == 1) {
            holder.itemView.viewPlayingAnimation.startAnimation()
        } else {
            holder.itemView.viewPlayingAnimation.clearAnimation()
        }

        loadImageScalar(mContext, holder.itemView.image, scalar)

        holder.itemView.setOnClickListener {
            if (scalar.is_free == 1) {
                mListener?.onClickItem(scalar)
            }
        }
        holder.itemView.setOnLongClickListener {
            mListener?.onLongClickItem(scalar)
            true
        }
        holder.itemView.image_lock.setOnClickListener {
            mListener?.onScalarSubscription()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    @SuppressLint("NotifyDataSetChanged")
    fun setData(categoryList: ArrayList<Scalar>) {
        categoryList.sortBy { it.order_number }
        mData = categoryList
        notifyDataSetChanged()
    }
}