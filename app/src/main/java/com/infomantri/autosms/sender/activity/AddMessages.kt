package com.infomantri.autosms.sender.activity

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import com.infomantri.autosms.sender.R
import com.infomantri.autosms.sender.TimerFragment
import com.infomantri.autosms.sender.asynctask.BaseAsyncTask
import com.infomantri.autosms.sender.base.BaseActivity
import com.infomantri.autosms.sender.constants.AppConstants
import com.infomantri.autosms.sender.database.Message
import com.infomantri.autosms.sender.receiver.TimerReceiver
import com.infomantri.autosms.sender.viewmodel.MessageViewModel
import kotlinx.android.synthetic.main.activity_add_message.btnSetAlarm
import kotlinx.android.synthetic.main.activity_add_message.etEnterMsg
import kotlinx.android.synthetic.main.activity_new_message.*
import java.text.SimpleDateFormat
import java.util.*

class AddMessages : BaseActivity() {

    var DEFAULT_MOBILE_NO = "9867169318"
    private lateinit var mViewModel: MessageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_message)

        mViewModel = ViewModelProviders.of(this).get(MessageViewModel::class.java)
        setOnClickListner()


    }

    private fun setOnClickListner() {

        val fab: View = findViewById(R.id.fabSaveMessage)
        fab.setOnClickListener {
            val msg = etEnterMsg.text.toString()
            if (msg.isNullOrEmpty().not())
                mViewModel.insert(Message(msg, System.currentTimeMillis(), false))
            finish()
        }

        btnSetAlarm.setOnClickListener{
            val intent = Intent(this, AddAlarmActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)        }
    }

    fun sendSMS(context: Context) {

        BaseAsyncTask(object : BaseAsyncTask.SendSMSFromDb {
            override fun onStarted() {

                val smsManager = SmsManager.getDefault() as SmsManager
                val repository = getFromDatabase(context)
                val allMessages = repository.allMessages
                var index = 0
                var mSentCount = 0

                try {
                    allMessages.iterator().forEach { msg ->
                        if (!msg.sent && mSentCount < 1) {
                            smsManager.sendTextMessage(
                                    DEFAULT_MOBILE_NO,
                                    null,
                                    msg.message,
                                    null,
                                    null
                            )
                            allMessages[index].apply {
                                sent = true
                            }
                            repository.updateMessage(allMessages[index])
                            Thread.sleep(1000)
                            mSentCount++
                            Log.v(
                                    "UPDATE_STATUS",
                                    " >>>  ${allMessages[index].sent} msg: ${msg.message} mSentCount: $mSentCount")
                            if(mSentCount == 1)
                            return@forEach
                        }
                        index++
                        Log.v("ALL_MESSAGES", ">>> all Msg ${msg.message} -> ${mSentCount}")
                    }

                    Log.v("UPDATED_MSG", allMessages.last().sent.toString())
                    Log.v("SEND_SMS_SUCCESS", ">>> SMS Sent Successfully to .....}")
                } catch (e: Exception) {
                    allMessages[index].apply {
                        sent = false
                        isFailed = true
                    }
                    Log.v("SEND_SMS_Error!...", ">>> Error While Sending SMS... $e")
                }
            }

            override fun onCompleted() {


            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)


    }
}



//    fun insertMobileNoInDb(context: Context) {
//        BaseAsyncTask(object : BaseAsyncTask.SendSMSFromDb {
//            override fun onStarted() {
//
//                getFromDatabase(context).insertMobileNo(Subscribers(etEnterPhoneNo.text.toString(),false))
//                Log.v("INSERT_MOBILE_NO", ">>> inserted Mobile No into Subscribers table .....")
//
//                val defaultMobileNo = getFromDatabase(context).defaultMobileNo
//                defaultMobileNo.iterator().forEach { subscribers ->
//                    Log.v("PRINT_DEFAULT_NO", ">>> Default: ${subscribers.mobileNo}")
//                }
//            }
//
//            override fun onCompleted() {
//
//            }
//        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
//
//    }
//}