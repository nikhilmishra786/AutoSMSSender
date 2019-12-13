package com.infomantri.autosms.send.activity

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.opengl.Visibility
import android.os.AsyncTask
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.SimpleAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.infomantri.autosms.send.R
import com.infomantri.autosms.send.adapter.MessageListAdapter
import com.infomantri.autosms.send.adapter.SwipeToDeleteCallback
import com.infomantri.autosms.send.asynctask.BaseAsyncTask
import com.infomantri.autosms.send.base.BaseActivity
import com.infomantri.autosms.send.database.MessageDbRepository
import com.infomantri.autosms.send.database.MessageRoomDatabase
import com.infomantri.autosms.send.viewmodel.MessageViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bootom_sheet.*
import kotlinx.android.synthetic.main.custom_toolbar.*
import java.security.Permission

class HomeActivity : BaseActivity() {

    private lateinit var mViewModel: MessageViewModel
    private var mSwipePosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mViewModel = ViewModelProviders.of(this).get(MessageViewModel::class.java)

        setToolbar()
        setOnClickListener()
        checkForSmsPermission()
        setRecyclerView()
//        observeDeleteMessage()


    }

    private fun setToolbar() {
        toolIvHome.visibility = View.GONE
        toolbar.setToolbar(
            false,
            titleColor = R.color.white,
            centerTitle = "SMS Sender",
            bgColor = R.color.colorPrimary
        )
    }

    private fun setOnClickListener() {
        val fab: View = findViewById(R.id.fabAddMessage)
        fab.setOnClickListener {
            val intent = Intent(this, AddMessages::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        toolIvAddAlarm.setOnClickListener {
            val intent = Intent(this, AddAlarmsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        toolIvSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        btnPhoneCall.setOnClickListener {
            val callIntent = Intent(Intent.ACTION_CALL, Uri.fromParts("tel", "+919867169318", null))
//            intent.data = Uri.parse("tel:+919867169318")
            if (checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                startActivity(callIntent)
            } else {
                checkForCallPermission()
            }
        }
    }

    private fun setRecyclerView() {
        val adapter = MessageListAdapter(copiedText = { copiedText, msg, id ->
            if (id != -1) {
                deleteMsgById(id)
            } else {
                copyToClipBoard(copiedText, msg)
            }
        })
        recyclerview.adapter = adapter
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerview.layoutManager = linearLayoutManager

        val swipeHandler = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                Log.v("onSwiped", ">>> postion: -> ${viewHolder.adapterPosition}")
                adapter.removeAt(viewHolder.adapterPosition)
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(recyclerview)

        mViewModel.allMessages.observe(this, Observer { list ->
            adapter.submitList(list)
        })
    }

    private fun copyToClipBoard(copiedText: String, msg: String) {
        val clipBoardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("MESSAGE", copiedText)
        clipBoardManager.setPrimaryClip(clipData)
        Toast.makeText(this, "Message copied to clipboard", Toast.LENGTH_SHORT).show()

//        showAlertDialog(deleteMsg = {
//            Log.v("DIALOG", ">>> return from Alert Dialog 1...")
//            deleteMessage(msg)
//            Log.v("DIALOG", ">>> return from Alert Dialog 2...")
//        })
    }

    private fun deleteMsgById(msgId: Int) {
        Log.v("DELETE_MSG", ">>> Inside deleting Msg... <<<")
        BaseAsyncTask(object : BaseAsyncTask.SendSMSFromDb {
            override fun onStarted() {
                Log.v("DELETE_MSG", ">>> deleting Msg...")
                val msgDao = MessageRoomDatabase.getDatabase(application).messageDbDao()
                val repository = MessageDbRepository(msgDao, msgId)
                val message = repository.messageById
                messageData = message
                repository.deleteMessage(message)
            }

            override fun onCompleted() {
                if (messageData.message != "error") {
                    "Message deleted successfully".showSnackbar(restoreData = {
                        mViewModel.insert(
                            messageData
                        )
                    })
                } else {
                    "Error while deleting message".showSnackbar { }
                }
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    var messageData =
        com.infomantri.autosms.send.database.Message("error", System.currentTimeMillis())

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                true
            }

            R.id.action_set_alarm -> {
                val intent = Intent(this, AddAlarmsActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                return true
            }
            else -> {

                return super.onOptionsItemSelected(item)
            }

        }
    }

    private fun setBootomSheet() {
        val bottomSheetLayout: LinearLayout = findViewById(R.id.bottomSheet)
        val bottomSheetBehavior: BottomSheetBehavior<LinearLayout> =
            BottomSheetBehavior.from(bottomSheetLayout)

        bottomSheet.setOnClickListener {
            if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
    }

    private val REQUEST_PERMISSION_SEND_SMS = 99
    private val REQUEST_CALL_PHONE = 100
    private fun checkForSmsPermission() {
// Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.SEND_SMS
                )
            ) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            }
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.SEND_SMS
                ),
                REQUEST_PERMISSION_SEND_SMS
            )

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.

        } else {
            // Permission has already been granted
            Log.v("REQUEST_PERMISSION_SMS", ">>> SMS Permission has already been granted...")
        }


    }

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
                "REQUEST_PERMISSION_CALL_PHONE",
                ">>> CALL_PHONE Permission has already been granted..."
            )
        }
    }

}
