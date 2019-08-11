package com.infomantri.autosms.sender.adapter

import android.content.res.Resources
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.infomantri.autosms.sender.R
import com.infomantri.autosms.sender.database.Message
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class MessageListAdapter : ListAdapter<Message, MessageListAdapter.WordViewHolder>(DIFF_UTIL) {

    companion object {
        val DIFF_UTIL = object : DiffUtil.ItemCallback<Message>() {
            override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
                return oldItem == newItem
            }

        }
    }

    inner class WordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val msgBodyItemView: TextView = itemView.findViewById(R.id.tvMsgBody)
        val categoryItemView: TextView = itemView.findViewById(R.id.tvCategoryValue)
        val sendMsgStatusItemView: TextView = itemView.findViewById(R.id.tvSendMsgStatus)
        val timeStampItemView: TextView = itemView.findViewById(R.id.tvTimeStamp)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.recyclerview_item, parent, false)
        return WordViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        val current = getItem(position)
        holder.msgBodyItemView.text = current.message
        holder.timeStampItemView.text = SimpleDateFormat("dd-MMM-yyyy", Locale.US).format(current.timeStamp)

        when {

            current.sent -> {
                holder.sendMsgStatusItemView.text = "sent"
                holder.sendMsgStatusItemView.setTextColor(Color.parseColor("#81C784"))
            }

            current.sent.not() -> {
                holder.sendMsgStatusItemView.text = "pending"
                holder.sendMsgStatusItemView.setTextColor(Color.parseColor("#FF7043"))
            }

            current.isFailed -> {
                holder.sendMsgStatusItemView.text = "failed"
                holder.sendMsgStatusItemView.setTextColor(Color.parseColor("#EF5350"))
            }
        }

        }

}