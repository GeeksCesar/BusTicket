package com.smartgeeks.busticket.utils

import android.content.Context
import android.provider.Settings

object Utilities {

    fun getDeviceId(context: Context): String =
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
}