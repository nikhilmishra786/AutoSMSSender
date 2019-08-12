package com.infomantri.autosms.sender

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.infomantri.autosms.sender.asynctask.BaseAsyncTask
import com.infomantri.autosms.sender.base.BaseActivity
import com.infomantri.autosms.sender.constants.AppConstants
import com.infomantri.autosms.sender.database.*
import com.infomantri.autosms.sender.receiver.TimerReceiver
import com.infomantri.autosms.sender.viewmodel.MessageViewModel
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.coroutines.awaitAll
import java.text.SimpleDateFormat
import java.util.*

class AddNewMessages : BaseActivity() {

    var DEFAULT_MOBILE_NO = "8424954824"

    private lateinit var mViewModel: MessageViewModel
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

        tvTimePicker.setOnClickListener {
            showTimePicker()
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        btnSetAlarm.setOnClickListener {
            setAlarm()
            getDefaultMobileNo(application)
        }
        btnSavePhoneNo.setOnClickListener {
            if (etEnterPhoneNo.text.isNullOrEmpty().not()) {
                DEFAULT_MOBILE_NO = etEnterPhoneNo.text.toString()
                insertMobileNoInDb()
            }else{
                Toast.makeText(this,"Mobile number cannot be empty...", Toast.LENGTH_SHORT).show()
            }
        }
        btnSaveMsg.setOnClickListener {
            val msg = etEnterMsg.text.toString()
            if (msg.isNullOrEmpty().not())
                mViewModel.insert(Message(msg, System.currentTimeMillis(),false))
            finish()
        }
    }

    fun insertMobileNoInDb() {
        BaseAsyncTask(object : BaseAsyncTask.SendSMSFromDb{
            override fun onStarted() {

                val isDefault = etEnterMsg.text.isNullOrEmpty().not()

                val subscribersDao = MessageRoomDatabase.getDatabase(application).subscribersDao()
                val repository = SubscribersRepository(subscribersDao)
                repository.insertMobileNo(
                    Subscribers(etEnterPhoneNo.text.toString(), isDefault))
            }

            override fun onCompleted() {
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

    }

    fun getDefaultMobileNo(context: Context) {
        BaseAsyncTask(object : BaseAsyncTask.SendSMSFromDb{
            override fun onStarted() {
                val subscribersDao = MessageRoomDatabase.getDatabase(context).subscribersDao()
                val repository = SubscribersRepository(subscribersDao)
                repository.defaultMobileNo.iterator().forEach {subscribers ->
                    Log.v("PRINT_DEFAULT_NO", ">>> Default: ${subscribers.mobileNo}  ${subscribers.isDefault}")
                }
            }

            override fun onCompleted() {
            }
        })
    }

    fun sendSMS(context: Context) {

        BaseAsyncTask(object : BaseAsyncTask.SendSMSFromDb {
            override fun onStarted() {

                val smsManager = SmsManager.getDefault() as SmsManager
                val repository = getFromDatabase(context)
                val allMessages = repository.allMessages
                var i = 0

                try {
                    allMessages.iterator().forEach { msg ->
                        if(!msg.sent && msg.maxLimit <= 2) {
                            smsManager.sendTextMessage(
                                DEFAULT_MOBILE_NO,
                                null,
                                msg.message + " >>> " + msg.id,
                                null,
                                null
                            )
                            allMessages[i].apply {
                                sent = true
                            }
                            repository.update(allMessages[i])
                            Thread.sleep(1000)
                            Log.v("UPDATE_STATUS", " >>>  ${allMessages[i].sent} i: $i")
                        }

                        i++
                        Log.v("ALL_MESSAGES", ">>> all Msg ${msg.message} -> ${msg.sent} i: $i")
                    }

                    Log.v("UPDATED_MSG", allMessages.last().sent.toString())
                    Log.v("SEND_SMS_SUCCESS", ">>> SMS Sent Successfully to .....}")
                } catch (e: Exception) {
                    allMessages.last().apply {
                        sent = false
                        isFailed = true
                    }
                    repository.update(allMessages.last())
                    Log.v("SEND_SMS_Error!...", ">>> Error While Sending SMS... $e")
                }
            }

            override fun onCompleted() {


            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)


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
        Toast.makeText(this@AddNewMessages, "Alarm is set for: \n${mSelectedDate.formatDate()}", Toast.LENGTH_SHORT)
            .show()
    }

    fun Calendar.formatDate(): String? {
        val simpleDateFormatter = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.US)
        return simpleDateFormatter.format(time)
    }

}