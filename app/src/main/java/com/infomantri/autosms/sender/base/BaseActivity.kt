package com.infomantri.autosms.sender.base

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.PersistableBundle
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.infomantri.autosms.sender.R
import com.infomantri.autosms.sender.asynctask.BaseAsyncTask
import com.infomantri.autosms.sender.database.MessageDbRepository
import com.infomantri.autosms.sender.database.MessageRoomDatabase
import com.infomantri.autosms.sender.receiver.DeliverReceiver
import com.infomantri.autosms.sender.receiver.SentReceiver

open class BaseActivity : AppCompatActivity() {

    val IS_DEFAULT_NO = "IS_DEFAULT_NO"
    val DEFAULT_MOBILE_NO = "DEFAULT_MOBILE_NO"

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState)

    }

    companion object

    fun getFromDatabase(context: Context): MessageDbRepository {

        val msgDao = MessageRoomDatabase.getDatabase(context).messageDbDao()
        val repository = MessageDbRepository(msgDao)

        return repository
    }

    fun getSharedPreference(context: Context): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

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
            NotificationCompat.Builder(context, "ReminderChannel")
                .setSmallIcon(R.drawable.ic_launcher_background)
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
        notificationManager?.notify(id, notificationBuilder.build())
    }

    fun sendSMS(context: Context, isFromDeliverReceiver: Boolean = false) {
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

                                val sentPIList = arrayListOf(sentPendingIntent)
                                val deliverPIList = arrayListOf(deliveredPendingIntent)

                                getSharedPreference(context).edit().putInt("MESSAGE_ID", msg.id).apply()

                                val msgListParts = smsManager.divideMessage(msg.message)
//                                msgListParts.iterator().forEach {msg ->
//                                    Log.v("DIVIDE_MESSAGE", ">>> divide Msg: $msg\n")
//                                    deliverPIList.add(deliveredPendingIntent)
//                                }

                                smsManager.sendMultipartTextMessage(
                                    DEFAULT_MOBILE_NO,
                                    null,
                                    msgListParts,
                                    sentPIList,
                                    deliverPIList
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