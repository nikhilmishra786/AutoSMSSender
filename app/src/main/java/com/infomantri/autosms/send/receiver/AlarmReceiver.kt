package com.infomantri.autosms.send.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.*
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.infomantri.autosms.send.R
import com.infomantri.autosms.send.activity.HomeActivity
import com.infomantri.autosms.send.asynctask.BaseAsyncTask
import com.infomantri.autosms.send.base.BaseActivity
import com.infomantri.autosms.send.constants.AppConstant
import com.syngenta.pack.util.*
import java.text.SimpleDateFormat
import java.util.*

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        Log.v("ALARM_onReceive", ">>> Alarm onRecive() .....")
        val reminderId = intent?.getIntExtra("reminder_id", 1000) ?: 1111
        val title = intent?.getStringExtra("reminder_title") ?: "title: "
        val timeStamp = intent?.getLongExtra("reminder_timestamp", -1) ?: "timeStamp (Error): ?"
        val subTitle =
            "Reminder is fired at : ${SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.US).format(
                timeStamp
            )}"

        context?.let {
            Log.e("REMINDER", ">>> Reminder recevied 1 ->>>>")
            sendNotification(context, reminderId, title, subTitle, HomeActivity::class.java)

            playRingtone(context)
            vibratePhone(context)
            Log.e("REMINDER", ">>> Reminder recevied 3 ->>>>")
            Toast.makeText(context, "REMINDER", Toast.LENGTH_LONG).show()
            Log.e("REMINDER", ">>> Reminder recevied 4 ->>>>")

            sendSMS(context)

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
            .setSmallIcon(R.drawable.ic_sms_red_108_dp)
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

//fun sendSMS(
//    context: Context
//) {
//    val smsManager = SmsManager.getDefault() as SmsManager
//    Log.v(
//        "SmsManager_",
//        ">>> SmsManger.getDefaultSmsSubscriptionId(): ${SmsManager.getDefaultSmsSubscriptionId()}"
//    )
//
//    val DEFAULT_MOBILE_NO =
//        getSharedPreference(context).getString(AppConstant.DEFAULT_MOBILE_NO, "9867169318")
//
//    BaseAsyncTask(object : BaseAsyncTask.SendSMSFromDb {
//        override fun onStarted() {
//            val repository = getFromDatabase(context)
//            val allMessages = repository.allMessages
//            var index = 0
//            var mSentCount = 0
//
//            try {
//                allMessages.iterator().forEach { msg ->
//                    if (!msg.sent && mSentCount < 1) {
//
//                        val sentIntent = Intent(context, SentReceiver::class.java).apply {
//                            putExtra(AppConstant.MESSAGE_ID, msg.id)
//                            putExtra(
//                                AppConstant.Reminder.TIME_STAMP,
//                                System.currentTimeMillis()
//                            )
//                            putExtra(AppConstant.Reminder.REMINDER_ID, 6)
//                            putExtra(
//                                AppConstant.Reminder.TITLE,
//                                "Message Sent Successfully..."
//                            )
//                        }
//
//                        val sentPendingIntent = PendingIntent.getBroadcast(
//                            context, 0, sentIntent, PendingIntent.FLAG_UPDATE_CURRENT
//                        )
//
//                        val deliveredIntent =
//                            Intent(context, DeliverReceiver::class.java).apply {
//                                putExtra(AppConstant.MESSAGE_ID, msg.id)
//                            }
//                        val deliveredPendingIntent = PendingIntent.getBroadcast(
//                            context, 0, deliveredIntent, PendingIntent.FLAG_UPDATE_CURRENT
//                        )
//
//                        val sentPIList = ArrayList<PendingIntent>()
//                        val deliveredPIList = ArrayList<PendingIntent>()
//
//
//                        val msgListParts = smsManager.divideMessage(msg.message)
////                                msgListParts.iterator().forEach {msg ->
////                                    Log.v("DIVIDE_MESSAGE", ">>> divide Msg: $msg\n")
////                                    deliverPIList.add(deliveredPendingIntent)
////                                }
//                        var count = 0
//
//                        repeat(msgListParts.size) {
//                            sentPIList.add(count, sentPendingIntent)
//                            deliveredPIList.add(count, deliveredPendingIntent)
//                            count++
//                        }
//
//                        smsManager.sendMultipartTextMessage(
//                            DEFAULT_MOBILE_NO,
//                            null,
//                            msgListParts,
//                            sentPIList,
//                            deliveredPIList
//                        )
//
////                            Thread.sleep(2 * 1000)
//                        mSentCount++
//                        if (mSentCount == 1)
//                            return@forEach
//                    }
//                    index++
////                            Log.v(
////                                "ALL_MESSAGES",
////                                ">>> all Msg ${msg.message} -> sent: ${msg.sent} -> isFailed: ${msg.isFailed}"
////                            )
//                    Log.v("MSG_STATUS_UPDATED", ">>> Msg sent: ${msg.sent}")
//
//                    if (msg.id != -1)
//                        msg.sent = true
//                    repository.updateMessage(msg)
//                    Log.v("MSG_STATUS_UPDATED", ">>> Msg sent: ${msg.sent}")
//                }
//
//            } catch (e: Exception) {
//                allMessages[index].apply {
//                }
//                com.syngenta.pack.util.sendNotification(
//                    context,
//                    404,
//                    "SMS send error!",
//                    "Error -> $e",
//                    HomeActivity::class.java
//                )
//                Log.v("SEND_SMS_Error!...", ">>> Error While Sending SMS... $e")
//            }
//        }
//
//        override fun onCompleted() {
//        }
//    }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
//
//}

