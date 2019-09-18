package com.infomantri.autosms.send.database

data class CallDetails(
    val name: String,
    val number: String,
    val duration: String,
    val type: String,
    val dayTime: String
)