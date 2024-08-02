package com.Meditation.Sounds.frequencies.feature.chatbot

import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.feature.chatbot.MessageChatBotAdapter.MyViewHolder
import com.bumptech.glide.Glide
import java.util.regex.Pattern
import kotlinx.android.synthetic.main.chat_item.view.imvTyping
import kotlinx.android.synthetic.main.chat_item.view.tvLeftChat
import kotlinx.android.synthetic.main.chat_item.view.tvRightChat
import kotlinx.android.synthetic.main.chat_item.view.viewLeftChat
import kotlinx.android.synthetic.main.chat_item.view.viewRightChat


class MessageChatBotAdapter(private var messageList: List<MessageChatBot>,
                            private val onAlbumClick:(String) -> Unit,
                            private val onUpdateTextTyping:() -> Unit) : Adapter<MyViewHolder>() {
    var isTextAnimation = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val chatView = LayoutInflater.from(parent.context).inflate(R.layout.chat_item, null)
        return MyViewHolder(chatView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val message = messageList[position]

        Glide.with(holder.itemView.context)
            .asGif()
            .load(R.drawable.ic_typing_texting)
            .into(holder.itemView.imvTyping)

        if (message.sentBy == MessageChatBot.SEND_BY_ME) {
            holder.itemView.viewLeftChat.visibility = View.GONE
            holder.itemView.viewRightChat.visibility = View.VISIBLE
            holder.itemView.tvRightChat.text = getSpanText(message.message)
            holder.itemView.tvRightChat.movementMethod = LinkMovementMethod.getInstance()
        } else {
            holder.itemView.viewRightChat.visibility = View.GONE
            holder.itemView.viewLeftChat.visibility = View.VISIBLE
            if (!isTextAnimation && position == messageList.size - 1) {
                isTextAnimation = true
                holder.itemView.imvTyping.visibility = View.GONE
                holder.itemView.tvLeftChat.visibility = View.VISIBLE
                holder.itemView.tvLeftChat.animateText(getSpanText(message.message))
                holder.itemView.tvLeftChat.setOnTypeWriterCompleteListener {
                    onUpdateTextTyping.invoke()
                }
            } else {
                holder.itemView.imvTyping.visibility = View.GONE
                holder.itemView.tvLeftChat.visibility = View.GONE
                if (message.message == "Typing") {
                    if (position == messageList.size - 1) {
                        holder.itemView.imvTyping.visibility = View.VISIBLE
                    } else {
                        holder.itemView.tvLeftChat.visibility = View.VISIBLE
                        holder.itemView.tvLeftChat.text = "Error!"
                    }
                } else {
                    holder.itemView.tvLeftChat.visibility = View.VISIBLE
                    holder.itemView.tvLeftChat.text = getSpanText(message.message)
                }
            }

            holder.itemView.tvLeftChat.movementMethod = LinkMovementMethod.getInstance()
        }
    }

    private fun getSpanText(msg: String): SpannableStringBuilder {
        val pattern = "##\\w+\\s*\\w*\\s*\\w*\\s*\\w*"
        val regex = Pattern.compile(pattern)
        val matcher = regex.matcher(msg)
        val listAlbum = arrayListOf<String>()
        while (matcher.find()) {
            val matchedText = matcher.group()
            val indexOfNewLine: Int = matchedText.indexOf('\n')
            listAlbum.add(matchedText.substring(0, indexOfNewLine).replace("##", ""))
        }

        val fullText = msg.replace("##", "")
        val spannableStringBuilder = SpannableStringBuilder(fullText)

        for (text in listAlbum) {
            var startIndex: Int = fullText.indexOf(text)
            while (startIndex != -1) {
                val endIndex = startIndex + text.length

                val clickableSpan: ClickableSpan = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        onAlbumClick.invoke(text)
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        super.updateDrawState(ds)
                        ds.isUnderlineText = false
                    }
                }
                val bss = StyleSpan(Typeface.BOLD)
                spannableStringBuilder.setSpan(bss, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                spannableStringBuilder.setSpan(clickableSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                spannableStringBuilder.setSpan(ForegroundColorSpan(Color.parseColor("#3F51B5")), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                startIndex = fullText.indexOf(text, startIndex + text.length)
            }
        }

        return spannableStringBuilder
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    inner class MyViewHolder(itemView: View) : ViewHolder(itemView) {

    }
}