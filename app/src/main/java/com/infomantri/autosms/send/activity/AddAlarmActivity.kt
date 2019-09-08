package com.infomantri.autosms.send.activity

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.infomantri.autosms.send.R
import com.infomantri.autosms.send.TimerFragment
import com.infomantri.autosms.send.asynctask.BaseAsyncTask
import com.infomantri.autosms.send.base.BaseActivity
import com.infomantri.autosms.send.database.MessageDbRepository
import com.infomantri.autosms.send.database.MessageRoomDatabase
import com.infomantri.autosms.send.receiver.AlarmReceiver
import kotlinx.android.synthetic.main.activity_add_alarm.*
import java.text.SimpleDateFormat
import java.util.*

class AddAlarmActivity : BaseActivity() {

    var isMorningAlarm = true
    val requestCodeMorning = 6666
    val requestCodeNight = 8888

    private val morningAlarm by lazy {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 5)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND,0)
        }
    }

    private val nightAlarm by lazy {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 29)
            set(Calendar.SECOND,0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_alarm)

        setOnClickListner()
        tvMorningAlarm.text = morningAlarm.formatDate()
        tvNightAlarm.text = nightAlarm.formatDate()
    }

    private fun setOnClickListner() {


        val fab: View = findViewById(R.id.fabSaveAlarm)
        fab.setOnClickListener {
            setAlarm(morningAlarm, requestCodeMorning, "Good Morning .....")
            setAlarm(nightAlarm, requestCodeNight, "Good Night .....")
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        btnSetMorningAlarm.setOnClickListener {
            showTimePicker(true)
        }
        btnSetNightAlarm.setOnClickListener {
            showTimePicker(false)
        }
    }

    private fun showTimePicker(isMorning: Boolean) {

        isMorningAlarm = isMorning
        Calendar.getInstance().apply {
            val timeFragment = TimerFragment.instance(get(Calendar.HOUR_OF_DAY), get(Calendar.MINUTE))

            timeFragment.setTimeChangeListener(mOnTimeChangeListener)
            timeFragment.show(supportFragmentManager, "time")
        }
    }

    private val mOnTimeChangeListener = object : TimerFragment.OnTimeSelected {
        override fun selectedTime(hour: Int, minute: Int, unit: String) {

            when{
                isMorningAlarm -> {
                    morningAlarm.apply {
                        set(Calendar.HOUR_OF_DAY, if (unit == "PM") hour + 12 else hour)
                        set(Calendar.MINUTE, minute)
                    }
                    tvMorningAlarm.text = morningAlarm.formatDate()
                }
                else -> {
                    nightAlarm.apply {
                        set(Calendar.HOUR_OF_DAY, if (unit == "PM") hour + 12 else hour)
                        set(Calendar.MINUTE, minute)
                    }
                    tvNightAlarm.text = nightAlarm.formatDate()
                }
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