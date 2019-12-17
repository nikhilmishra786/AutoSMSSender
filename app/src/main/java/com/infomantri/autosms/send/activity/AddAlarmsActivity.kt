package com.infomantri.autosms.send.activity

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.infomantri.autosms.send.R
import com.infomantri.autosms.send.TimerFragment
import com.infomantri.autosms.send.adapter.AddAlarmsListAdapter
import com.infomantri.autosms.send.adapter.PhoneCallListAdapter
import com.infomantri.autosms.send.adapter.SwipeToDeleteCallback
import com.infomantri.autosms.send.asynctask.BaseAsyncTask
import com.infomantri.autosms.send.base.BaseActivity
import com.infomantri.autosms.send.constants.AppConstant
import com.infomantri.autosms.send.database.*
import com.infomantri.autosms.send.receiver.AlarmReceiver
import com.infomantri.autosms.send.receiver.DozeReceiver
import com.infomantri.autosms.send.util.formatDate
import com.infomantri.autosms.send.util.formatTime
import com.infomantri.autosms.send.util.initViewModel
import com.infomantri.autosms.send.util.showBlendToast
import com.infomantri.autosms.send.viewmodel.AddAlarmViewModel
import com.infomantri.autosms.send.viewmodel.PhoneCallViewModel
import kotlinx.android.synthetic.main.activity_add_alarms.*
import kotlinx.android.synthetic.main.custom_toolbar.*
import java.util.*

class AddAlarmsActivity : BaseActivity() {

    private val addAlarmCalendar by lazy {
        Calendar.getInstance().apply {
            set(Calendar.SECOND, 0)
        }
    }

    private var repeatAlarm: Boolean = false
    private lateinit var mAlarmView: AddAlarmViewModel
    private lateinit var mPhoneCallViewModel: PhoneCallViewModel

    var alarmData =
        AddAlarm(System.currentTimeMillis(), id = -1)
    var phoneCallAlarmData = PhoneCallAlarm(System.currentTimeMillis(), id = -1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_alarms)

        mPhoneCallViewModel = initViewModel()
        mAlarmView = initViewModel()

        setToolbar()
        setRecyclerView()
        PhoneCallRecycler()
        setOnClickListener()
        checkForCallPermission()
        setDozeModeAlarm()
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
        val adapter = AddAlarmsListAdapter(repeatAlarm = { repeatAlarm, timeStamp, id ->
            //            repeatAlarm.toggleAlarm(timeStamp, id)
        }, deleteAlarm = { alarmId ->
            if (alarmId != -1)
                deleteAlarmById(alarmId)
            else
                showBlendToast("Error while deleting", Toast.LENGTH_LONG)
        })

        add_alarm_recyclerview.isNestedScrollingEnabled = false
        add_alarm_recyclerview.adapter = adapter
        add_alarm_recyclerview.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )

