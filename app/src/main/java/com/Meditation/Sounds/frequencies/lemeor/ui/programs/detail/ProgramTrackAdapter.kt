package com.Meditation.Sounds.frequencies.lemeor.ui.programs.detail

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.data.model.Program
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
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_program_track.view.divider
import kotlinx.android.synthetic.main.item_program_track.view.image_lock
import kotlinx.android.synthetic.main.item_program_track.view.item_album_name
import kotlinx.android.synthetic.main.item_program_track.view.item_track_image
import kotlinx.android.synthetic.main.item_program_track.view.item_track_name
import kotlinx.android.synthetic.main.item_program_track.view.item_track_options
import kotlinx.android.synthetic.main.item_program_track.view.item_track_scalar_status

class ProgramTrackAdapter(
    private val onClickItem: (item: Search, index: Int) -> Unit,
    private val onClickSubscriptionItem: (item: Search) -> Unit,
    private val onClickOptions: (item: Search) -> Unit
) : ListAdapter<Search, ProgramTrackAdapter.ViewHolder>(SearchDiffCallback()) {
    private var selectedItem: Search? = null
    private var program: Program? = null
    var isMy = false
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_program_track, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ProgramTrackAdapter.ViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
        fun bind(item: Search, position: Int) {
            if (isMy) {
                itemView.item_track_options.visibility = View.VISIBLE
            } else {
                itemView.item_track_options.visibility = View.GONE
            }
            if (position == itemCount - 1) {
                itemView.divider.visibility = View.INVISIBLE
            } else {
                itemView.divider.visibility = View.VISIBLE
            }
            itemView.updateView(item, position)
            itemView.item_track_options.setOnClickListener {
                onClickOptions.invoke(item)
            }
            itemView.setOnClickListener {
                if (item.obj !is Scalar) {
                    setSelectedItem(item)
                }
                if ((item.obj is Scalar && SharedPreferenceHelper.getInstance()
                        .getBool(PREF_SETTING_ADVANCE_SCALAR_ON_OFF) && (item.obj as Scalar).is_free == 1) || item.obj !is Scalar
                ) {
                    onClickItem.invoke(item, layoutPosition)
                } else if ((item.obj as Scalar).is_free == 0) {
                    onClickSubscriptionItem.invoke(item)
                }
            }
        }
    }

    private fun View.updateView(item: Search, position: Int) {
        when (item.obj) {
            is Track -> {
                val t = item.obj as Track
                t.isSelected = position == selectedItem?.id
                updateUIForTrack(t)
            }

            is MusicRepository.Frequency -> {
                val f = item.obj as MusicRepository.Frequency
                f.isSelected = position == selectedItem?.id
                updateUIForFrequency(f)
            }

            is Scalar -> {
                val f = item.obj as Scalar
                updateUIForScalar(f)
            }
        }
    }

    private fun View.updateUIForTrack(track: Track) {
        image_lock.visibility = View.GONE
        item_track_scalar_status.visibility = View.GONE
        item_track_name.setTextColor(
            ContextCompat.getColor(
                context, if (track.isSelected) R.color.colorPrimary else android.R.color.white
            )
        )
        item_album_name.setTextColor(
            ContextCompat.getColor(
                context, if (track.isSelected) R.color.colorPrimary else android.R.color.white
            )
        )
        item_track_name.text = track.name
        track.album?.let { album ->
            loadImage(context, item_track_image, album)
            item_album_name.text = album.name
        }
    }

    private fun View.updateUIForFrequency(frequency: MusicRepository.Frequency) {
        image_lock.visibility = View.GONE
        item_track_scalar_status.visibility = View.GONE
        item_track_name.setTextColor(
            ContextCompat.getColor(
                context, if (frequency.isSelected) R.color.colorPrimary else android.R.color.white
            )
        )
        item_album_name.setTextColor(
            ContextCompat.getColor(
                context, if (frequency.isSelected) R.color.colorPrimary else android.R.color.white
            )
        )
        item_track_image.setImageResource(R.drawable.frequency_v2)
        item_track_name.text = context.getString(R.string.navigation_lbl_rife)
        item_album_name.text = frequency.frequency.toString()
    }

    private fun View.updateUIForScalar(scalar: Scalar) {
        val isSilentEnable =
            SharedPreferenceHelper.getInstance().getBool(PREF_SETTING_ADVANCE_SCALAR_ON_OFF)
        item_track_name.setTextColor(
            ContextCompat.getColor(
                context,
                if (!isSilentEnable || scalar.is_free == 0) R.color.item_selected else android.R.color.white
            )
        )
        item_album_name.setTextColor(
            ContextCompat.getColor(
                context,
                if (!isSilentEnable || scalar.is_free == 0) R.color.item_selected else android.R.color.white
            )
        )
        if (isSilentEnable || scalar.is_free == 0) {
            image_lock.visibility = View.GONE
        } else {
            image_lock.visibility = View.VISIBLE
        }
        loadImageScalar(context, item_track_image, scalar)
        if (scalar.silent_energy_tier == SilentQuantumType.PRO.value) {
            item_track_name.text = context.getString(R.string.navigation_lbl_silent_quantum_pro)
        } else {
            item_track_name.text = context.getString(R.string.navigation_lbl_silent_quantum)
        }

        item_album_name.text = scalar.name

        if (playListScalar.contains(scalar) && playProgramId == program?.id && isSilentEnable && scalar.is_free == 1) {
            Glide.with(context)
                .asGif()
                .load(R.drawable.ic_scalar_playing)
                .into(item_track_scalar_status)
            item_track_scalar_status.visibility = View.VISIBLE
        } else {
            item_track_scalar_status.visibility = View.GONE
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setProgram(pr: Program) {
        program = pr
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setSelectedItem(item: Search?) {
        selectedItem = item
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitData(list: List<Search>) {
        submitList(list)
        notifyDataSetChanged()
    }

    private class SearchDiffCallback : DiffUtil.ItemCallback<Search>() {
        override fun areItemsTheSame(oldItem: Search, newItem: Search): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Search, newItem: Search): Boolean {
            return oldItem == newItem
        }
    }
}