package com.infomantri.autosms.send

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*

class TimerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {
    override fun onTimeSet(p0: TimePicker?, hour: Int, minute: Int) {
        mOnTimeSelected?.selectedTime(
            if (hour >= 12) hour - 12 else hour,
            minute,
            if (hour >= 12) "PM" else "AM"
        )
    }

    private var mOnTimeSelected: OnTimeSelected? = null

    fun setTimeChangeListener(onDateSelected: OnTimeSelected) {
        mOnTimeSelected = onDateSelected
    }

    interface OnTimeSelected {
        fun selectedTime(hour: Int, minute: Int, unit: String)
    }

    companion object {

        const val HOUR = "hour"
        const val MINUTE = "minute"
        const val RESTRICT_FUTURE_TIME = "restrict_future_time"

        fun instance(dayOfMonth: Int, monthOfYear: Int, restrictFutureTime: Boolean = false): TimerFragment {
            val timerFragment = TimerFragment()
            val bundle = Bundle().apply {
                putInt(HOUR, dayOfMonth)
                putInt(MINUTE, monthOfYear)
                putBoolean(RESTRICT_FUTURE_TIME, restrictFutureTime)
            }

            timerFragment.arguments = bundle

            return timerFragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val timePickerDialog = TimePickerDialog(
            requireActivity(), this,
            arguments?.getInt(HOUR) ?: 8, arguments?.getInt(MINUTE) ?: 5, false
        )

        when {
            arguments?.getBoolean(RESTRICT_FUTURE_TIME) == true -> {
                timePickerDialog.updateTime( Calendar.DAY_OF_MONTH, Calendar.MINUTE)
            }

        }

        return  timePickerDialog
    }
}