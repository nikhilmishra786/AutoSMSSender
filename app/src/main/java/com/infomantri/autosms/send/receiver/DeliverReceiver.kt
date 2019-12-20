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
import android.os.Handler
import android.os.HandlerThread
import android.text.format.DateUtils
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.infomantri.autosms.send.R
import com.infomantri.autosms.send.activity.HomeActivity
import com.infomantri.autosms.send.constants.AppConstant
import com.infomantri.autosms.send.database.MessageDbRepository
import com.infomantri.autosms.send.database.MessageRoomDatabase
import com.infomantri.autosms.send.util.formatTime
import com.infomantri.autosms.send.util.sendNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
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
                        "Message Delivered Successfully",
                        "Msg id: $msgId Time: ${Calendar.getInstance().formatTime()}",
                        HomeActivity::class.java,
                        channelId = AppConstant.Notification.Channel.MESSAGE_CHANNEL_ID,
                        channelName = AppConstant.Notification.Channel.MESSAGE_CHANNEL
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
                        "Activity.RESULT_CANCELED Time: ${Calendar.getInstance().formatTime()}",
                        HomeActivity::class.java,
                        channelId = AppConstant.Notification.Channel.MESSAGE_CHANNEL_ID,
                        channelName = AppConstant.Notification.Channel.MESSAGE_CHANNEL
                    )
                }
                else -> {

                }
            }
        }
    }

    fun updateDeliverStatus(context: Context, msgId: Int) {
        Log.v("MSG_DELIVERED", ">>> Msg delivered Running AsyncTask onStarted()...")

        val mJob = Job()
        CoroutineScope(Dispatchers.Default + mJob).launch {

            Log.v("MSG_DECODED_SHARED_PREF", ">>> Msg Id is received  Id: $msgId")
            val msgDao = MessageRoomDatabase.getDatabase(context).messageDbDao()
            val repository = MessageDbRepository(msgDao, id = msgId)

            val message = repository.messageById
            Log.v("Updated_MSG", ">>> Msg: $message")

            message?.let {
                if (msgId != -1) {
                    message.sent = true
                    message.timeStamp = System.currentTimeMillis()
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