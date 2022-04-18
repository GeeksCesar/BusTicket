package com.smartgeeks.busticket.utils

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
import pub.devrel.easypermissions.EasyPermissions
import java.text.DateFormat
import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object Utilities {

    fun getDeviceId(context: Context): String =
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

    fun hasLocationPermission(context: Context) = EasyPermissions.hasPermissions(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    fun checkLocationPermission(context: Context): Boolean = (ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED ||
        ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)

    fun hasBluetoothPermission(context: Context) = EasyPermissions.hasPermissions(
        context,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_CONNECT
    )

    fun isBluetoothEnabled(context: Context): Boolean {
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        return bluetoothManager.adapter.isEnabled
    }

    fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
        val formatter = SimpleDateFormat(format, locale)
        return formatter.format(this)
    }

    fun getDate(format: String = "yyyy-MM-dd", locale: Locale = Locale.getDefault()): String {
        val df: DateFormat = SimpleDateFormat(format, locale)
        return df.format(Calendar.getInstance().time)
    }

    fun getTime(format: String = "HH:mm:ss", locale: Locale = Locale.getDefault()): String {
        val dateFormat: DateFormat = SimpleDateFormat(format, locale)
        return dateFormat.format(Calendar.getInstance().time)
    }

    fun getCurrentDateTime(): Date {
        return Calendar.getInstance().time
    }

    fun getTimeByTimezone(
        format: String = "yyyy-MM-dd",
        timezone: String = "America/Santiago"
    ): String {
        val c = Calendar.getInstance()
        val date = c.time //current date and time in UTC
        val df = SimpleDateFormat(format, Locale.getDefault())
        df.timeZone = TimeZone.getTimeZone(timezone) //format in given timezone
        return df.format(date)
    }

    fun getVoucherName(idVehicle: Int, idOperator: Int, date: String, hour: String): String {
        var voucher = ""
        voucher += idVehicle.toString()
        voucher += idOperator.toString()
        voucher += "-" + stringToDate(date)?.toString("ddMMyy")
        voucher += "-" + hour.split(":").joinToString("")
        return voucher
    }

    /**
     * This method return the date given a date in string
     */
    fun stringToDate(date: String, format: String = "yyyy-MM-dd"): Date? {
        val formatter = SimpleDateFormat(format, Locale.getDefault())
        return formatter.parse(date)
    }

    fun string2Calendar(dateStr: String, pattern: String = "dd/MM/yyyy"): Calendar {
        val calendar = Calendar.getInstance()
        val format = SimpleDateFormat(pattern, Locale.getDefault())
        try {
            val date = format.parse(dateStr)
            calendar.time = date
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return calendar
    }

    fun String.formatCurrency(): String {
        val currencyType = NumberFormat.getCurrencyInstance(Locale.getDefault())
        currencyType.maximumFractionDigits = 0
        return currencyType.format(this.toDouble()).replace(",",".")
    }


    fun String.formatDate(inputFormat : String = "dd/MM/yyyy", outputFormat : String = "yyyy-MM-dd"): String {
        val inputFormatter = SimpleDateFormat(inputFormat, Locale.getDefault())
        val outputFormatter = SimpleDateFormat(outputFormat, Locale.getDefault())
        return try {
            inputFormatter.parse(this)?.let { outputFormatter.format(it) } ?: this
        } catch (e: ParseException) {
            e.printStackTrace()
            this
        }
    }

}