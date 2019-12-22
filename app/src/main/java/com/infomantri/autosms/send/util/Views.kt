package com.infomantri.autosms.send.util

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.*
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.util.Patterns
import android.view.KeyEvent
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
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import androidx.preference.PreferenceManager
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.infomantri.autosms.send.R
import com.infomantri.autosms.send.activity.AppSignatureHelper
import com.infomantri.autosms.send.activity.HomeActivity
import com.infomantri.autosms.send.base.BaseActivity
import com.infomantri.autosms.send.base.BaseActivity.Companion.MESSAGE_SPLIT_COUNT
import com.infomantri.autosms.send.constants.AppConstant
import com.infomantri.autosms.send.database.MessageDbRepository
import com.infomantri.autosms.send.database.MessageRoomDatabase
import com.infomantri.autosms.send.receiver.AlarmReceiver
import com.infomantri.autosms.send.receiver.DeliverReceiver
import com.infomantri.autosms.send.receiver.DozeReceiver
import com.infomantri.autosms.send.receiver.SentReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

fun Context.phoneCallToNumber(mobileNumber: String) {
    if (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CALL_PHONE
        )
        == PackageManager.PERMISSION_GRANTED
    ) {
        val callIntent = Intent(Intent.ACTION_CALL, Uri.fromParts("tel", "+91$mobileNumber", null))
        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(callIntent)
    }
}

inline fun <reified T : ViewModel> AppCompatActivity.initViewModel(): T {
    return ViewModelProviders.of(this).get(T::class.java)
}

inline fun <reified T : ViewModel> Fragment.initViewModel(): T {
    return ViewModelProviders.of(this).get(T::class.java)
}

inline fun EditText.imeDoneClick(crossinline func: () -> Unit) {
    setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            func.invoke()
        }
        false
    }
}

fun BaseActivity.showToast(msg: String) {
    android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_SHORT).show()
}

fun AppCompatActivity.showBlendToast(msg: String, length: Int = Toast.LENGTH_SHORT) {
    val toast = Toast.makeText(this, msg, length)
    toast.view.apply {
        background.colorFilter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            BlendModeColorFilter(
                ContextCompat.getColor(this@showBlendToast, R.color.redColorBlendToast),
                BlendMode.SRC_IN
            )
        } else {
            PorterDuffColorFilter(
                ContextCompat.getColor(this@showBlendToast, R.color.redColorBlendToast),
                PorterDuff.Mode.SRC_IN
            )
        }
//        setBackgroundColor(ContextCompat.getColor(this@showToast, R.color.title))
        findViewById<TextView>(android.R.id.message).setTextColor(Color.WHITE)
    }

    toast.show()
}

fun EditText.isValidEmail(): Boolean {
    return !TextUtils.isEmpty(getTrimmedText()) && Patterns.EMAIL_ADDRESS.matcher(getTrimmedText()).matches()
}

inline fun EditText.onKeyBoardDonePress(crossinline action: () -> Unit) {
    setOnEditorActionListener { v, actionId, event ->
        if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
            action()
        }

        true
    }
}

fun getSharedPreference(context: Context): SharedPreferences =
    PreferenceManager.getDefaultSharedPreferences(context)


/**
 *
 * Minimum eight characters, at least one letter and one number:

"^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{8,}$"
Minimum eight characters, at least one letter, one number and one special character:

"^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$"
Minimum eight characters, at least one uppercase letter, one lowercase letter and one number:

"^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d]{8,}$"
Minimum eight characters, at least one uppercase letter, one lowercase letter, one number and one special character:

"^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$"
Minimum eight and maximum 10 characters, at least one uppercase letter, one lowercase letter, one number and one special character:

"^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,10}$"
 *
 *
 */

fun EditText.isNotEmpty() = getTrimmedText().isNotEmpty()

fun EditText.getTrimmedText() = this.text.toString().trim()


fun View.setGone() {
    visibility = View.GONE
}

fun View.setVisible() {
    visibility = View.VISIBLE
}

fun View.setInvisible() {
    visibility = View.INVISIBLE
}

fun Context.addStringToPreference(key: String, value: String?) {
    PreferenceManager.getDefaultSharedPreferences(this).edit()
        .putString(key, value).apply()
}

