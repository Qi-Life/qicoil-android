package com.Meditation.Sounds.frequencies.lemeor.ui.main

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.data.model.Program
import kotlinx.android.synthetic.main.item_program_tab.view.item_track_name

class NavigatorProgramsAdapter(val onClickItem: (Program?) -> Unit) :
    RecyclerView.Adapter<NavigatorProgramsAdapter.ViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    var data = listOf<Program>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    @SuppressLint("NotifyDataSetChanged")
    var isCollapse = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_program_tab, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data.getOrNull(position))
    }


    inner class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                onClickItem.invoke(data.getOrNull(layoutPosition))
            }
        }

        fun bind(item: Program?) {
            if (isCollapse) {
                itemView.item_track_name.visibility = View.GONE
            } else {
                itemView.item_track_name.visibility = View.VISIBLE
            }
            itemView.item_track_name.text = item?.name.toString()

        }
    }

}