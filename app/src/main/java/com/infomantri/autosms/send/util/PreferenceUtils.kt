package com.syngenta.pack.util

import androidx.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity

fun AppCompatActivity.addStringToPreference(key: String, value: String?) {
    PreferenceManager.getDefaultSharedPreferences(this).edit()
        .putString(key, value).apply()
}

fun AppCompatActivity.getStringFromPreference(key: String): String? {
    return PreferenceManager.getDefaultSharedPreferences(this).getString(key, null)
}

fun AppCompatActivity.getBooleanFromPreference(key: String): Boolean? {
    return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(key, false)
}

fun AppCompatActivity.setBooleanToPreferences(key: String, value: Boolean) {
    PreferenceManager.getDefaultSharedPreferences(this).edit()
        .putBoolean(key, value).apply()
}

fun AppCompatActivity.addIntToPreference(key: String, value: Int) {
    PreferenceManager.getDefaultSharedPreferences(this).edit()
        .putInt(key, value).apply()
}

fun AppCompatActivity.getIntFromPreference(key: String): Int? {
    return PreferenceManager.getDefaultSharedPreferences(this).getInt(key, 0)
}

fun AppCompatActivity.clearPreference() {
    PreferenceManager.getDefaultSharedPreferences(this).edit().clear().apply()
}
