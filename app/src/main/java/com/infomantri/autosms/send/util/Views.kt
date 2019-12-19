package com.infomantri.autosms.send.util

import android.Manifest
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
import android.os.AsyncTask
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.telephony.SmsManager
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import android.util.Patterns
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.infomantri.autosms.send.R
import com.infomantri.autosms.send.activity.HomeActivity
import com.infomantri.autosms.send.asynctask.BaseAsyncTask
import com.infomantri.autosms.send.base.BaseActivity
import com.infomantri.autosms.send.base.BaseActivity.Companion.MESSAGE_SPLIT_COUNT
import com.infomantri.autosms.send.constants.AppConstant
import com.infomantri.autosms.send.constants.AppConstant.DEFAULT_MOBILE_NO
import com.infomantri.autosms.send.database.MessageDbRepository
import com.infomantri.autosms.send.database.MessageRoomDatabase
import com.infomantri.autosms.send.receiver.DeliverReceiver
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
                ContextCompat.getColor(this@showBlendToast, R.color.colorBlendToast),
                BlendMode.SRC_IN
            )
        } else {
            PorterDuffColorFilter(
                ContextCompat.getColor(this@showBlendToast, R.color.colorBlendToast),
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
fun EditText.isValidPassword(): Boolean {

    val containsLowerChar = getTrimmedText().matches(Regex(".*[a-z].*"))
    val containsUpperChar = getTrimmedText().matches(Regex(".*[A-Z].*"))
    val containsDigit = getTrimmedText().matches(Regex(".*[0-9].*"))
    val containSpecialChar = getTrimmedText().matches(Regex(".*[@${'$'}!%*?&#].*"))

    return getTrimmedText().length >= 8 &&
            ((containsLowerChar && containSpecialChar && containsDigit) ||
                    (containsLowerChar && containSpecialChar && containsUpperChar) ||
                    (containsLowerChar && containsDigit && containsUpperChar))


//    return getTrimmedText().matches(Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@${'$'}!%*?&])[A-Za-z\\d@${'$'}!%*?&]{8,}$".trimIndent()))
}

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

fun Context.setBooleanFromPreference(key: String, value: Boolean) {
    PreferenceManager.getDefaultSharedPreferences(this).edit()
        .putBoolean(key, value).apply()
}

fun Context.getBooleanFromPreference(key: String): Boolean? {
    return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(key, false)
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
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Text"
        }
        notificationManager?.createNotificationChannel(channel)
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
            "SmsManager_",
            ">>> SmsManger.getDefaultSmsSubscriptionId(): ${SmsManager.getDefaultSmsSubscriptionId()}"
        )


        val repository = getFromDatabase(context)
        val allMessages = repository.allMessages
        var index = 0
        var mSentCount = 0

        val sentMessages = repository.getSentMessages

        try {

            if (allMessages.size == sentMessages.size) {
                allMessages.iterator().withIndex().forEach { msg ->
                    resetSentMessages(context)
                    sendSMS(context)
                }
            } else {
                allMessages.iterator().forEach { msg ->
                    if (!msg.sent && mSentCount < 1) {
                        requestSendSMS(context = context, messageId = msg.id, message = msg.message)

                        mSentCount++
                        if (mSentCount == 1)
                            return@forEach
                    }
                    index++
                }
            }

        } catch (e: Exception) {
            allMessages[index].apply {
            }
            sendNotification(
                context,
                404,
                "SMS send error!",
                "Error -> $e",
                HomeActivity::class.java,
                "ErrorChannel",
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