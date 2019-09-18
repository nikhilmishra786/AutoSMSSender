package com.infomantri.autosms.send.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.infomantri.autosms.send.R
import com.infomantri.autosms.send.base.BaseActivity
import com.infomantri.autosms.send.constants.AppConstants
import com.infomantri.autosms.send.viewmodel.MessageViewModel
import com.infomantri.autosms.send.viewmodel.SubscribersViewModel
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.activity_settings.view.*
import kotlinx.android.synthetic.main.custom_toolbar.*

class SettingsActivity : BaseActivity() {

    private lateinit var mSubscribersViewModel: SubscribersViewModel
    private lateinit var mMessageViewModel: MessageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        init()
        setToolbar()

        mSubscribersViewModel = ViewModelProviders.of(this).get(SubscribersViewModel::class.java)
        mMessageViewModel = ViewModelProviders.of(this).get(MessageViewModel::class.java)
        setOnClickListener()

        mMessageViewModel.getSentMsgCount.observe(this, Observer { msgCount ->
            var count = 0
            msgCount.iterator().forEach {
                count++
            }
            tvTotalMsgSentValue.text = count.toString()
        })
    }

    private fun init() {
        switchWidget1.isChecked = getSharedPreference(this).getBoolean(AppConstants.MOBILE_NO_1, true)
        switchWidget2.isChecked = getSharedPreference(this).getBoolean(AppConstants.MOBILE_NO_2, false)
        tvDefaultActiveMobileNo.text = getSharedPreference(this).getString(DEFAULT_MOBILE_NO, "9867169318")

        getCallsDetails().iterator().forEach {callLog ->
            if (callLog.name == "Nikhil 4g")
            tvMsgFailureValue.text = callLog.name.plus(" ${callLog.number} type: ${callLog.type} ${callLog.duration}")
        }
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

        switchWidget1.setOnCheckedChangeListener{buttonView, isChecked ->

            writeToSharedPref(isChecked, tvDefaultMobileNoValue1.text.toString(), switchWidget1.id)
            Log.v("SHARED_PREF_DEFAULT_NO1", ">>> getSharedPref: ${getSharedPreference(this).getString(DEFAULT_MOBILE_NO,"unknown")}")
        }

        switchWidget2.setOnCheckedChangeListener { buttonView, isChecked ->

            writeToSharedPref(isChecked, tvDefaultMobileNoValue2. tvDefaultMobileNoValue2.text.toString(), switchWidget2.id)
            Log.v("SHARED_PREF_DEFAULT_NO2", ">>> getSharedPref: ${getSharedPreference(this).getString(DEFAULT_MOBILE_NO,"unknown")}")
        }

        val fab: View = findViewById(R.id.fabSaveMobileNo)
        fab.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        toolIvAddAlarm.setOnClickListener {
            val intent = Intent(this, AddAlarmsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        toolIvBack.setOnClickListener{
            finish()
        }

    }

    override fun onResume() {
        super.onResume()
        tvDefaultActiveMobileNo.text = getSharedPreference(this).getString(DEFAULT_MOBILE_NO, "unknown...")
    }

    private fun writeToSharedPref(isChecked: Boolean, mobileNo: String, id: Int) {
        when(id){
            switchWidget2.id -> {
                if(isChecked) {
                    switchWidget1.isChecked = false
                    getSharedPreference(this).edit().putBoolean(AppConstants.MOBILE_NO_2, switchWidget2.isChecked).apply()
                    getSharedPreference(this).edit().putBoolean(AppConstants.MOBILE_NO_1, false).apply()
                    getSharedPreference(this).edit().putString(DEFAULT_MOBILE_NO,mobileNo).apply()
                    tvDefaultActiveMobileNo.text = mobileNo
                    Log.v("DEFAULT_MOBILE_NO",">>> mobile: ${getSharedPreference(this).getString(DEFAULT_MOBILE_NO, "unknown")}")
                }else{
                    switchWidget1.isChecked = true
                    getSharedPreference(this).edit().putBoolean(AppConstants.MOBILE_NO_1, true).apply()
                    getSharedPreference(this).edit().putString(DEFAULT_MOBILE_NO, tvDefaultMobileNoValue1.text.toString()).apply()
                }
            }
            else -> {
                if(isChecked) {
                    switchWidget2.isChecked = false
                    getSharedPreference(this).edit().putBoolean(AppConstants.MOBILE_NO_1, switchWidget1.isChecked).apply()
                    getSharedPreference(this).edit().putBoolean(AppConstants.MOBILE_NO_2, false).apply()
                    getSharedPreference(this).edit().putString(DEFAULT_MOBILE_NO, mobileNo).apply()
                    tvDefaultActiveMobileNo.text = mobileNo
                    Log.v(
                        "NON_DEFAULT_MOBILE_NO",
                        ">>> NoDefault Mobile NO: ${getSharedPreference(this).getString(DEFAULT_MOBILE_NO, "unknown")}"
                    )
                }else {
                    switchWidget2.isChecked = true
                    getSharedPreference(this).edit().putBoolean(AppConstants.MOBILE_NO_2, true).apply()
                    getSharedPreference(this).edit().putString(DEFAULT_MOBILE_NO,tvDefaultMobileNoValue2.text.toString()).apply()
                }
            }
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }


}