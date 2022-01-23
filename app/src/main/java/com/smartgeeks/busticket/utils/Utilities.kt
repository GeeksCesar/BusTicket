package com.smartgeeks.busticket.utils

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
import pub.devrel.easypermissions.EasyPermissions
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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

    fun isBluetoothEnabled(context: Context) : Boolean {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        return bluetoothManager.adapter.isEnabled
    }

    fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
        val formatter = SimpleDateFormat(format, locale)
        return formatter.format(this)
    }

    fun getDate(format: String = "dd-MM-yyyy", locale: Locale = Locale.getDefault()): String {
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
}