package com.infomantri.autosms.send.receiver

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.telephony.SmsManager
import android.util.Log
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.infomantri.autosms.send.activity.AddAlarmsActivity
import com.infomantri.autosms.send.constants.AppConstant
import com.infomantri.autosms.send.util.getStringFromPreference
import com.infomantri.autosms.send.util.phoneCallToNumber
import com.infomantri.autosms.send.util.sendNotification
import com.infomantri.autosms.send.util.sendSMS
import kotlinx.coroutines.*

class SmsReceiver : BroadcastReceiver() {

    private val codePattern = "(\\d{4})".toRegex()
    private val mJob = Job()

    override fun onReceive(context: Context?, intent: Intent?) {

        Log.v("SMS_RECEIVER", ">>> SMS Received successfully... $$$ SMS Retriver()")
        if (SmsRetriever.SMS_RETRIEVED_ACTION == intent?.action) {
            val bundle = intent.extras
            val status = bundle?.get(SmsRetriever.EXTRA_STATUS) as Status
            Log.v(
                "SMS_RECEIVER",
                ">>> SMS Received successfully... $ SmsRetriever.SMS_RETRIEVED_ACTION"
            )

            when (status.statusCode) {
                CommonStatusCodes.SUCCESS -> {
                    val message = bundle.get(SmsRetriever.EXTRA_SMS_MESSAGE) as String
                    print("Received message : $message")

                    Log.v("SMS_RECEIVER", ">>> MSG: $message")
                    val code: MatchResult? = codePattern.find(message)
                    code?.let {
                        context?.let {
                            if (code.value.toInt() % 100 == 99) {
                                sendNotification(
                                    context,
                                    AppConstant.Notification.PHONE_CALL,
                                    "Phone Call Request Received...",
                                    "Alarm Request Received from Nitin Jio ${extractTimeFromMsg(
                                        message
                                    )}...",
                                    AddAlarmsActivity::class.java,
                                    channelId = AppConstant.Notification.Channel.PHONE_CALL_CHANNEL_ID,
                                    channelName = AppConstant.Notification.Channel.PHONE_CALL_CHANNEL
                                )

                                CoroutineScope(Dispatchers.Default + mJob).launch {
                                    context.phoneCallToNumber(
                                        context.getStringFromPreference(AppConstant.DEFAULT_MOBILE_NO)
                                            ?: "9321045517"
                                    )
                                    sendNotification(
                                        context,
                                        System.currentTimeMillis().toInt(),
                                        "Phone Call Alarm Successfully Done",
                                        "Called to Nitin Jio for Alarm Wakeup...",
                                        AddAlarmsActivity::class.java,
                                        channelId = AppConstant.Notification.Channel.PHONE_CALL_CHANNEL_ID,
                                        channelName = AppConstant.Notification.Channel.PHONE_CALL_CHANNEL
                                    )

                                    extractTimeFromMsg(message) * 60 * 1000
                                    val sendMessage =
                                        "Your Alarm has been set at Time: ${extractTimeFromMsg(
                                            message
                                        )}".plus(
                                            if (message.contains("min")) "minutes" else "hours"
                                        )
                                    sendSMS(context, sendMessage)
                                }


                            } else {
                                //                            if (code.value.toInt() % 10 == 0)
                                sendSMS(
                                    context
                                )
                            }
                        }
                        Log.v("SMS_RECEIVER", ">>> Code Value is: ${code.value}")
                    }
                }
                CommonStatusCodes.TIMEOUT -> {
                    // Handle Timeout error
                    Log.v(
                        "SMS_RECEIVER",
                        ">>> Error! SMS Retriever Time Out..."
                    )
                }
            }
        }
    }

    private fun extractTimeFromMsg(message: String): Long {
        val code: MatchResult?
        if (message.contains("min")) {
            code = ("(\\d{2})".toRegex()).find(message)
            code?.let {
                Log.v(
                    "TIME_FROM_MSG",
                    ">>> extractTimeFromMsg : Time : ${it.value} message: $message"
                )
                return it.value.toLong()
            }
        } else if (message.contains("hr")) {
            code = ("(\\d{2})".toRegex()).find(message.split("hr")[0].trim())
            code?.let {
                Log.v(
                    "TIME_FROM_MSG",
                    ">>> extractTimeFromMsg : Time : ${it.value} message: $message"
                )
                return it.value.toLong() * 60
            }
        }
        Log.v("TIME_FROM_MSG", ">>> extractTimeFromMsg : Time : 10 min message: $message")
        return 10
    }

    private fun sendSMS(
        context: Context, message: String
    ) {
        var handler: Handler?
        val handlerThread = HandlerThread(AppConstant.Handler.SENT_HANDLER)
        handlerThread.also {
            it.start()
            handler = Handler(it.looper)
        }
        handler?.post {
            val smsManager = SmsManager.getDefault() as SmsManager

            val DEFAULT_MOBILE_NO =
                context.getStringFromPreference(AppConstant.DEFAULT_MOBILE_NO)

            try {


                val sentIntent = Intent(context, SentReceiver::class.java).apply {
                    putExtra(AppConstant.MESSAGE, message)
                    putExtra(
                        AppConstant.Reminder.TIME_STAMP,
                        System.currentTimeMillis()
                    )
                    putExtra(AppConstant.Reminder.REMINDER_ID, System.currentTimeMillis().toInt())
                    putExtra(AppConstant.Reminder.TITLE, "Message Sent Successfully...")
                }
                sentIntent.action = AppConstant.MESSAGE_SENT

                val sentPendingIntent = PendingIntent.getBroadcast(
                    context, 0, sentIntent, PendingIntent.FLAG_UPDATE_CURRENT
                )

                val deliveredIntent =
                    Intent(context, DeliverReceiver::class.java)

                val deliveredPendingIntent = PendingIntent.getBroadcast(
                    context, 0, deliveredIntent, PendingIntent.FLAG_UPDATE_CURRENT
                )

                smsManager.sendTextMessage(
                    DEFAULT_MOBILE_NO,
                    null,
                    message,
                    sentPendingIntent,
                    deliveredPendingIntent
                )


            } catch (e: Exception) {

                Log.v(
                    "SEND_SMS_Error!...",
                    ">>> (inside SmsReceiver) Error While Sending SMS... $e"
                )
            }
        }
    }
}