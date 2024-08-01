package com.Meditation.Sounds.frequencies.feature.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.feature.chat.MessageAdapter.MyViewHolder
import kotlinx.android.synthetic.main.chat_item.view.tvLeftChat
import kotlinx.android.synthetic.main.chat_item.view.tvRightChat
import kotlinx.android.synthetic.main.chat_item.view.viewLeftChat
import kotlinx.android.synthetic.main.chat_item.view.viewRightChat

class MessageAdapter(private var messageList: List<MessageChat>) : Adapter<MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val chatView = LayoutInflater.from(parent.context).inflate(R.layout.chat_item, null)
        return MyViewHolder(chatView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val message = messageList[position]
        if (message.sentBy == MessageChat.SEND_BY_ME) {
            holder.itemView.viewLeftChat.visibility = View.GONE
            holder.itemView.viewRightChat.visibility = View.VISIBLE
            holder.itemView.tvRightChat.text = message.message
        } else {
            holder.itemView.viewRightChat.visibility = View.GONE
            holder.itemView.viewLeftChat.visibility = View.VISIBLE
            holder.itemView.tvLeftChat.text = message.message
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    inner class MyViewHolder(itemView: View) : ViewHolder(itemView) {

    }
}