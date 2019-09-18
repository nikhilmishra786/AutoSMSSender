package com.infomantri.autosms.send.receiver

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.text.format.DateUtils
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager
import com.infomantri.autosms.send.R
import com.infomantri.autosms.send.activity.HomeActivity
import com.infomantri.autosms.send.base.BaseActivity
import com.infomantri.autosms.send.util.sendSMS

class DeliverReceiver: BroadcastReceiver() {

    var MESSAGE_SPLIT_COUNT = 0
    var SENT_MESSAGE_COUNT = 0
    var DELIVERED_MESSAGE_COUNT = 0

    override fun onReceive(context: Context?, intent: Intent?) {

//        val pendingResult: PendingResult = goAsync()

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        DELIVERED_MESSAGE_COUNT = sharedPref.getInt("DELIVERED_MESSAGE_COUNT", 0)
        SENT_MESSAGE_COUNT = sharedPref.getInt("SENT_MESSAGE_COUNT", 0)
        val totalDeliveredTimeTaken = sharedPref.getLong("FIRST_DELIVERED_FIRED_TIME_STAMP", System.currentTimeMillis())

        Log.v("Deliver_onReceive", ">>> DeliverReceiver onRecive() .....")
        context?.let {
            DELIVERED_MESSAGE_COUNT++
            sharedPref.edit().putInt("DELIVERED_MESSAGE_COUNT", DELIVERED_MESSAGE_COUNT).apply()
            Log.v("ASYNC_TASK", ">>> Before Async Task in Deliver onReceive()... resultCode: $resultCode Count: ${DELIVERED_MESSAGE_COUNT}")

            when(resultCode) {

                Activity.RESULT_OK -> {
                    sendSMS(context, isMessageSent = true)
                    sendNotification(context, 123456, "Message Delivered", "Message delivered Count: $DELIVERED_MESSAGE_COUNT Time: ${DateUtils.getRelativeTimeSpanString(totalDeliveredTimeTaken)}", HomeActivity::class.java)
                }

                Activity.RESULT_CANCELED -> {
                    sharedPref.edit().putInt("DELIVERED_MESSAGE_COUNT", --DELIVERED_MESSAGE_COUNT).apply()
                    sendNotification(context, 123456, "Message not Delivered", "Activity.RESULT_CANCELED DeliverCount--: $DELIVERED_MESSAGE_COUNT Time: ${DateUtils.getRelativeTimeSpanString(totalDeliveredTimeTaken)}", HomeActivity::class.java)
                }
            }
            if(MESSAGE_SPLIT_COUNT == DELIVERED_MESSAGE_COUNT || MESSAGE_SPLIT_COUNT == SENT_MESSAGE_COUNT) {
                sharedPref.edit().putInt("DELIVERED_MESSAGE_COUNT", 0).apply()
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

}