fun Context.getStringFromPreference(key: String): String? {
    return PreferenceManager.getDefaultSharedPreferences(this).getString(key, null)
}

fun Context.addIntToPreference(key: String, value: Int) {
    PreferenceManager.getDefaultSharedPreferences(this).edit()
        .putInt(key, value).apply()
}

fun Context.getIntFromPreference(key: String): Int? {
    return PreferenceManager.getDefaultSharedPreferences(this).getInt(key, 0)
}

fun Context.addLongToPreference(key: String, value: Long) {
    PreferenceManager.getDefaultSharedPreferences(this).edit()
        .putLong(key, value).apply()
}

fun Context.getLongFromPreference(key: String): Long? {
    return PreferenceManager.getDefaultSharedPreferences(this).getLong(key, 0)
}

fun Context.setBooleanFromPreference(key: String, value: Boolean) {
    PreferenceManager.getDefaultSharedPreferences(this).edit()
        .putBoolean(key, value).apply()
}

fun Context.getBooleanFromPreference(key: String): Boolean? {
    return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(key, false)
}

fun Context.clearBooleanFromPreference(key: String, value: Boolean) {
    PreferenceManager.getDefaultSharedPreferences(this).edit()
        .putBoolean(key, value).clear().apply()
}

fun Context.clearPreference() {
    PreferenceManager.getDefaultSharedPreferences(this).edit().clear().apply()
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

inline fun TextView.multiColorFromToText(
    lineSplitStartLength: Int,
    lineSplitEndLength: Int,
    @ColorRes firstTextColor: Int,
    @ColorRes secondTextColor: Int,
    crossinline onClick: () -> Unit
) {

    val word = SpannableString(this.text.toString())

    word.setSpan(
        ForegroundColorSpan(ContextCompat.getColor(this.context, firstTextColor)),
        0, lineSplitStartLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    word.setSpan(
        ForegroundColorSpan(ContextCompat.getColor(this.context, secondTextColor)),
        lineSplitStartLength + 1, lineSplitEndLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    word.setSpan(
        ForegroundColorSpan(ContextCompat.getColor(this.context, firstTextColor)),
        lineSplitEndLength + 1, word.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
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

    word.setSpan(spanClick, lineSplitStartLength, lineSplitEndLength, 0)
    this.movementMethod = LinkMovementMethod.getInstance()
    this.text = word
}

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

fun Context.startSmsRetriever() {
    val appSignatureHelper = AppSignatureHelper(this)

    val client = SmsRetriever.getClient(this)

    val task = client.startSmsRetriever()

    task.addOnSuccessListener { _ -> Log.d("CodeActivity", "Sms listener started!") }
    task.addOnFailureListener { e ->
        Log.e("CodeActivity", "Failed to start sms retriever: ${e.message}")
    }
}

fun Context.cancelAlarm(requestCode: Int) {

    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(this, AlarmReceiver::class.java)
    val pendingIntent =
        PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    alarmManager.cancel(pendingIntent)
    Log.v("NEXT_ALARM", ">>> Next Alarm : ${alarmManager.nextAlarmClock}")
}

fun sendNotification(
    context: Context, id: Int, title: String,
    subTitle: String,
    activity: Class<*>,
    channelId: String,
    channelName: String
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
        NotificationCompat.Builder(context, channelId)
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
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Text"
        }
        // As we are already created Notification Channel in our Application class onCreate method, so writing 2 times will not create notifications, so write only once.
//        notificationManager?.createNotificationChannel(channel)
    }

    notificationManager?.notify(id, notificationBuilder.build())
}

fun getFromDatabase(context: Context): MessageDbRepository {

    val msgDao = MessageRoomDatabase.getDatabase(context).messageDbDao()

    return MessageDbRepository(msgDao)
}

fun requestSendSMS(context: Context, messageId: Int, message: String) {
    val DEFAULT_MOBILE_NO =
        context.getStringFromPreference(AppConstant.DEFAULT_MOBILE_NO)

    val smsManager = SmsManager.getDefault() as SmsManager
    val sentIntent = Intent(context, SentReceiver::class.java).apply {
        putExtra(AppConstant.MESSAGE_ID, messageId)
        putExtra(
            AppConstant.Reminder.TIME_STAMP,
            System.currentTimeMillis()
        )
        putExtra(
            AppConstant.Reminder.REMINDER_ID,
            System.currentTimeMillis().toInt()
        )
        putExtra(AppConstant.Reminder.TITLE, "Message Sent Successfully...")
    }
    sentIntent.action = AppConstant.MESSAGE_SENT

    val sentPendingIntent = PendingIntent.getBroadcast(
        context,
        System.currentTimeMillis().toInt(),
        sentIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    val deliveredIntent =
        Intent(context, DeliverReceiver::class.java).apply {
            putExtra(AppConstant.MESSAGE_ID, messageId)
        }
    val deliveredPendingIntent = PendingIntent.getBroadcast(
        context,
        System.currentTimeMillis().toInt(),
        deliveredIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    val sentPIList = ArrayList<PendingIntent>()
    val deliveredPIList = ArrayList<PendingIntent>()


    val msgListParts = smsManager.divideMessage(message)
    var count = 0

    repeat(msgListParts.size) {
        sentPIList.add(count, sentPendingIntent)
        deliveredPIList.add(count, deliveredPendingIntent)
        count++
    }
    MESSAGE_SPLIT_COUNT = count

    smsManager.sendMultipartTextMessage(
        DEFAULT_MOBILE_NO,
        null,
        msgListParts,
        sentPIList,
        deliveredPIList
    )
}

fun sendSMS(
    context: Context
) {
    val mJob = Job()
    CoroutineScope(Dispatchers.Default + mJob).launch {
        Log.v(
            "SmsManager",
            ">>> SmsManger.getDefaultSmsSubscriptionId(): ${SmsManager.getDefaultSmsSubscriptionId()}"
        )

        val repository = getFromDatabase(context)
        val allMessages = repository.allMessages
        val sentMessages = repository.getSentMessages
        var index = 0
        try {

            if (allMessages.size == sentMessages.size) {
                allMessages.iterator().withIndex().forEach { msg ->
                    resetSentMessages(context)
                    sendSMS(context)
                }
            } else {
                allMessages.iterator().withIndex().forEach { msg ->
                    if (!msg.value.sent && index < 1) {
                        requestSendSMS(
                            context = context,
                            messageId = msg.value.id,
                            message = msg.value.message
                        )
                        index++
                        return@launch
                    }
                }
            }

        } catch (e: Exception) {
            repository.updateMessage(allMessages[index].apply {
                isFailed = true
            })
            sendNotification(
                context,
                System.currentTimeMillis().toInt(),
                "SMS send error!",
                "Error -> $e",
                HomeActivity::class.java,
                AppConstant.Notification.Channel.MESSAGE_CHANNEL_ID,
                "Error Channel"
            )
            Log.v("SEND_SMS_Error!...", ">>> Error While Sending SMS... $e")
        }
    }
}

fun resetSentMessages(context: Context) {
    val mJob = Job()
    Log.v("REST_SENT_MESSAGES", ">>> REST_SENT_MESSAGES Running CoroutineScope ...")

    CoroutineScope(Dispatchers.Default + mJob).launch {

        val msgDao = MessageRoomDatabase.getDatabase(context).messageDbDao()
        val repository = MessageDbRepository(msgDao)

        val allMessages = repository.allMessages

        allMessages.iterator().forEach { message ->
            message.sent = false
            repository.updateMessage(message)
            Log.v(
                "REST_SENT_MESSAGES",
                ">>> -> ${message.message} Status: ${message.sent} -> id: ${message.id}"
            )
        }
    }
}

fun updateStatusToPending(context: Context, msgId: Int) {
    Log.v("MSG_DELIVERED", ">>> Msg updateStatusToPending Running AsyncTask onStarted()...")

    val mJob = Job()
    CoroutineScope(Dispatchers.Default + mJob).launch {

        Log.v("MSG_DECODED_SHARED_PREF", ">>> Msg Id is received  Id: $msgId")
        val msgDao = MessageRoomDatabase.getDatabase(context).messageDbDao()
        val repository = MessageDbRepository(msgDao, id = msgId)

        val message = repository.messageById
        Log.v("Updated_MSG", ">>> Msg: $message")

        message?.let {
            if (msgId != -1) {
                message.sent = false
                message.timeStamp = System.currentTimeMillis()
                message.isFailed = false
            }
            Log.v("MSG_STATUS_UPDATED", ">>> Msg sent = false : sent: ${message.sent}")
            repository.updateMessage(message)

            Log.v(
                "MSG_DELIVERY_STATUS",
                ">>> onReceive() Msg: -> ${message.message} Status: ${message.sent} -> id: ${message.id}"
            )
        }
    }
}

fun Context.setAlarm(
    calendar: Calendar,
    requestCode: Int, title: String, isAddMsgAlarm: Boolean
) {
    Log.v(
        "SET_ALARM_TIME_STAMP",
        ">>> SET_ALARM_TIME_STAMP : $requestCode"
    )
    val notifyIntent = Intent(this, AlarmReceiver::class.java).apply {
        putExtra(AppConstant.Reminder.TIME_STAMP, calendar.timeInMillis)
        putExtra(AppConstant.Reminder.REMINDER_ID, requestCode)
        putExtra(AppConstant.Reminder.TITLE, title)
        if (isAddMsgAlarm) {
            action = AppConstant.Intent.ACTION_MESSAGE_ALARM
            putExtra(AppConstant.Intent.PHONE_CALL_ALARM, false)
        } else {
            action = AppConstant.Intent.ACTION_PHONE_CALL_ALARM
            putExtra(AppConstant.Intent.PHONE_CALL_ALARM, true)
        }
    }

    val pendingIntent = PendingIntent.getBroadcast(
        this,
        requestCode,
        notifyIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )
    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

    if (Date().after(calendar.time)) {
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            24 * 60 * 60 * 1000,
            pendingIntent
        )
    } else {
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            24 * 60 * 60 * 1000,
            pendingIntent
        )
    }

    when (calendar.get(Calendar.HOUR_OF_DAY)) {
        in 0..6 -> setDozeModeAlarm(calendar, requestCode, title)
    }

    Log.v(
        "SET_ALARM",
        ">>> $title: Time: ${calendar.formatTime()} Date: ${calendar.formatDate()}"
    )
}

fun Context.setDozeModeAlarm(
    calendar: Calendar,
    requestCode: Int, title: String
) {
    calendar.apply {
        set(Calendar.SECOND, 0)
    }

    val notifyIntent = Intent(this, DozeReceiver::class.java).apply {
        putExtra(AppConstant.Reminder.TIME_STAMP, calendar.timeInMillis)
        putExtra(AppConstant.Reminder.REMINDER_ID, requestCode)
        putExtra(AppConstant.Reminder.TITLE, title)
        action = AppConstant.Intent.ACTION_DOZE_MODE_ALARM
    }
    notifyIntent.action = "action.doze.alarm"

    val pendingIntent = PendingIntent.getBroadcast(
        this,
        requestCode,
        notifyIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )
    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

    if (Date().after(calendar.time)) {
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    } else {
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }
    Log.v(
        "SET_ALARM",
        ">>> $title: Time: ${calendar.formatTime()} Date: ${calendar.formatDate()}"
    )
}

fun getAlarmTitle(timeStamp: Long): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timeStamp

    return when (calendar.get(Calendar.HOUR_OF_DAY)) {

        in 0..3 -> "Mid Night"
        in 4..11 -> "Good Morning"
        in 12..15 -> "Good After Noon"
        in 16..20 -> "Good Evening"
        in 21..23 -> "Good Night"
        else -> "Good Day"
    }
}

fun Calendar.formatTime(): String? {
    val simpleDateFormatter = SimpleDateFormat("hh:mm a", Locale.US)
    return simpleDateFormatter.format(time)
}

fun Calendar.formatDate(): String? {
    val simpleDateFormatter = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.US)
    return simpleDateFormatter.format(time)
}

fun Long.formatTime(): String? {
    val simpleDateFormatter = SimpleDateFormat("hh:mm a", Locale.US)
    return simpleDateFormatter.format(this)
}