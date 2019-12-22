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
    const val MESSAGE = "message"

    const val DOZE_ALARM = "doze_alarm"
    const val CONFIRMATION_SMS_ALARM = "confirmation_sms_alarm"
    const val IS_AUTO_STARTUP = "is_auto_startup"

    object Status {
        const val SENT = "Sent"
        const val PENDING = "Pending"
        const val FAILURE = "Failure"
    }

    object Notification {
        const val PHONE_CALL = 99
        const val MSG_DELIVERED = 200

        object Channel {
            const val MESSAGE_CHANNEL_ID = "MessageChannel"
            const val MESSAGE_CHANNEL = "Message Channel"
            const val MESSAGE_CONFIRMATION_CHANNEL_ID = "MessageConfirmationChannelId"
            const val MESSAGE_CONFIRMATION_CHANNEL = "MessageConfirmationChannel"
            const val PHONE_CALL_CHANNEL_ID = "PhoneCallChannelId"
            const val PHONE_CALL_CHANNEL = "Phone Call Channel"
        }
    }


    object Reminder {
        const val TIME_STAMP = "reminder_timestamp"
        const val REMINDER_ID = "reminder_id"
        const val TITLE = "reminder_title"
    }

    object Handler {
        const val UPDATE_HANDLER = "UpdateHandlerThread"
        const val SENT_HANDLER = "SentHandlerThread"
        const val HOME_HANDLER = "HomeHandlerThread"
        const val DELETE_ALARM = "DeleteAlarm"
        const val ADD_ALARM = "AddAlarm"
        const val PHONE_CALL = "PhoneCall"
    }

    object Intent {
        const val ACTION_PHONE_CALL_ALARM = "action.phone.call.alarm"
        const val ACTION_MESSAGE_ALARM = "action.message.alarm"
        const val ACTION_DOZE_MODE_ALARM = "action.doze.alarm"
        const val ACTION_CONFIRMATION_SMS = "action.confirmation.sms"

        const val PHONE_CALL_ALARM = "phone_call_alarm"
        const val MESSAGE_ALARM = "message_alarm"
    }

    object Error {
        const val SENT_ERROR = "sent_error"
    }

    const val DEBUG_MOBILE_NO = "9867169318"

    object Color {
        val colorAsset = arrayOf(
            "#FFFFEBEE",
            "#FFFCE4EC",
            "#FFF3E5F5",
            "#FFF3E5F5",
            "#FFEDE7F6",
            "#FFE3F2FD",
            "#FFE1F5FE",
            "#FFE0F7FA",
            "#FFE0F2F1",
            "#FFE8F5E9",
            "#FFF1F8E9",
            "#FFF9FBE7",
            "#FFFFFDE7",
            "#FFFFF8E1",
            "#FFFFF3E0",
            "#FFFBE9E7"
        )
    }
}



//    val colorAsset = arrayOf(
//        R.color.dozeModeColor0,
//        R.color.dozeModeColor1,
//        R.color.dozeModeColor2,
//        R.color.dozeModeColor3,
//        R.color.dozeModeColor4,
//        R.color.dozeModeColor5,
//        R.color.dozeModeColor6,
//        R.color.dozeModeColor7,
//        R.color.dozeModeColor8,
//        R.color.dozeModeColor9,
//        R.color.dozeModeColor10,
//        R.color.dozeModeColor11,
//        R.color.dozeModeColor12,
//        R.color.dozeModeColor13,
//        R.color.dozeModeColor14,
//        R.color.dozeModeColor15,
//        R.color.dozeModeColor16
//    )