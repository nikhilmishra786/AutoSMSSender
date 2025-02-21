package com.infomantri.autosms.send.adapter

import android.graphics.Color
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.infomantri.autosms.send.R
import com.infomantri.autosms.send.database.AddAlarm
import com.infomantri.autosms.send.R.layout.recyclerview_add_alarm_item
import java.text.SimpleDateFormat
import java.util.*

class AddAlarmsListAdapter(repeatAlarm: (Boolean, Long, Int) -> Unit, deleteAlarm: (Int) -> Unit) :
    ListAdapter<AddAlarm, AddAlarmsListAdapter.AddAlarmsViewHolder>(DIFF_UTIL) {

    val mRepeatAlarm = repeatAlarm
    val mDeleteAlarm = deleteAlarm

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
        var alarmSwithItemView: Switch = itemView.findViewById(R.id.swRepeatAlarm)
        val alarmStatus: TextView = itemView.findViewById(R.id.tvAlarmStatus)

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
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = current.alarmTimeStamp

        holder.alarmItemView.text = current.alarmTimeStamp.formatDate()
        holder.alarmSwithItemView.isSelected = current.repeatAlarm
        holder.alarmStatus.text = DateUtils.getRelativeTimeSpanString(calendar.timeInMillis)

        if (DateUtils.getRelativeTimeSpanString(current.alarmTimeStamp).contains("ago")) {
            holder.alarmStatus.setTextColor(Color.parseColor("#E53935"))
        } else {
            holder.alarmStatus.setTextColor(Color.parseColor("#43A047"))
        }

        holder.alarmSwithItemView.setOnCheckedChangeListener { buttonView, isChecked ->
            mRepeatAlarm(isChecked, current.alarmTimeStamp, current.id)
        }

        Log.v(
            "ALARM_STATUS",
            ">>> ${DateUtils.getRelativeTimeSpanString(calendar.timeInMillis)} Time: ${calendar.timeInMillis.formatDate()} repeat: ${current.repeatAlarm}"
        )
    }

    fun removeAt(position: Int) {
        mDeleteAlarm(getItem(position).id)
    }

    fun Long.formatDate(): String? {
        val simpleDateFormatter = SimpleDateFormat("hh:mm a", Locale.US)
        return simpleDateFormatter.format(this)
    }

    fun Calendar.formatDate(): String? {
        val simpleDateFormatter = SimpleDateFormat("hh:mm a", Locale.US)
        return simpleDateFormatter.format(time)
    }

}