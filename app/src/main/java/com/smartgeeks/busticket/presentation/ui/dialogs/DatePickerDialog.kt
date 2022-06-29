package com.smartgeeks.busticket.presentation.ui.dialogs

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import com.smartgeeks.busticket.R
import com.smartgeeks.busticket.utils.Utilities
import java.util.Calendar

class DatePickerDialog(
    private val dateStr: String,
    val listener: (day: String, month: String, year: String) -> Unit
) : DialogFragment(),
    DatePickerDialog.OnDateSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val currentDate = Calendar.getInstance()

        val calendar = if (dateStr.isNotEmpty()) {
            Utilities.string2Calendar(dateStr)
        } else {
            Calendar.getInstance()
        }

        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONDAY)
        val year = calendar.get(Calendar.YEAR)

        val picker =
            DatePickerDialog(activity as Context, R.style.datePickerTheme, this, year, month, day)
        picker.datePicker.minDate = currentDate.timeInMillis
        return picker
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val monthString = if (month >= 10) "${month + 1}" else "0${month + 1}"
        val dayString = if (dayOfMonth >= 10) "$dayOfMonth" else "0${dayOfMonth}"
        listener(dayString, monthString, year.toString())
    }
}