package com.infomantri.autosms.send.adapter

import android.graphics.Color
import android.graphics.ColorFilter
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.infomantri.autosms.send.R
import com.infomantri.autosms.send.constants.AppConstant.Color.colorAsset
import com.infomantri.autosms.send.database.AddAlarm
import com.infomantri.autosms.send.database.PhoneCallAlarm
import java.text.SimpleDateFormat
import java.util.*

class PhoneCallListAdapter(repeatAlarm: (Boolean, Long, Int) -> Unit, deleteAlarm: (Int) -> Unit) :
    ListAdapter<PhoneCallAlarm, PhoneCallListAdapter.PhoneCallViewHolder>(DIFF_UTIL) {

    val mRepeatAlarm = repeatAlarm
    val mDeleteAlarm = deleteAlarm
//    val colorAsset = arrayOf(
//        R.color.dozeModeColor0,
//        R.color.dozeModeColor1,
//        R.color.dozeModeColor2,
//        R.color.dozeModeColor3,
//        R.color.dozeModeColor4,
//        R.color.dozeModeColor5,
//        R.color.dozeModeColor6,
//        R.color.dozeModeColor7,
//        R.color.dozeModeColor8,
//        R.color.dozeModeColor9,
//        R.color.dozeModeColor10,
//        R.color.dozeModeColor11,
//        R.color.dozeModeColor12,
//        R.color.dozeModeColor13,
//        R.color.dozeModeColor14,
//        R.color.dozeModeColor15,
//        R.color.dozeModeColor16
//    )

    companion object {
        val DIFF_UTIL = object : DiffUtil.ItemCallback<PhoneCallAlarm>() {
            override fun areItemsTheSame(
                oldItem: PhoneCallAlarm,
                newItem: PhoneCallAlarm
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: PhoneCallAlarm,
                newItem: PhoneCallAlarm
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class PhoneCallViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val alarmItemView: TextView = itemView.findViewById(R.id.tvAlarmTime)
        var alarmSwithItemView: Switch = itemView.findViewById(R.id.swRepeatAlarm)
        val alarmStatus: TextView = itemView.findViewById(R.id.tvAlarmStatus)

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PhoneCallViewHolder {

        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_add_alarm_item, parent, false)
        return PhoneCallViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PhoneCallViewHolder, position: Int) {

        val current = getItem(position)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = current.alarmTimeStamp

        holder.itemView.setBackgroundColor(Color.parseColor(colorAsset[(colorAsset.indices).random()]))
        holder.alarmItemView.text = current.alarmTimeStamp.formatDate()
        holder.alarmSwithItemView.isSelected = current.repeatAlarm
        holder.alarmStatus.text = DateUtils.getRelativeTimeSpanString(calendar.timeInMillis)

        if (DateUtils.getRelativeTimeSpanString(current.alarmTimeStamp).contains("ago")) {
            holder.alarmStatus.setTextColor(Color.parseColor("#E53935"))
        } else {
            holder.alarmStatus.setTextColor(Color.parseColor("#43A047"))
        }

        when (calendar.get(Calendar.HOUR_OF_DAY)) {
            in 0..7 -> holder.itemView.setBackgroundResource(R.drawable.ic_border_bg)
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

