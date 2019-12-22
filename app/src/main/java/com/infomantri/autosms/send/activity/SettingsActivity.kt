package com.infomantri.autosms.send.activity

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import com.infomantri.autosms.send.R
import com.infomantri.autosms.send.base.BaseActivity
import com.infomantri.autosms.send.constants.AppConstant
import com.infomantri.autosms.send.receiver.ConfirmationSmsReceiver
import com.infomantri.autosms.send.util.*
import com.infomantri.autosms.send.viewmodel.MessageViewModel
import com.infomantri.autosms.send.viewmodel.SubscribersViewModel
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.activity_settings.view.*
import kotlinx.android.synthetic.main.custom_toolbar.*
import java.util.*

class SettingsActivity : BaseActivity() {

    private lateinit var mSubscribersViewModel: SubscribersViewModel
    private lateinit var mMessageViewModel: MessageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        init()
        setToolbar()

        mSubscribersViewModel = initViewModel()
        mMessageViewModel = initViewModel()
        setOnClickListener()

        mMessageViewModel.allMessages.observe(this, Observer { totalMessages ->
            tvTotalMessagesValue.text = totalMessages.size.toString()
        })

        mMessageViewModel.getSentMessages.observe(this, Observer { sentMessages ->
            tvMsgSuccessValue.text = sentMessages.size.toString()
        })