//        add_alarm_recyclerview.scrollToPosition(0)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        linearLayoutManager.isSmoothScrollbarEnabled = true
        add_alarm_recyclerview.layoutManager = linearLayoutManager

        val swipeHandler = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                Log.v("onSwiped", ">>> position: -> ${viewHolder.adapterPosition}")
                adapter.removeAt(viewHolder.adapterPosition)
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(add_alarm_recyclerview)

        mAlarmView.allAlarms.observe(this, androidx.lifecycle.Observer { alarmList ->
            var handler: Handler?
            val handlerThread = HandlerThread(AppConstant.Handler.ADD_ALARM)
            handlerThread.also {
                it.start()
                handler = Handler(it.looper)
            }
            handler?.post {
                adapter.submitList(alarmList)
            }
        })
    }

    private fun PhoneCallRecycler() {
        val phoneCallAdapter = PhoneCallListAdapter(repeatAlarm = { repeatAlarm, timeStamp, id -> },
            deleteAlarm = { alarmId ->
                if (alarmId != -1) {
                    var handler: Handler?
                    val handlerThread = HandlerThread(AppConstant.Handler.DELETE_ALARM)
                    handlerThread.also {
                        it.start()
                        handler = Handler(it.looper)
                    }
                    handler?.post {
                        deletePhoneCallAlarm(alarmId)
                    }
                } else
                    showBlendToast("Error while deleting", Toast.LENGTH_LONG)
            })

        rvPhoneCallAlarm.isNestedScrollingEnabled = false
        rvPhoneCallAlarm.adapter = phoneCallAdapter
        rvPhoneCallAlarm.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )

        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        linearLayoutManager.isSmoothScrollbarEnabled = true
        rvPhoneCallAlarm.layoutManager = linearLayoutManager

        val swipeHandler = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                Log.v("onSwiped", ">>> position: -> ${viewHolder.adapterPosition}")
                phoneCallAdapter.removeAt(viewHolder.adapterPosition)
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(rvPhoneCallAlarm)

        mPhoneCallViewModel.allPhoneCallAlarms.observe(
            this,
            androidx.lifecycle.Observer { alarmList ->
                var handler: Handler?
                val handlerThread = HandlerThread(AppConstant.Handler.PHONE_CALL)
                handlerThread.also {
                    it.start()
                    handler = Handler(it.looper)
                }
                handler?.post {
                    phoneCallAdapter.submitList(alarmList)
                }
            })
    }

    private fun setOnClickListener() {

//        swRepeatAlarm.setOnCheckedChangeListener { compoundButton, isSelected ->
//            repeatAlarm = isSelected
//        }

        toolIvAddAlarm.setOnClickListener {
            showTimePicker(false)
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

        fabAddMsgAlarm.setOnClickListener {
            showTimePicker(true)
        }

    }

    private fun setDozeModeAlarm() {
        val morningAlarm = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 4)
            set(Calendar.MINUTE, 29)
            set(Calendar.SECOND, 0)
        }
        val notifyIntent = Intent(this, DozeReceiver::class.java).apply {
            putExtra(AppConstant.Reminder.TIME_STAMP, morningAlarm.timeInMillis)
            putExtra(AppConstant.Reminder.REMINDER_ID, System.currentTimeMillis().toInt() * 1000)
            putExtra(AppConstant.Reminder.TITLE, title)
        }
        notifyIntent.action = "action.doze.alarm"

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            System.currentTimeMillis().toInt() * 1000,
            notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Date().after(morningAlarm.time)) {
            morningAlarm.add(Calendar.DAY_OF_MONTH, 1)
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                morningAlarm.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                morningAlarm.timeInMillis,
                pendingIntent
            )
        }
        Log.v(
            "SET_ALARM",
            ">>> $title: Time: ${morningAlarm.formatTime()} Date: ${morningAlarm.formatDate()}"
        )
    }

    private fun showTimePicker(isAddMsgAlarm: Boolean) {

        Calendar.getInstance().apply {
            val timeFragment =
                TimerFragment.instance(get(Calendar.HOUR_OF_DAY), get(Calendar.MINUTE))

            if (isAddMsgAlarm) {
                timeFragment.setTimeChangeListener(mOnTimeChangeListener)
            } else {
                timeFragment.setTimeChangeListener(mOnPhoneCallTimeChangeListener)
            }
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
                calendar = addAlarmCalendar,
                requestCode = ((addAlarmCalendar.timeInMillis).toInt() * 1000),
                title = getAlarmTitle(alarmData.alarmTimeStamp),
                isAddMsgAlarm = true
            )

//            setRepeatingAlarm(
//                addAlarmCalendar,
//                ((addAlarmCalendar.timeInMillis).toInt() % 10 * 1000),
//                getAlarmTitle(addAlarmCalendar.timeInMillis)
//            )
//            showBlendToast("Alarm added at ${addAlarmCalendar.formatDateToTime()}")
            Log.v("ALARM_TITLE", ">>> Alarm Title: ${getAlarmTitle(addAlarmCalendar.timeInMillis)}")
        }
    }

    private val mOnPhoneCallTimeChangeListener = object : TimerFragment.OnTimeSelected {
        override fun selectedTime(hour: Int, minute: Int, unit: String) {

            addAlarmCalendar.apply {
                set(Calendar.HOUR_OF_DAY, if (unit == "PM") hour + 12 else hour)
                set(Calendar.MINUTE, minute)
            }
            mPhoneCallViewModel.insertPhoneCallAlarm(PhoneCallAlarm(addAlarmCalendar.timeInMillis))
            setAlarm(
                calendar = addAlarmCalendar,
                requestCode = ((addAlarmCalendar.timeInMillis).toInt() / 1000),
                title = getAlarmTitle(phoneCallAlarmData.alarmTimeStamp),
                isAddMsgAlarm = false
            )
//            setRepeatingAlarm(
//                addAlarmCalendar,
//                ((addAlarmCalendar.timeInMillis).toInt() % 10 * 1000),
//                getAlarmTitle(addAlarmCalendar.timeInMillis)
//            )
//            showBlendToast("Alarm added at ${addAlarmCalendar.formatDateToTime()}")
            Log.v("ALARM_TITLE", ">>> Alarm Title: ${getAlarmTitle(addAlarmCalendar.timeInMillis)}")
        }
    }

    private fun setAlarm(
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
                putExtra(AppConstant.Intent.PHONE_CALL_ALARM, false)
            } else {
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
        Log.v(
            "SET_ALARM",
            ">>> $title: Time: ${calendar.formatTime()} Date: ${calendar.formatDate()}"
        )
    }

    private fun setRepeatingAlarm(
        calendar: Calendar,
        requestCode: Int,
        title: String = ""
    ) {

        Log.v("REPEAT_ALARM", ">>> Repeat Alarm ---> ")
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
        Log.v("SET_ALARM", ">>> Repeating -> $title: ${calendar.formatTime()}")
    }

    private fun setNonRepeatingAlarm(timeStamp: Long, requestCode: Int, title: String) {
        Log.v("REPEAT_ALARM", ">>> Non-Repeat Alarm ---> ")

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeStamp
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
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
        Log.v("SET_ALARM", ">>> Non-Repeating -> $title: ${calendar.formatTime()}")
    }

    private fun Boolean.toggleAlarm(timeStamp: Long, alarmId: Int) {

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeStamp
        val requestCode = (timeStamp).toInt() * 1000
        cancelAlarm(requestCode)

        if (this) {
            setRepeatingAlarm(calendar, requestCode, getAlarmTitle(addAlarmCalendar.timeInMillis))
        } else {
            cancelAlarm(requestCode)
            var handler: Handler?
            val handlerThread = HandlerThread(AppConstant.Handler.UPDATE_HANDLER)
            handlerThread.also {
                it.start()
                handler = Handler(it.looper)
            }
            handler?.post {
                val addAlarmDao = MessageRoomDatabase.getDatabase(application).addAlarmDao()
                val repository = AddAlarmRepository(addAlarmDao, alarmId)
                val alarm = repository.alarmById
                alarm.repeatAlarm = this
                repository.updateAlarm(alarm)
                Log.v(
                    "ALARM_BY_ID",
                    ">>> alarm: ${alarm.alarmTimeStamp.formatTime()} repeat: ${repository.alarmById.repeatAlarm}"
                )
            }
        }
    }

    private fun deleteAlarmById(msgId: Int) {
        Log.v("DELETE_MSG", ">>> Inside deleting Msg... <<<")
        BaseAsyncTask(object : BaseAsyncTask.SendSMSFromDb {
            override fun onStarted() {
                Log.v("DELETE_MSG", ">>> deleting Msg...")
                val addAlarmDao = MessageRoomDatabase.getDatabase(application).addAlarmDao()
                val repository = AddAlarmRepository(addAlarmDao, msgId)
                val alarmToDelete = repository.alarmById
                alarmData = alarmToDelete
                repository.deleteAlarm(alarmToDelete)
                cancelAlarm((alarmToDelete.alarmTimeStamp).toInt() * 1000)
            }

            override fun onCompleted() {
                if (alarmData.id != -1) {
                    "Alarm deleted successfully".showSnackbar(restoreData = {
                        mAlarmView.insert(
                            alarmData
                        )
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = alarmData.alarmTimeStamp
//                        setRepeatingAlarm(
//                            calendar = calendar,
//                            requestCode = ((alarmData.alarmTimeStamp).toInt() * 1000),
//                            title = getAlarmTitle(alarmData.alarmTimeStamp)
//                        )
                        setAlarm(
                            calendar = calendar,
                            requestCode = ((alarmData.alarmTimeStamp).toInt() * 1000),
                            title = getAlarmTitle(alarmData.alarmTimeStamp),
                            isAddMsgAlarm = true
                        )
                    })
                } else {
                    "Error while deleting alarm".showSnackbar { }
                }
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    private fun deletePhoneCallAlarm(msgId: Int) {
        Log.v("DELETE_MSG", ">>> Inside deleting Msg... <<<")
        BaseAsyncTask(object : BaseAsyncTask.SendSMSFromDb {
            override fun onStarted() {
                Log.v("DELETE_MSG", ">>> deleting Phone Call Alarm...")
                val phoneCallDao = MessageRoomDatabase.getDatabase(application).phoneCallAlarmDao()
                val repository = PhoneCallRepository(phoneCallDao, msgId)
                val alarmToDelete = repository.alarmById
                phoneCallAlarmData = alarmToDelete
                repository.deletePhoneCallAlarm(alarmToDelete)
                cancelAlarm((alarmToDelete.alarmTimeStamp).toInt() * 1000)
            }

            override fun onCompleted() {
                if (alarmData.id != -1) {
                    "Alarm deleted successfully".showSnackbar(restoreData = {
                        mPhoneCallViewModel.insertPhoneCallAlarm(
                            phoneCallAlarmData
                        )
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = alarmData.alarmTimeStamp
//                        setRepeatingAlarm(
//                            calendar = calendar,
//                            requestCode = ((alarmData.alarmTimeStamp).toInt() % 10 * 1000),
//                            title = getAlarmTitle(alarmData.alarmTimeStamp)
//                        )
                        setAlarm(
                            calendar = calendar,
                            requestCode = ((alarmData.alarmTimeStamp).toInt() * 1000),
                            title = getAlarmTitle(alarmData.alarmTimeStamp),
                            isAddMsgAlarm = false
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

    private val REQUEST_CALL_PHONE = 100
    private fun checkForCallPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CALL_PHONE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.CALL_PHONE
                ),
                REQUEST_CALL_PHONE
            )
        } else {
            // Permission has already been granted
            Log.v(
                "REQUEST_CALL_PHONE",
                ">>> CALL_PHONE Permission has already been granted..."
            )
        }
    }


}