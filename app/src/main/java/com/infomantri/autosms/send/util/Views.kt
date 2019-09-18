package com.infomantri.autosms.send.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.*
import android.media.RingtoneManager
import android.os.AsyncTask
import android.os.Build
import android.telephony.SmsManager
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import androidx.preference.PreferenceManager
import com.infomantri.autosms.send.R
import com.infomantri.autosms.send.SmsSenderApp
import com.infomantri.autosms.send.activity.HomeActivity
import com.infomantri.autosms.send.asynctask.BaseAsyncTask
import com.infomantri.autosms.send.base.BaseActivity
import com.infomantri.autosms.send.database.Message
import com.infomantri.autosms.send.database.MessageDbRepository
import com.infomantri.autosms.send.database.MessageRoomDatabase
import com.infomantri.autosms.send.receiver.DeliverReceiver
import com.infomantri.autosms.send.receiver.SentReceiver
import java.text.SimpleDateFormat
import java.util.*

const val IS_DEFAULT_NO = "IS_DEFAULT_NO"
const val DEFAULT_MOBILE_NO = "DEFAULT_MOBILE_NO"

inline fun <reified T : ViewModel> AppCompatActivity.initViewModel(): T {
    return ViewModelProviders.of(this).get(T::class.java)
}

fun EditText.isNotEmpty() = getTrimmedText().isNotEmpty()

fun EditText.getTrimmedText() = this.text.toString().trim()

fun TextView.getStringText() = this.text.toString()

fun View.setGone() {
    visibility = View.GONE
}

fun View.setVisible() {
    visibility = View.VISIBLE
}

fun View.setInvisible() {
    visibility = View.INVISIBLE
}

fun Calendar.formatDate(): String? {
    val simpleDateFormatter = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.US)
    return simpleDateFormatter.format(time)
}

fun EditText.removeNewLine(): String {

    val text = this.text.toString().trim().removeSurrounding("\n", "\n").trimMargin("|")
        .trimIndent()
    val newText = ""
    var indexWhiteSpace = 0
    text.forEach {
        if(!it.isWhitespace() && (it == '\n').not()){
            newText.plus(text.get(indexWhiteSpace))
            indexWhiteSpace++
        }
    }
    return text
}

fun getSharedPreference(context: Context): SharedPreferences =
    PreferenceManager.getDefaultSharedPreferences(context)

fun TextView.multiColorTextView(
    lineSplitLength: Int,
    @ColorRes firstTextColor: Int,
    @ColorRes secondTextColor: Int
) {

    val word = SpannableString(this.text.toString())

    word.setSpan(
        ForegroundColorSpan(ContextCompat.getColor(this.context, firstTextColor)),
        0, lineSplitLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    word.setSpan(
        ForegroundColorSpan(ContextCompat.getColor(this.context, secondTextColor)),
        lineSplitLength + 1, word.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    this.text = word
}

inline fun TextView.multiColorText(
    lineSplitLength: Int,
    @ColorRes firstTextColor: Int,
    @ColorRes secondTextColor: Int,
    crossinline onClick: () -> Unit
) {

    val word = SpannableString(this.text.toString())

    word.setSpan(
        ForegroundColorSpan(ContextCompat.getColor(this.context, firstTextColor)),
        0, lineSplitLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    word.setSpan(
        ForegroundColorSpan(ContextCompat.getColor(this.context, secondTextColor)),
        lineSplitLength + 1, word.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    val spanClick = object : ClickableSpan() {
        override fun onClick(widget: View) {
            onClick()
        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.isUnderlineText = false
        }
    }

    word.setSpan(spanClick, lineSplitLength, word.toString().length, 0)
    this.movementMethod = LinkMovementMethod.getInstance()
    this.text = word
}

inline fun EditText.onKeyBoardDonePress(crossinline action: () -> Unit) {
    setOnEditorActionListener { v, actionId, event ->
        if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
            action()
        }

        true
    }
}

fun AppCompatActivity.showToast(msg: String, length: Int = Toast.LENGTH_SHORT) {
    val toast = Toast.makeText(this, msg, length)
    toast.view.apply {
        background.colorFilter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            BlendModeColorFilter(
                ContextCompat.getColor(this@showToast, R.color.title),
                BlendMode.SRC_IN
            )
        } else {
            PorterDuffColorFilter(
                ContextCompat.getColor(this@showToast, R.color.title),
                PorterDuff.Mode.SRC_IN
            )
        }
//        setBackgroundColor(ContextCompat.getColor(this@showToast, R.color.title))
        findViewById<TextView>(android.R.id.message).setTextColor(Color.WHITE)
    }

    toast.show()
}

fun getFromDatabase(context: Context): MessageDbRepository {

    val msgDao = MessageRoomDatabase.getDatabase(context).messageDbDao()

    return MessageDbRepository(msgDao)
}

fun sendSMS(context: Context, isMessageSent: Boolean = false, isClientRequested: Boolean = false) {
    val smsManager = SmsManager.getDefault() as SmsManager
    Log.v(
        "SmsManager_",
        ">>> SmsManger.getDefaultSmsSubscriptionId(): ${SmsManager.getDefaultSmsSubscriptionId()}"
    )

    val DEFAULT_MOBILE_NO =
        getSharedPreference(context).getString(DEFAULT_MOBILE_NO, "9867169318")
    val IS_DEFAULT = getSharedPreference(context).getBoolean(IS_DEFAULT_NO, true)

    BaseAsyncTask(object : BaseAsyncTask.SendSMSFromDb {
        override fun onStarted() {
            val repository = getFromDatabase(context)
            val allMessages = repository.allMessages
            var index = 0
            var mSentCount = 0

            try {
                if (isMessageSent) {
                    updateDeliverStatus(context, isClientRequested)
                } else {
                    allMessages.iterator().forEach { msg: Message ->
                        if (mSentCount < 1 && isClientRequested || !msg.sent) {

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

                            val deliveredIntent =
                                Intent(context, DeliverReceiver::class.java).apply {
                                    putExtra("MESSAGE_DELIVER", msg.id)
                                }
                            val deliveredPendingIntent = PendingIntent.getBroadcast(
                                context, 0, deliveredIntent, PendingIntent.FLAG_UPDATE_CURRENT
                            )

                            val sentPIList = ArrayList<PendingIntent>()
                            val deliveredPIList = ArrayList<PendingIntent>()

                            getSharedPreference(context).edit().putInt("MESSAGE_ID", msg.id)
                                .apply()

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
                            BaseActivity.MESSAGE_SPLIT_COUNT = count
                            getSharedPreference(context).edit()
                                .putInt("MESSAGE_SPLIT_COUNT", count).apply()

                            smsManager.sendMultipartTextMessage(
                                DEFAULT_MOBILE_NO,
                                null,
                                msgListParts,
                                sentPIList,
                                deliveredPIList
                            )

                            Thread.sleep(2 * 1000)

                            if (getSharedPreference(context).getBoolean(
                                    "IS_SENT_ERROR",
                                    false
                                )
                            ) {
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
                sendNotification(
                    SmsSenderApp.mApplication,
                    404,
                    "SMS send error!",
                    "Error -> $e",
                    HomeActivity::class.java
                )
                Log.v("SEND_SMS_Error!...", ">>> Error While Sending SMS... $e")
            }
        }

        override fun onCompleted() {
        }
    }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
}

fun updateDeliverStatus(context: Context, isClientRequested: Boolean = false) {
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
