package com.infomantri.autosms.send.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.infomantri.autosms.send.R
import com.infomantri.autosms.send.database.AddAlarm
import com.infomantri.autosms.send.database.Message
import java.text.SimpleDateFormat
import java.util.*

class AddAlarmsListAdapter :
    ListAdapter<AddAlarm, AddAlarmsListAdapter.AddAlarmsViewHolder>(DIFF_UTIL) {

    companion object {
        val DIFF_UTIL = object : DiffUtil.ItemCallback<AddAlarm>() {
            override fun areItemsTheSame(oldItem: AddAlarm, newItem: AddAlarm): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: AddAlarm, newItem: AddAlarm): Boolean {
                return oldItem == newItem
            }

        }
    }

    inner class AddAlarmsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val alarmItemView: TextView = itemView.findViewById(R.id.tvAlarmTime)
        var repeatAlarmItemView: View = itemView.findViewById(R.id.swRepeatAlarm)


    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AddAlarmsViewHolder {

        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_add_alarm_item, parent, false)
        return AddAlarmsViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AddAlarmsViewHolder, position: Int) {

        val current = getItem(position)
        holder.alarmItemView.text =
            SimpleDateFormat("dd-MMM-yyyy", Locale.US).format(current.alarmTimeStamp)
        holder.repeatAlarmItemView.isSelected = current.repeatAlarm


    }


}