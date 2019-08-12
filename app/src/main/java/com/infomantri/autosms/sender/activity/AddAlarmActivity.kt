package com.infomantri.autosms.sender.activity

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.infomantri.autosms.sender.R
import com.infomantri.autosms.sender.TimerFragment
import com.infomantri.autosms.sender.base.BaseActivity
import com.infomantri.autosms.sender.constants.AppConstants
import com.infomantri.autosms.sender.receiver.TimerReceiver
import kotlinx.android.synthetic.main.activity_add_alarm.*
import kotlinx.android.synthetic.main.activity_new_message.*
import java.text.SimpleDateFormat
import java.util.*

class AddAlarmActivity : BaseActivity() {

    var isMorningAlarm = true
    val requestCodeMorning = 6666
    val requestCodeNight = 8888

    private val morningAlarm by lazy {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 5)
            set(Calendar.MINUTE, 58)
        }
    }

    private val nightAlarm by lazy {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 28)
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
            setAlarm(morningAlarm, requestCodeMorning)
            setAlarm(nightAlarm, requestCodeNight)
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
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
                        set(Calendar.HOUR_OF_DAY, if (unit == "PM" && hour == 0) hour + 12 else hour)
                        set(Calendar.MINUTE, minute)
                    }
                    tvMorningAlarm.text = morningAlarm.formatDate()
                }
                else -> {
                    nightAlarm.apply {
                        set(Calendar.HOUR_OF_DAY, if (unit == "PM" && hour == 0) hour + 12 else hour)
                        set(Calendar.MINUTE, minute)
                    }
                    tvNightAlarm.text = nightAlarm.formatDate()
                }
            }
        }
    }

    private fun setAlarm(calendar: Calendar, requestCode: Int) {

        val notifyIntent = Intent(this, TimerReceiver::class.java).apply {
            putExtra(AppConstants.REMINDER_TIMESTAMP, calendar.timeInMillis)
            putExtra(AppConstants.REMINDER_ID, 9999)
        }

        val pendingIntent = PendingIntent.getBroadcast(
                this,
                requestCode,
                notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                24 * 60 * 60 * 1000,
                pendingIntent
        )
        Log.v("MORNING_ALARM", ">>> Morning Alarm: ${morningAlarm.formatDate()} \n night: ${nightAlarm.formatDate()}")
    }

    fun Calendar.formatDate(): String? {
        val simpleDateFormatter = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.US)
        return simpleDateFormatter.format(time)
    }

}