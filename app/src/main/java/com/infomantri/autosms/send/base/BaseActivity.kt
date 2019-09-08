package com.infomantri.autosms.send.base

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.telephony.SmsManager
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.infomantri.autosms.send.R
import com.infomantri.autosms.send.activity.AddAlarmsActivity
import com.infomantri.autosms.send.activity.HomeActivity
import com.infomantri.autosms.send.activity.SettingsActivity
import com.infomantri.autosms.send.asynctask.BaseAsyncTask
import com.infomantri.autosms.send.database.MessageDbRepository
import com.infomantri.autosms.send.database.MessageRoomDatabase
import com.infomantri.autosms.send.receiver.DeliverReceiver
import com.infomantri.autosms.send.receiver.SentReceiver
import kotlinx.android.synthetic.main.custom_toolbar.*

open class BaseActivity : AppCompatActivity() {

    val IS_DEFAULT_NO = "IS_DEFAULT_NO"
    val DEFAULT_MOBILE_NO = "DEFAULT_MOBILE_NO"

    companion object {
        var MESSAGE_SPLIT_COUNT = 0
        var SENT_MESSAGE_COUNT = 0
        var DELIVERED_MESSAGE_COUNT = 0
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState)
    }

    fun getFromDatabase(context: Context): MessageDbRepository {

        val msgDao = MessageRoomDatabase.getDatabase(context).messageDbDao()

        return MessageDbRepository(msgDao)
    }

    fun getSharedPreference(context: Context): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    fun Toolbar.setToolbar(
        showBackNav: Boolean = true,
        titleColor: Int = R.color.title,
        centerTitle: String? = null,
        bgColor: Int = R.color.red
    ) {
        setSupportActionBar(this)

        toolIvSettings.visibility = if (showBackNav) View.GONE else View.VISIBLE
        supportActionBar?.apply {
            title = ""
            setDisplayHomeAsUpEnabled(showBackNav)
        }

        if (titleColor == R.color.white) {
            toolIvSettings.setColorFilter(ContextCompat.getColor(context, R.color.white))
            toolIvAddAlarm.setColorFilter(ContextCompat.getColor(context, R.color.white))

            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_tool_back_white)
        }else {
            toolIvAddAlarm.setColorFilter(ContextCompat.getColor(context, R.color.orange))
            toolIvSettings.setColorFilter(ContextCompat.getColor(context, R.color.lightBlue))
        }

        centerTitle?.let {
            toolTvTitle?.text = it
            toolTvTitle?.setTextColor(
                ContextCompat.getColor(
                    this@BaseActivity,
                    titleColor
                )
            )
        }

        toolbar.setBackgroundColor(ContextCompat.getColor(this@BaseActivity, bgColor))
    }

    fun showAlertDialog(deleteMsg: (Boolean) -> Unit) {
        val builder = AlertDialog.Builder(this)
        //set title for alert dialog
        builder.setTitle("Delete Message")
        //set message for alert dialog
        builder.setMessage("Deleting message will remove from list")
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        //performing positive action
        builder.setPositiveButton("Yes") { dialogInterface, which ->
            deleteMsg(true)
            Log.v("DIALOG", ">>> Inside Alert Dialog >>><<< Yes")
//            Toast.makeText(applicationContext, "clicked yes", Toast.LENGTH_LONG).show()
        }
//        //performing cancel action
//        builder.setNeutralButton("Cancel") { dialogInterface, which ->
////            Toast.makeText(applicationContext, "clicked cancel\n operation cancel", Toast.LENGTH_LONG).show()
//        }
        //performing negative action
        builder.setNegativeButton("No") { dialogInterface, which ->
//            Toast.makeText(applicationContext, "clicked No", Toast.LENGTH_LONG).show()
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(true)
        alertDialog.show()
    }

    fun sendNotification(
        context: Context, id: Int, title: String,
        subTitle: String,
        activity: Class<*>
    ) {

        val intent = Intent(context, activity)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.action = "" + Math.random()

        val pendingIntent = PendingIntent.getActivity(
            context, 2 /* Request code */, intent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder =
            NotificationCompat.Builder(context, "AlarmReminderChannel")
                .setSmallIcon(R.drawable.ic_sms_launcher_icon_108x108)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        context.resources,
                        R.mipmap.ic_launcher_round
                    )
                )
                .setContentTitle(title)
                .setContentText(subTitle)
                .setSound(defaultSoundUri)
                .setPriority(NotificationManagerCompat.IMPORTANCE_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

        val notificationManager = context
            .getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("AlarmReminderChannel", "Auto SMS Sender channel", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Text"
            }
            notificationManager?.createNotificationChannel(channel)
        }

        notificationManager?.notify(id, notificationBuilder.build())
    }

    fun sendSMS(context: Context, isMessageSent: Boolean = false) {
        val smsManager = SmsManager.getDefault() as SmsManager
        Log.v("SmsManager_",">>> SmsManger.getDefaultSmsSubscriptionId(): ${SmsManager.getDefaultSmsSubscriptionId()}")

        val DEFAULT_MOBILE_NO = getSharedPreference(context).getString(DEFAULT_MOBILE_NO, "9867169318")
        val IS_DEFAULT = getSharedPreference(context).getBoolean(IS_DEFAULT_NO, true)

        BaseAsyncTask(object : BaseAsyncTask.SendSMSFromDb {
            override fun onStarted() {
                val repository = getFromDatabase(context)
                val allMessages = repository.allMessages
                var index = 0
                var mSentCount = 0

                try {
                    if (isMessageSent) {
                        updateDeliverStatus(context)
                    } else {
                        allMessages.iterator().forEach { msg ->
                            if (!msg.sent && mSentCount < 1) {

                                val sentIntent = Intent(context, SentReceiver::class.java).apply {
                                    putExtra("MESSAGE_SENT", msg.id)
                                    putExtra("reminder_timestamp", System.currentTimeMillis())
                                    putExtra("reminder_id", 6)
                                    putExtra("reminder_title", "Message Sent Successfully...")
                                }
                                sentIntent.action = "message_sent"

                                val sentPendingIntent = PendingIntent.getBroadcast(
                                    context, 0, sentIntent, PendingIntent.FLAG_UPDATE_CURRENT
                                )

                                val deliveredIntent = Intent(context, DeliverReceiver::class.java).apply {
                                    putExtra("MESSAGE_DELIVER", msg.id)
                                }
                                val deliveredPendingIntent = PendingIntent.getBroadcast(
                                    context, 0, deliveredIntent, PendingIntent.FLAG_UPDATE_CURRENT
                                )

                                val sentPIList = ArrayList<PendingIntent>()
                                val deliveredPIList = ArrayList<PendingIntent>()

                                getSharedPreference(context).edit().putInt("MESSAGE_ID", msg.id).apply()

                                val msgListParts = smsManager.divideMessage(msg.message)
//                                msgListParts.iterator().forEach {msg ->
//                                    Log.v("DIVIDE_MESSAGE", ">>> divide Msg: $msg\n")
//                                    deliverPIList.add(deliveredPendingIntent)
//                                }
                                var count = 0

                                repeat(msgListParts.size) {
                                    sentPIList.add(count, sentPendingIntent)
                                    deliveredPIList.add(count, deliveredPendingIntent)
                                    count++
                                }
                                MESSAGE_SPLIT_COUNT = count
                                getSharedPreference(context).edit().putInt("MESSAGE_SPLIT_COUNT", count).apply()

                                smsManager.sendMultipartTextMessage(
                                    DEFAULT_MOBILE_NO,
                                    null,
                                    msgListParts,
                                    sentPIList,
                                    deliveredPIList
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
//                            Log.v(
//                                "ALL_MESSAGES",
//                                ">>> all Msg ${msg.message} -> sent: ${msg.sent} -> isFailed: ${msg.isFailed}"
//                            )
                        }
                    }

                } catch (e: Exception) {
                    allMessages[index].apply {
                        sent = false
                        isFailed = true
                    }
                    sendNotification(application, 404, "SMS send error!", "Error -> $e", HomeActivity::class.java)
                    Log.v("SEND_SMS_Error!...", ">>> Error While Sending SMS... $e")
                }
            }

            override fun onCompleted() {
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

    }

    fun updateDeliverStatus(context: Context) {
        Log.v("MSG_DELIVERED", ">>> updateDeliverStatus Running AsyncTask onStarted()...")
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
                ">>> Msg: -> ${message.message} Status: ${message.sent} -> id: ${message.id}"
            )
        }
    }
}