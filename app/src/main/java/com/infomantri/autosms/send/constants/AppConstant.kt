package com.infomantri.autosms.send.constants

object AppConstant {

    const val REMINDER_TIMESTAMP = "reminder_timestamp"
    const val REMINDER_ID = "reminder_id"
    const val MOBILE_NO_1 = "mobile_no_1"
    const val MOBILE_NO_2 = "mobile_no_2"
    const val DEFAULT_MOBILE_NO = "default_mobile_no"

    const val MESSAGE_DELIVERED = "message_delivered"
    const val MESSAGE_SENT = "message_sent"
    const val NOTIFICATION_ID = 123456

    const val MESSAGE_ID = "message_id"

    object Reminder {
        const val TIME_STAMP = "reminder_timestamp"
        const val REMINDER_ID = "reminder_id"
        const val TITLE = "reminder_title"
    }

    object Handler {
        const val UPDATE_HANDLER = "UpdateHandlerThread"
        const val SENT_HANDLER = "SentHandlerThread"
        const val HOME_HANDLER = "HomeHandlerThread"
    }

    object Error {
        const val SENT_ERROR = "sent_error"
    }

    const val DEBUG_MOBILE_NO = "9867169318"

}