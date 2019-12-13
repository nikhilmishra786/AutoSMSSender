package com.infomantri.autosms.send.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

private val REQUEST_CALL_PHONE = 100

fun Context.callPhoneNumber(mobileNumber: String) {
    val callIntent = Intent(Intent.ACTION_CALL, Uri.fromParts("tel", "+91$mobileNumber", null))
//            intent.data = Uri.parse("tel:+919867169318")
    if (checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
        startActivity(callIntent)
    }
}
