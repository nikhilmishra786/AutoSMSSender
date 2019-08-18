package com.infomantri.autosms.sender.activity

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProviders
import com.infomantri.autosms.sender.asynctask.BaseAsyncTask
import com.infomantri.autosms.sender.base.BaseActivity
import com.infomantri.autosms.sender.database.Message
import com.infomantri.autosms.sender.viewmodel.MessageViewModel
import kotlinx.android.synthetic.main.activity_add_message.btnSetAlarm
import kotlinx.android.synthetic.main.activity_add_message.etEnterMsg
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Handler
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.infomantri.autosms.sender.R
import com.infomantri.autosms.sender.database.MessageDbRepository
import com.infomantri.autosms.sender.database.MessageRoomDatabase
import com.infomantri.autosms.sender.receiver.DeliverReceiver
import com.infomantri.autosms.sender.receiver.SentReceiver
import java.io.Serializable


class AddMessages : BaseActivity() {

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
            if (msg.isEmpty().not()) {
                mViewModel.insert(Message(msg, System.currentTimeMillis(), false))
            }
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        btnSetAlarm.setOnClickListener {
            //            val intent = Intent(this, AddAlarmActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//            startActivity(intent)        }
            sendSMS(application)
        }

        fun sendSMS(context: Context, isFromDeliverReceiver: Boolean = false) {
            val smsManager = SmsManager.getDefault() as SmsManager

            val DEFAULT_MOBILE_NO = getSharedPreference(context).getString(DEFAULT_MOBILE_NO, "9867169318")
            val IS_DEFAULT = getSharedPreference(context).getBoolean(IS_DEFAULT_NO, true)

            BaseAsyncTask(object : BaseAsyncTask.SendSMSFromDb {
                override fun onStarted() {
                    val repository = getFromDatabase(context)
                    val allMessages = repository.allMessages
                    var index = 0
                    var mSentCount = 0

                    try {
                        if (isFromDeliverReceiver) {
                            updateDeliverStatus(context)
                        } else {
                            allMessages.iterator().forEach { msg ->
                                if (!msg.sent && mSentCount < 1) {

                                    val sentIntent = Intent(context, SentReceiver::class.java).apply {
                                        putExtra("MESSAGE_SENT", msg.id)
                                    }

                                    val sentPendingIntent = PendingIntent.getBroadcast(
                                        context, 0, sentIntent, PendingIntent.FLAG_UPDATE_CURRENT
                                    )

                                    val deliverIntent = Intent(context, DeliverReceiver::class.java).apply {
                                        putExtra("MESSAGE_DELIVER", msg.id)
                                    }
                                    val deliveredPendingIntent = PendingIntent.getBroadcast(
                                        context, 1, deliverIntent, PendingIntent.FLAG_UPDATE_CURRENT
                                    )

                                    getSharedPreference(context).edit().putInt("MESSAGE_ID", msg.id).apply()
                                    smsManager.sendTextMessage(
                                        DEFAULT_MOBILE_NO,
                                        null,
                                        msg.message,
                                        sentPendingIntent,
                                        deliveredPendingIntent
                                    )

                                    Thread.sleep(2 * 1000)
                                    if (getSharedPreference(context).getBoolean("IS_SENT_ERROR", false)) {
                                        msg.apply {
                                            sent = false
                                            isFailed = true
                                        }
                                        repository.updateMessage(msg)
                                        Log.v(
                                            "IS_SENT_ERROR",
                                            ">>> received an Error!... from sentReceiver() msg: ${msg.message} -> sent Error: ${msg.isFailed}..."
                                        )
                                    }
                                    mSentCount++
                                    if (mSentCount == 1)
                                        return@forEach
                                }
                                index++
                                Log.v(
                                    "ALL_MESSAGES",
                                    ">>> all Msg ${msg.message} -> sent: ${msg.sent} -> isFailed: ${msg.isFailed}"
                                )
                            }
                        }

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

        fun updateDeliverStatus(context: Context) {
            Log.v("MSG_DELIVERED", ">>> Msg delivered Running AsyncTask onStarted()...")
//                val messageId = intent?.getIntExtra("MESSAGE_DELIVER",-1)

            val messageId = getSharedPreference(context).getInt("MESSAGE_ID", -1)
            messageId.let {
                Log.v("MSG_DECODED_SHARED_PREF", ">>> Msg Id is received  Id: $messageId")
                val msgDao = MessageRoomDatabase.getDatabase(context).messageDbDao()
                val repository = MessageDbRepository(msgDao, messageId)

                val message = repository.messageById
                message.apply {
                    sent = true
                }
                Log.v("MSG_STATUS_UPDATED", ">>> Msg sent = true : sent: ${message.sent}")
                repository.updateMessage(message)

                Log.v(
                    "MSG_DELIVERY_STATUS",
                    ">>> onReceive() Msg: -> ${message.message} Status: ${message.sent} -> id: ${message.id}"
                )
            }
        }
    }
}