        mMessageViewModel.getFailedMessages.observe(this, Observer { failedMessages ->
            tvMsgFailureValue.text = failedMessages.size.toString()
        })
    }

    private fun init() {
        tvConfirmationSms.text =
            getLongFromPreference(AppConstant.CONFIRMATION_SMS_ALARM)?.formatTime() ?: "null"
        swAutoStartup.isChecked = getBooleanFromPreference(AppConstant.IS_AUTO_STARTUP) ?: false
        switchWidget1.isChecked =
            getBooleanFromPreference(AppConstant.MOBILE_NO_1) ?: true
        switchWidget2.isChecked =
            getBooleanFromPreference(AppConstant.MOBILE_NO_2) ?: false
        tvDefaultActiveMobileNo.text =
            getStringFromPreference(AppConstant.DEFAULT_MOBILE_NO) ?: AppConstant.DEBUG_MOBILE_NO
    }

    private fun setToolbar() {
        toolIvHome.visibility = View.GONE
        toolIvBack.visibility = View.VISIBLE
        toolbar.setToolbar(
            true,
            titleColor = R.color.orange,
            centerTitle = "Setting",
            bgColor = R.color.white
        )
    }

    private fun setOnClickListener() {

        switchWidget1.setOnCheckedChangeListener { buttonView, isChecked ->

            writeToSharedPref(isChecked, tvDefaultMobileNoValue1.text.toString(), switchWidget1.id)
            Log.v(
                "SHARED_PREF_DEFAULT_NO2",
                ">>> getSharedPref: ${getStringFromPreference(AppConstant.DEFAULT_MOBILE_NO)
                    ?: "unknown...1"}"
            )
        }

        switchWidget2.setOnCheckedChangeListener { buttonView, isChecked ->

            writeToSharedPref(
                isChecked,
                tvDefaultMobileNoValue2.tvDefaultMobileNoValue2.text.toString(),
                switchWidget2.id
            )
            Log.v(
                "SHARED_PREF_DEFAULT_NO2",
                ">>> getSharedPref: ${getStringFromPreference(AppConstant.DEFAULT_MOBILE_NO)
                    ?: "unknown...2"}"
            )
        }

        swAutoStartup.setOnCheckedChangeListener { buttonView, isChecked ->
            setBooleanFromPreference(AppConstant.IS_AUTO_STARTUP, isChecked)
        }

        val fab: View = findViewById(R.id.fabSaveMobileNo)
        fab.setOnClickListener {
            startActivityFromLeft(HomeActivity::class.java)
        }

        toolIvAddAlarm.setOnClickListener {
            startActivityFromLeft(AddAlarmsActivity::class.java)
        }

        toolIvBack.setOnClickListener {
            finish()
        }

        tvConfirmationSms.setOnClickListener {
            setConfirmationSmsAlarm()
        }

    }

    override fun onResume() {
        super.onResume()
        tvDefaultActiveMobileNo.text =
            getStringFromPreference(AppConstant.DEFAULT_MOBILE_NO) ?: AppConstant.DEBUG_MOBILE_NO
    }

    private fun writeToSharedPref(isChecked: Boolean, mobileNo: String, id: Int) {
        when (id) {
            switchWidget2.id -> {
                if (isChecked) {
                    switchWidget1.isChecked = false
                    getSharedPreference(this).edit()
                        .putBoolean(AppConstant.MOBILE_NO_2, switchWidget2.isChecked).apply()
                    getSharedPreference(this).edit().putBoolean(AppConstant.MOBILE_NO_1, false)
                        .apply()
                    addStringToPreference(AppConstant.DEFAULT_MOBILE_NO, mobileNo)
                    tvDefaultActiveMobileNo.text = mobileNo
                    Log.v(
                        "DEFAULT_MOBILE_NO",
                        ">>> mobile: ${getSharedPreference(this).getString(
                            mobileNo,
                            "unknown"
                        )}"
                    )
                } else {
                    switchWidget1.isChecked = true
                    setBooleanFromPreference(AppConstant.MOBILE_NO_1, true)
                    addStringToPreference(
                        AppConstant.DEFAULT_MOBILE_NO,
                        tvDefaultMobileNoValue1.text.toString()
                    )
                }
            }
            else -> {
                if (isChecked) {
                    switchWidget2.isChecked = false
                    getSharedPreference(this).edit()
                        .putBoolean(AppConstant.MOBILE_NO_1, switchWidget1.isChecked).apply()
                    getSharedPreference(this).edit().putBoolean(AppConstant.MOBILE_NO_2, false)
                        .apply()
                    addStringToPreference(AppConstant.DEFAULT_MOBILE_NO, mobileNo)
                    tvDefaultActiveMobileNo.text = mobileNo
                    Log.v(
                        "NON_DEFAULT_MOBILE_NO",
                        ">>> NoDefault Mobile NO: ${getStringFromPreference(AppConstant.DEFAULT_MOBILE_NO)
                            ?: "unknown...Switch2"}"
                    )
                } else {
                    switchWidget2.isChecked = true
                    setBooleanFromPreference(AppConstant.MOBILE_NO_2, true)
                    addStringToPreference(
                        AppConstant.DEFAULT_MOBILE_NO,
                        tvDefaultMobileNoValue2.text.toString()
                    )
                }
            }
        }
    }

    private fun setConfirmationSmsAlarm() {
        cancelAlarm(getLongFromPreference(AppConstant.CONFIRMATION_SMS_ALARM)?.toInt() ?: 0 * 1000)

        val addAlarmCalendar = Calendar.getInstance()

        addAlarmCalendar.apply {
            set(Calendar.HOUR_OF_DAY, 22)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        setAlarm(
            calendar = addAlarmCalendar,
            requestCode = ((addAlarmCalendar.timeInMillis).toInt()),
            title = "Confirmation Message..." + getAlarmTitle(addAlarmCalendar.timeInMillis)
        )
    }

    fun setAlarm(
        calendar: Calendar,
        requestCode: Int, title: String
    ) {
        Log.v(
            "SET_ALARM_TIME_STAMP",
            ">>> SET_ALARM_TIME_STAMP : $requestCode"
        )
        val notifyIntent = Intent(this, ConfirmationSmsReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            requestCode,
            notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            24 * 60 * 60 * 1000,
            pendingIntent
        )
        addLongToPreference(AppConstant.CONFIRMATION_SMS_ALARM, calendar.timeInMillis)

        Log.v(
            "SET_ALARM",
            ">>> Confirmation SMS -> $title: Time: ${calendar.formatTime()} Date: ${calendar.formatDate()}"
        )
    }
}