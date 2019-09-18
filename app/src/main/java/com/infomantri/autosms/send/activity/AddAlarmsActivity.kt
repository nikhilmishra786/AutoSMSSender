package com.infomantri.autosms.send.activity

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.infomantri.autosms.send.R
import com.infomantri.autosms.send.TimerFragment
import com.infomantri.autosms.send.adapter.AddAlarmsListAdapter
import com.infomantri.autosms.send.adapter.SwipeToDeleteCallback
import com.infomantri.autosms.send.asynctask.BaseAsyncTask
import com.infomantri.autosms.send.base.BaseActivity
import com.infomantri.autosms.send.database.*
import com.infomantri.autosms.send.receiver.AlarmReceiver
import com.infomantri.autosms.send.util.formatDate
import com.infomantri.autosms.send.viewmodel.AddAlarmViewModel
import kotlinx.android.synthetic.main.activity_add_alarms.*
import kotlinx.android.synthetic.main.custom_toolbar.*
import java.text.SimpleDateFormat
import java.util.*

class AddAlarmsActivity : BaseActivity() {

    private val addAlarmCalendar by lazy {
        Calendar.getInstance().apply {
            set(Calendar.SECOND, 0)
        }
    }

    private lateinit var mAlarmView: AddAlarmViewModel

    var alarmData =
        AddAlarm(System.currentTimeMillis(), id = -1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_alarms)

        mAlarmView = ViewModelProviders.of(this).get(AddAlarmViewModel::class.java)

        setToolbar()
        setRecyclerView()
        setOnClickListener()
    }

    private fun setToolbar() {
        toolbar.setToolbar(
            false,
            titleColor = R.color.orange,
            centerTitle = "Add Alarm",
            bgColor = R.color.white
        )
    }

    private fun setRecyclerView() {
        val adapter = AddAlarmsListAdapter({}, deleteAlarm = { alarmId ->
            if (alarmId != -1)
                deleteMsgById(alarmId)
            else
                Toast.makeText(this, "Error while deleting", Toast.LENGTH_SHORT).show()
        })

        add_alarm_recyclerview.adapter = adapter
        add_alarm_recyclerview.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )
        add_alarm_recyclerview.scrollToPosition(0)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        add_alarm_recyclerview.layoutManager = linearLayoutManager

        val swipeHandler = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                Log.v("onSwiped", ">>> postion: -> ${viewHolder.adapterPosition}")
                adapter.removeAt(viewHolder.adapterPosition)
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(add_alarm_recyclerview)

        mAlarmView.allAlarms.observe(this, androidx.lifecycle.Observer { alarmList ->
            adapter.submitList(alarmList)
        })
    }

    private fun setOnClickListener() {

//        swRepeatAlarm.setOnCheckedChangeListener { compoundButton, isSelected ->
//            repeatAlarm = isSelected
//        }

        toolIvAddAlarm.setOnClickListener {
            showTimePicker()
        }

        toolIvSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        toolIvHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

    }

    private fun showTimePicker() {


        Calendar.getInstance().apply {
            val timeFragment =
                TimerFragment.instance(get(Calendar.HOUR_OF_DAY), get(Calendar.MINUTE))

            timeFragment.setTimeChangeListener(mOnTimeChangeListener)
            timeFragment.show(supportFragmentManager, "time")
        }
    }

    private val mOnTimeChangeListener = object : TimerFragment.OnTimeSelected {
        override fun selectedTime(hour: Int, minute: Int, unit: String) {

            addAlarmCalendar.apply {
                set(Calendar.HOUR_OF_DAY, if (unit == "PM") hour + 12 else hour)
                set(Calendar.MINUTE, minute)
            }
            mAlarmView.insert(AddAlarm(addAlarmCalendar.timeInMillis))
            setAlarm(
                addAlarmCalendar,
                ((addAlarmCalendar.timeInMillis).toInt() % 10 * 1000),
                getAlarmTitle(addAlarmCalendar.timeInMillis)
            )
            Log.v("ALARM_TITLE", ">>> Alarm Title: ${getAlarmTitle(addAlarmCalendar.timeInMillis)}")
        }
    }

    private fun setAlarm(calendar: Calendar, requestCode: Int, title: String = "") {

        val notifyIntent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("reminder_timestamp", calendar.timeInMillis)
            putExtra("reminder_id", requestCode)
            putExtra("reminder_title", title)
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
        Log.v("SET_ALARM", ">>> $title: ${calendar.formatDate()}")
    }

    private fun deleteMsgById(msgId: Int) {
        Log.v("DELETE_MSG", ">>> Inside deleting Msg... <<<")
        BaseAsyncTask(object : BaseAsyncTask.SendSMSFromDb {
            override fun onStarted() {
                Log.v("DELETE_MSG", ">>> deleting Msg...")
                val addAlarmDao = MessageRoomDatabase.getDatabase(application).addAlarmDao()
                val repository = AddAlarmRepository(addAlarmDao, msgId)
                val alarmToDelete = repository.alarmById
                alarmData = alarmToDelete
                repository.deleteAlarm(alarmToDelete)
                cancelAlarm((alarmToDelete.alarmTimeStamp).toInt() % 10 * 1000)
            }

            override fun onCompleted() {
                if (alarmData.id != -1) {
                    "Alarm deleted successfully".showSnackbar(restoreData = {
                        mAlarmView.insert(
                            alarmData
                        )
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = alarmData.alarmTimeStamp
                        setAlarm(
                            calendar = calendar,
                            requestCode = ((alarmData.alarmTimeStamp).toInt() % 10 * 1000),
                            title = getAlarmTitle(alarmData.alarmTimeStamp)
                        )
                    })
                } else {
                    "Error while deleting alarm".showSnackbar { }
                }
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    private fun cancelAlarm(requestCode: Int) {

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.cancel(pendingIntent)
        Log.v("NEXT_ALARM", ">>> Next Alarm : ${alarmManager.nextAlarmClock}")
    }

    private fun getAlarmTitle(timeStamp: Long): String {
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

}