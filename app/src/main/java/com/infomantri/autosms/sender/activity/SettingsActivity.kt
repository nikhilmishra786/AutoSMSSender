package com.infomantri.autosms.sender.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.infomantri.autosms.sender.R
import com.infomantri.autosms.sender.asynctask.BaseAsyncTask
import com.infomantri.autosms.sender.base.BaseActivity
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        ivSettingEdit.setOnClickListener{
            startActivity(Intent(this, AddNewMessages::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
    }

    var defaultNo = ""

    private fun dispData() {
        BaseAsyncTask(object : BaseAsyncTask.SendSMSFromDb{

            override fun onStarted() {
                val defaultMobileNo = getFromDatabase(application).defaultMobileNo
                defaultNo =  defaultMobileNo.first().mobileNo
                Log.v("GET_DEFAULT_NO", ">>> settings default mobile No is: ${defaultMobileNo.first().mobileNo}")
            }

            override fun onCompleted() {
                toast("Mobile number changed successfully...")
            }
        })
    }

    private fun toast(msg: String) {
        Toast.makeText(this,msg, Toast.LENGTH_SHORT).show()
    }


}