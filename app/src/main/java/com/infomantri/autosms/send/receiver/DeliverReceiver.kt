package com.infomantri.autosms.send.receiver

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.AsyncTask
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.text.format.DateUtils
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager
import com.infomantri.autosms.send.R
import com.infomantri.autosms.send.activity.HomeActivity
import com.infomantri.autosms.send.asynctask.BaseAsyncTask
import com.infomantri.autosms.send.base.BaseActivity
import com.infomantri.autosms.send.constants.AppConstant
import com.infomantri.autosms.send.database.MessageDbRepository
import com.infomantri.autosms.send.database.MessageRoomDatabase
import com.syngenta.pack.util.*
import java.util.*

class DeliverReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

//        val pendingResult: PendingResult = goAsync()

        Log.v("Deliver_onReceive", ">>> DeliverReceiver onReceive() .....")
        context?.let {
            val msgId = intent?.extras?.getInt(AppConstant.MESSAGE_ID) ?: -1
            Log.v(
                "ASYNC_TASK",
                ">>> Before Async Task in Deliver onReceive()... resultCode: $resultCode msgId: $msgId}"
            )

            when (resultCode) {

                Activity.RESULT_OK -> {

                    updateDeliverStatus(
                        context,
                        msgId
                    )

                    sendNotification(
                        context,
                        AppConstant.NOTIFICATION_ID,
                        "Message Delivered",
                        "Msg id: $msgId",
                        HomeActivity::class.java
                    )
                    Log.v(
                        AppConstant.MESSAGE_DELIVERED,
                        ">>> Message Delivered -> Message delivered  Time: ${DateUtils.getRelativeTimeSpanString(
                            System.currentTimeMillis()
                        )}"
                    )
                }

                Activity.RESULT_CANCELED -> {
                    sendNotification(
                        context,
                        AppConstant.NOTIFICATION_ID,
                        "Message not Delivered",
                        "Activity.RESULT_CANCELED DeliverCount--: Time: ${DateUtils.getRelativeTimeSpanString(
                            System.currentTimeMillis()
                        )}",
                        HomeActivity::class.java
                    )
                }
                else -> {

                }
            }
        }
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

    fun updateDeliverStatus(context: Context, msgId: Int) {
        var handler: Handler?
        Log.v("MSG_DELIVERED", ">>> Msg delivered Running AsyncTask onStarted()...")

        val handlerThread = HandlerThread(AppConstant.Handler.UPDATE_HANDLER)
        handlerThread.also {
            it.start()
            handler = Handler(it.looper)
        }
        handler?.postDelayed({

            Log.v("MSG_DECODED_SHARED_PREF", ">>> Msg Id is received  Id: $msgId")
            val msgDao = MessageRoomDatabase.getDatabase(context).messageDbDao()
            val repository = MessageDbRepository(msgDao, id = msgId)

            val message = repository.messageById
            Log.v("Updated_MSG", ">>> Msg: $message")

            message?.let {
                if (msgId != -1)
                    message.sent = true
                Log.v("MSG_STATUS_UPDATED", ">>> Msg sent = true : sent: ${message.sent}")
                repository.updateMessage(message)

                Log.v(
                    "MSG_DELIVERY_STATUS",
                    ">>> onReceive() Msg: -> ${message.message} Status: ${message.sent} -> id: ${message.id}"
                )
            }
        }, 500)
    }

}