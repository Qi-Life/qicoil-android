package com.Meditation.Sounds.frequencies.lemeor.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album
import com.Meditation.Sounds.frequencies.lemeor.data.model.Scalar
import com.Meditation.Sounds.frequencies.lemeor.data.model.Search
import com.Meditation.Sounds.frequencies.lemeor.data.model.Track
import com.Meditation.Sounds.frequencies.lemeor.loadImage
import com.Meditation.Sounds.frequencies.lemeor.loadImageScalar
import com.Meditation.Sounds.frequencies.lemeor.playListScalar
import com.Meditation.Sounds.frequencies.lemeor.playProgramId
import com.Meditation.Sounds.frequencies.lemeor.tools.player.MusicRepository
import com.Meditation.Sounds.frequencies.models.SilentQuantumType
import com.Meditation.Sounds.frequencies.utils.Constants.Companion.PREF_SETTING_ADVANCE_SCALAR_ON_OFF
import com.Meditation.Sounds.frequencies.utils.SharedPreferenceHelper
import com.Meditation.Sounds.frequencies.utils.extensions.shouldShow
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.my_frequencies_item.view.image
import kotlinx.android.synthetic.main.my_frequencies_item.view.image_lock
import kotlinx.android.synthetic.main.my_frequencies_item.view.tvName
import kotlinx.android.synthetic.main.my_frequencies_item.view.tvTitle
import kotlinx.android.synthetic.main.my_frequencies_item.view.view_album_info

class FavoritesAdapter(
    private val mContext: Context, private val onClickItem: (Search) -> Unit
) : ListAdapter<Search, FavoritesAdapter.ViewHolder>(TrackDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): FavoritesAdapter.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.my_frequencies_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: FavoritesAdapter.ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        init {
            itemView.setOnClickListener {
                onClickItem.invoke(getItem(layoutPosition))
            }
        }

        @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
        fun bind(item: Search) {
            when (item.obj) {
                is Track -> {
                    val track = (item.obj as Track)
                    itemView.view_album_info.visibility = View.VISIBLE
                    itemView.image.radius =
                        mContext.resources.getDimensionPixelOffset(R.dimen.radius_recent_albums)
                    itemView.image_lock.shouldShow(!track.isUnlocked)
                    track.album?.let { loadImage(mContext, itemView.image, it) }
                    itemView.tvTitle.text = track.album?.name
                    itemView.tvName.text = track.name
                }

                is MusicRepository.Frequency -> {
                    val frequency = item.obj as MusicRepository.Frequency

                    itemView.image.setImageResource(R.drawable.frequency_v2)
                    itemView.tvTitle.text = itemView.context.getString(R.string.navigation_lbl_rife)
                    itemView.tvName.text = frequency.frequency.toString()
                }

                is Scalar -> {
                    val scalar = item.obj as Scalar

                    val isSilentEnable =
                        SharedPreferenceHelper.getInstance().getBool(
                            PREF_SETTING_ADVANCE_SCALAR_ON_OFF
                        )
                    itemView.tvName.setTextColor(
                        ContextCompat.getColor(
                            itemView.context,
                            if (!isSilentEnable || scalar.is_free == 0) R.color.item_selected else android.R.color.white
                        )
                    )
                    itemView.tvTitle.setTextColor(
                        ContextCompat.getColor(
                            itemView.context,
                            if (!isSilentEnable || scalar.is_free == 0) R.color.item_selected else android.R.color.white
                        )
                    )
                    if (isSilentEnable || scalar.is_free == 0) {
                        itemView.image_lock.visibility = View.GONE
                    } else {
                        itemView.image_lock.visibility = View.VISIBLE
                    }
                    loadImageScalar(itemView.context, itemView.image, scalar)
                    if (scalar.silent_energy_tier == SilentQuantumType.PRO.value) {
                        itemView.tvName.text = itemView.context.getString(R.string.navigation_lbl_silent_quantum_pro)
                    } else {
                        itemView.tvName.text = itemView.context.getString(R.string.navigation_lbl_silent_quantum)
                    }

                    itemView.tvName.text = scalar.name

                    if (playListScalar.contains(scalar) && playProgramId == scalar.id.toInt() && isSilentEnable && scalar.is_free == 1) {
                        Glide.with(itemView.context)
                            .asGif()
                            .load(R.drawable.ic_scalar_playing)
                            .into(itemView.image)
                    } else {

                    }
                }

            }

        }
    }

    class TrackDiffCallback : DiffUtil.ItemCallback<Search>() {
        override fun areItemsTheSame(oldItem: Search, newItem: Search): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Search, newItem: Search): Boolean {
            return oldItem == newItem
        }
    }
}