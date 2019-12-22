package com.infomantri.autosms.send.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.Toast
import com.infomantri.autosms.send.activity.AddAlarmsActivity
import com.infomantri.autosms.send.constants.AppConstant
import com.infomantri.autosms.send.util.*
import java.text.SimpleDateFormat
import java.util.*

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        Log.v("ALARM_onReceive", ">>> Alarm onRecive() ... action: ${intent?.action}")
        val reminderId = intent?.getIntExtra("reminder_id", 1000) ?: 1111
        val title = intent?.getStringExtra("reminder_title") ?: "title: "
        val timeStamp = intent?.getLongExtra("reminder_timestamp", -1) ?: "timeStamp (Error): ?"
        val subTitle =
            "Reminder is from : ${SimpleDateFormat("hh:mm a", Locale.US).format(
                timeStamp
            )}"

        context?.let {
            playRingtone(context)
            vibratePhone(context)
            Toast.makeText(context, "REMINDER", Toast.LENGTH_LONG).show()

            if (intent?.action == AppConstant.Intent.ACTION_MESSAGE_ALARM) {
                timeStamp as Long
                sendSMS(context)
                sendNotification(
                    context,
                    System.currentTimeMillis().toInt(),
                    "Phone Call Alarm Successfully Done",
                    "Phone Call for Wakeup Alarm... ${timeStamp.formatTime()}",
                    AddAlarmsActivity::class.java,
                    channelId = AppConstant.Notification.Channel.PHONE_CALL_CHANNEL_ID,
                    channelName = AppConstant.Notification.Channel.PHONE_CALL_CHANNEL
                )
            }

            if (intent?.action == AppConstant.Intent.ACTION_PHONE_CALL_ALARM) {
                context.phoneCallToNumber(
                    context.getStringFromPreference(AppConstant.DEFAULT_MOBILE_NO) ?: "9321045517"
                )
                timeStamp as Long
                sendNotification(
                    context,
                    System.currentTimeMillis().toInt(),
                    "Phone Call Alarm Successfully Done",
                    "Phone Call for Wakeup Alarm... ${timeStamp.formatTime()}",
                    AddAlarmsActivity::class.java,
                    channelId = AppConstant.Notification.Channel.PHONE_CALL_CHANNEL_ID,
                    channelName = AppConstant.Notification.Channel.PHONE_CALL_CHANNEL
                )
            }

            if (intent?.action == AppConstant.Intent.ACTION_CONFIRMATION_SMS) {
                context.phoneCallToNumber(
                    context.getStringFromPreference(AppConstant.DEFAULT_MOBILE_NO) ?: "9321045517"
                )
                timeStamp as Long
                sendNotification(
                    context,
                    System.currentTimeMillis().toInt(),
                    "Phone Call Alarm for Request Successfully Done",
                    "Phone Call for Requested Alarm... ${timeStamp.formatTime()}",
                    AddAlarmsActivity::class.java,
                    channelId = AppConstant.Notification.Channel.PHONE_CALL_CHANNEL_ID,
                    channelName = AppConstant.Notification.Channel.PHONE_CALL_CHANNEL
                )
            }
        }
    }
}

private fun playRingtone(context: Context?) {
    val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    val ringtone = RingtoneManager.getRingtone(context, uri)
    ringtone.play()
}

fun vibratePhone(context: Context?) {
    val vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (Build.VERSION.SDK_INT >= 26) {
        vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        vibrator.vibrate(5000)
    }
}

fun formatDate(timeStamp: Long): String? {
    val simpleDateFormatter = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.US)
    return simpleDateFormatter.format(timeStamp)
}
//
//fun sendNotification(
//    context: Context, id: Int, title: String,
//    subTitle: String,
//    activity: Class<*>
//) {
//
//    val intent = Intent(context, activity)
//    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
//    intent.action = "" + Math.random()
//
//    val pendingIntent = PendingIntent.getActivity(
//        context, 2 /* Request code */, intent,
//        PendingIntent.FLAG_CANCEL_CURRENT
//    )
//
//    val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
//
//    val notificationBuilder =
//        NotificationCompat.Builder(context, "AlarmReminderChannel")
//            .setSmallIcon(R.drawable.ic_sms_red_108_dp)
////                .setLargeIcon(
////                    BitmapFactory.decodeResource(
////                        context.resources,
////                        R.mipmap.ic_launcher_round
////                    )
////                )
//            .setContentTitle(title)
//            .setContentText(subTitle)
//            .setSound(defaultSoundUri)
//            .setPriority(NotificationManagerCompat.IMPORTANCE_HIGH)
//            .setAutoCancel(true)
//            .setContentIntent(pendingIntent)
//
//    val notificationManager = context
//        .getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//        val channel = NotificationChannel(
//            "AlarmReminderChannel",
//            "Auto SMS Sender channel",
//            NotificationManager.IMPORTANCE_HIGH
//        ).apply {
//            description = "Text"
//        }
//        notificationManager?.createNotificationChannel(channel)
//    }
//
//    notificationManager?.notify(id, notificationBuilder.build())
//}
