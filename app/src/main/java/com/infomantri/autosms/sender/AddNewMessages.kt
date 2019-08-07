package com.infomantri.autosms.sender

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.infomantri.autosms.sender.adapter.MessageListAdapter
import com.infomantri.autosms.sender.constants.AppConstants
import com.infomantri.autosms.sender.database.Message
import com.infomantri.autosms.sender.receiver.TimerReceiver
import com.infomantri.autosms.sender.viewmodel.MessageViewModel
import kotlinx.android.synthetic.main.activity_new_message.*
import java.lang.Exception
import java.lang.reflect.Array.set
import java.text.SimpleDateFormat
import java.util.*

class AddNewMessages: AppCompatActivity() {

    private lateinit var mViewModel : MessageViewModel
    private val mSelectedDate by lazy {
        Calendar.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        mViewModel = ViewModelProviders.of(this).get(MessageViewModel::class.java)
        setOnClickListner()
    }

    private fun setOnClickListner() {

        tvTimePicker.setOnClickListener { showTimePicker() }
        btnSetAlarm.setOnClickListener { setAlarm() }
        btnSavePhoneNo.setOnClickListener {
            if(etEnterPhoneNo.text.isNullOrEmpty().not() && etEnterMsg.text.isNullOrEmpty().not()) sendSMS()
            else Toast.makeText(this,"Enter Phone No.", Toast.LENGTH_SHORT).show() }
        btnSaveMsg.setOnClickListener {
            val msg = etEnterMsg.text.toString()
            if(msg.isNullOrEmpty().not())
                mViewModel.insert(Message(msg))
        }
    }

    fun sendSMS() {
        try{
            val smsManager = SmsManager.getDefault() as SmsManager
            smsManager.sendTextMessage(etEnterPhoneNo.text.toString(),null,etEnterMsg.text.toString(),null,null)
//            Toast.makeText(this@AddNewMessages, "SMS Sent Successfully", Toast.LENGTH_SHORT).show()
        }
        catch (e: Exception){
//            Toast.makeText(this@AddNewMessages, "SMS Failed to Send, Please try again...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showTimePicker() {

        Calendar.getInstance().apply {
            val timeFragment = TimerFragment
                    .instance(get(Calendar.HOUR_OF_DAY), get(Calendar.MINUTE))

            timeFragment.setTimeChangeListener(mOnTimeChangeListener)
            timeFragment.show(supportFragmentManager, "time")
        }
    }

    private val mOnTimeChangeListener = object : TimerFragment.OnTimeSelected {
        override fun selectedTime(hour: Int, minute: Int, unit: String) {
            mSelectedDate.apply {
                set(Calendar.HOUR_OF_DAY, if (unit == "PM" && hour == 0) hour + 12 else hour)
                set(Calendar.MINUTE, minute)
            }
            tvTimePicker.text = mSelectedDate.formatDate()
        }
    }

    private fun setAlarm() {

        val notifyIntent = Intent(this, TimerReceiver::class.java).apply {
            putExtra(AppConstants.REMINDER_TIMESTAMP, mSelectedDate.timeInMillis)
            putExtra(AppConstants.REMINDER_ID, 9999)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            9999,
            notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                mSelectedDate.timeInMillis,
                24 * 60 * 60 * 1000,
                pendingIntent
        )
        Toast.makeText(this@AddNewMessages,"Alarm is set for: \n${mSelectedDate.formatDate()}",Toast.LENGTH_SHORT).show()
    }

    fun Calendar.formatDate(): String? {
        val simpleDateFormatter = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.US)
        return simpleDateFormatter.format(time)
    }

}