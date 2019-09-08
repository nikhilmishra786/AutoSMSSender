package com.infomantri.autosms.send.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.infomantri.autosms.send.R
import com.infomantri.autosms.send.database.Message
import java.text.SimpleDateFormat
import java.util.*

class MessageListAdapter(copiedText: (String, String, Int) -> Unit) :
    ListAdapter<Message, MessageListAdapter.MsgViewHolder>(DIFF_UTIL) {

    val mCopiedText = copiedText

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

    inner class MsgViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val msgBodyItemView: TextView = itemView.findViewById(R.id.tvMsgBody)
        val categoryItemView: TextView = itemView.findViewById(R.id.tvCategoryValue)
        val sendMsgStatusItemView: TextView = itemView.findViewById(R.id.tvSendMsgStatus)
        val timeStampItemView: TextView = itemView.findViewById(R.id.tvTimeStamp)
        val onLongClicked = itemView.setOnLongClickListener {

            mCopiedText(msgBodyItemView.text.toString(), msgBodyItemView.text.toString(), -1)
            Log.v("Message_Copied", ">>> msg copied: ${msgBodyItemView.text}")
            return@setOnLongClickListener true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MsgViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_item, parent, false)
        return MsgViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MsgViewHolder, position: Int) {
        val current = getItem(position)
        holder.msgBodyItemView.text = current.message
        holder.timeStampItemView.text =
            SimpleDateFormat("dd-MMM-yyyy", Locale.US).format(current.timeStamp)

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

    fun removeAt(position: Int) {
        mCopiedText(getItem(position).message, getItem(position).message, getItem(position).id)
    }

}