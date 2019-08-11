package com.infomantri.autosms.sender

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.infomantri.autosms.sender.asynctask.BaseAsyncTask
import com.infomantri.autosms.sender.base.BaseActivity
import com.infomantri.autosms.sender.database.MessageDbRepository
import com.infomantri.autosms.sender.database.MessageRoomDatabase
import com.infomantri.autosms.sender.database.SubscribersRepository
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        dispData()
        ivSettingEdit.setOnClickListener{
            startActivity(Intent(this, AddNewMessages::class.java))
        }
    }

    private fun dispData() {

        BaseAsyncTask(object : BaseAsyncTask.SendSMSFromDb{

            val isCompleted = false
            override fun onStarted() {
                val subscribersDao = MessageRoomDatabase.getDatabase(application).subscribersDao()
                val repository = SubscribersRepository(subscribersDao)
                val defaultMobileNo = repository.defaultMobileNo
                tvDefaultMobileNoValue.text = defaultMobileNo.first().mobileNo
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