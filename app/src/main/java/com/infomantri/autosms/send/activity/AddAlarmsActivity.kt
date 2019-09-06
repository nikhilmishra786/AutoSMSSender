package com.infomantri.autosms.send.activity

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.infomantri.autosms.send.R
import com.infomantri.autosms.send.TimerFragment
import com.infomantri.autosms.send.adapter.AddAlarmsListAdapter
import com.infomantri.autosms.send.base.BaseActivity
import com.infomantri.autosms.send.database.AddAlarm
import com.infomantri.autosms.send.receiver.AlarmReceiver
import kotlinx.android.synthetic.main.activity_add_alarm.*
import kotlinx.android.synthetic.main.activity_add_alarms.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.recyclerview_add_alarm_item.*
import java.text.SimpleDateFormat
import java.util.*

class AddAlarmsActivity : BaseActivity() {

    private var repeatAlarm = false
    val requestCode = 9999

    private val addAlarmCalendar by lazy {
        Calendar.getInstance().apply {
            set(Calendar.SECOND,0)
        }
    }

    private val alarmList by lazy {
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_alarms)

        setRecyclerView()
        setOnClikckListiner()
    }

    private fun setRecyclerView() {
        val adapter = AddAlarmsListAdapter()
        add_alarm_recyclerview.adapter = adapter
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        add_alarm_recyclerview.layoutManager = linearLayoutManager

        adapter.submitList(alarmList)
    }

    private fun setOnClikckListiner() {

//        swRepeatAlarm.setOnCheckedChangeListener { compoundButton, isSelected ->
//            repeatAlarm = isSelected
//        }

        ivAddReminder.setOnClickListener{
            showTimePicker()
        }

    }

    private fun showTimePicker() {


        Calendar.getInstance().apply {
            val timeFragment = TimerFragment.instance(get(Calendar.HOUR_OF_DAY), get(Calendar.MINUTE))

            timeFragment.setTimeChangeListener(mOnTimeChangeListener)
            timeFragment.show(supportFragmentManager, "time")
        }
    }

    private val mOnTimeChangeListener = object : TimerFragment.OnTimeSelected {
        override fun selectedTime(hour: Int, minute: Int, unit: String) {

                    addAlarmCalendar.apply {
                        set(Calendar.HOUR_OF_DAY, if (unit == "PM") hour + 12 else hour)
                        set(Calendar.MINUTE, minute)
                    }
        }
    }

    private fun setAlarm(calendar: Calendar, requestCode: Int, title: String) {

        val notifyIntent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("reminder_timestamp", calendar.timeInMillis)
            putExtra("reminder_id", requestCode)
            putExtra("reminder_title", title)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            requestCode,
            notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if(Date().after(calendar.time)){
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                24 * 60 * 60 * 1000,
                pendingIntent
            )
        }
        else {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                24 * 60 * 60 * 1000,
                pendingIntent
            )
        }
        Log.v("SET_ALARM", ">>> $title: ${calendar.formatDate()}")
    }

    fun Calendar.formatDate(): String? {
        val simpleDateFormatter = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.US)
        return simpleDateFormatter.format(time)
    }

}