package com.infomantri.autosms.sender.receiver

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.telephony.SmsManager
import android.text.format.DateUtils
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager
import com.infomantri.autosms.sender.R
import com.infomantri.autosms.sender.activity.HomeActivity
import com.infomantri.autosms.sender.asynctask.BaseAsyncTask
import com.infomantri.autosms.sender.base.BaseActivity
import com.infomantri.autosms.sender.database.MessageDbRepository
import com.infomantri.autosms.sender.database.MessageRoomDatabase
import java.text.SimpleDateFormat
import java.util.*

class SentReceiver : BroadcastReceiver() {

    var MESSAGE_SPLIT_COUNT = 0
    var SENT_MESSAGE_COUNT = 0
    var DELIVERED_MESSAGE_COUNT = 0

    override fun onReceive(context: Context?, intent: Intent?) {

        val pendingResult: PendingResult = goAsync()

        Log.v("Sent_onReceive", ">>> Sent onRecive() .....")
        val reminderId = intent?.getIntExtra("reminder_id", 1000) ?: 1111
        val title = intent?.getStringExtra("reminder_title") ?: "title: "
        val timeStamp = intent?.getLongExtra("reminder_timestamp", -1) ?: "timeStamp: "
        val subTitle = "Message sent at : ${SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.US).format(timeStamp)}"

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        SENT_MESSAGE_COUNT = sharedPref.getInt("SENT_MESSAGE_COUNT", 0)
        MESSAGE_SPLIT_COUNT = sharedPref.getInt("MESSAGE_SPLIT_COUNT", 0)
        val totalSentTimeTaken = sharedPref.getLong("FIRST_SENT_FIRED_TIME_STAMP", System.currentTimeMillis())

        Log.v(
            "Intent_Extras_Sent",
            ">>> intent extras values: reminderId: $reminderId title: $title timeStamp: $timeStamp subTitle: $subTitle"
        )

        context?.let {
            BaseActivity.SENT_MESSAGE_COUNT++
            SENT_MESSAGE_COUNT++
            sharedPref.edit().putInt("SENT_MESSAGE_COUNT", SENT_MESSAGE_COUNT).apply()
            if(SENT_MESSAGE_COUNT == 1) {
                sharedPref.edit().putLong("FIRST_SENT_FIRED_TIME_STAMP",System.currentTimeMillis()).apply()
            }

            Log.v("CONTEXT_CHECK", ">>> inside context?.let{ } resultCode: $resultCode sentCount: $SENT_MESSAGE_COUNT MsgCount: $MESSAGE_SPLIT_COUNT")
            Log.v("CONTEXT_CHECK", ">>> inside context?.let{ } intent.action: ${intent?.action} timeDiff: ${DateUtils.getRelativeTimeSpanString(totalSentTimeTaken)}")

            when (resultCode) {

                Activity.RESULT_OK -> {
                    if(MESSAGE_SPLIT_COUNT == SENT_MESSAGE_COUNT) {
                        toast(context, "Message sent successfully...", false)
                        sendNotification(context, reminderId, title, "Count: $SENT_MESSAGE_COUNT Time: ${DateUtils.getRelativeTimeSpanString(totalSentTimeTaken)}}", HomeActivity::class.java)
                        BaseActivity().sendSMS(context, true)
                        sharedPref.edit().putInt("SENT_MESSAGE_COUNT", 0).apply()
                    }
                }

                SmsManager.RESULT_ERROR_GENERIC_FAILURE -> {
                    toast(context, "Sms Error Generic Failure...")
                }

                SmsManager.RESULT_ERROR_NO_SERVICE -> {
                    toast(context, "Sms Error No Service")
                }

                SmsManager.RESULT_ERROR_NULL_PDU -> {
                    toast(context, "Sms Error Null PDU")
                }

                SmsManager.RESULT_ERROR_RADIO_OFF -> {
                    toast(context, "Sms Error Radio Off")
                }

                Activity.RESULT_CANCELED -> {
                    Log.v("RESULT_CANCELED", ">>> STATUS_ON_ICC_SENT: ${SmsManager.STATUS_ON_ICC_SENT}...->")
                    if (intent?.action.equals("message_sent") && (MESSAGE_SPLIT_COUNT == SENT_MESSAGE_COUNT)) {
                        Log.v("RESULT_CANCELED", ">>> STATUS_ON_ICC_SENT: ${SmsManager.STATUS_ON_ICC_SENT}")
                        toast(context, "Message sent successfully...", false)
                        sendNotification(context, reminderId, title, "Activity.RESULT_CANCELED sentCount: $SENT_MESSAGE_COUNT Time: ${DateUtils.getRelativeTimeSpanString(totalSentTimeTaken)}", HomeActivity::class.java)
                        BaseActivity().sendSMS(context, true)
                        sharedPref.edit().putInt("SENT_MESSAGE_COUNT", 0).apply()
                    }
                }

                0 -> {
                    Log.v("RESULT_CANCELED", ">>> direct Jump to resultCode '0'").let { }
                }
            }
        }
    }

    private fun toast(context: Context?, msg: String, isSentFailed: Boolean = true) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        Log.v("SMS_SENT_OnReceive", msg)
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("IS_SENT_ERROR", isSentFailed).apply()
    }

    private fun sendNotification(
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
//                .setLargeIcon(
//                    BitmapFactory.decodeResource(
//                        context.resources,
//                        R.mipmap.ic_launcher_round
//                    )
//                )
                .setContentTitle(title)
                .setContentText(subTitle)
                .setSound(defaultSoundUri)
                .setPriority(NotificationManagerCompat.IMPORTANCE_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

        val notificationManager = context
            .getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "AlarmReminderChannel",
                "Auto SMS Sender channel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Text"
            }
            notificationManager?.createNotificationChannel(channel)
        }

        notificationManager?.notify(id, notificationBuilder.build())
    }

    fun Calendar.formatDate(): String? {
        val simpleDateFormatter = SimpleDateFormat("hh:mm a", Locale.US)
        return simpleDateFormatter.format(time)
    }

    fun updateDeliverStatus(context: Context, pendingResult: PendingResult) {
        Log.v("MSG_DELIVERED", ">>> Msg delivered Running AsyncTask onStarted()...")
//                val messageId = intent?.getIntExtra("MESSAGE_DELIVER",-1)

        BaseAsyncTask(object : BaseAsyncTask.SendSMSFromDb {

            override fun onStarted() {
                val messageId = PreferenceManager.getDefaultSharedPreferences(context).getInt("MESSAGE_ID", -1)
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
                    pendingResult.finish()
                }
            }

            override fun onCompleted() {
                pendingResult.finish()
            }
        })

    }
}