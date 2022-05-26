package com.smartgeeks.busticket.core

import android.content.Context
import android.content.SharedPreferences
import com.smartgeeks.busticket.utils.Constants

object AppPreferences {

    private lateinit var pref: SharedPreferences

    fun init(context: Context) {
        pref = context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    var isLockedDevice: Boolean
        get() = pref.getBoolean(IS_LOCKED_DEVICE, false)
        set(value) = pref.edit {
            it.putBoolean(IS_LOCKED_DEVICE, value)
        }

    /**
     * SharedPreferences extension function, so we won't need to call edit() and apply()
     * ourselves on every SharedPreferences operation.
     */
    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = edit()
        operation(editor)
        editor.apply()
    }

    /**
     * List of preferences in APP
     */
    private const val IS_LOCKED_DEVICE = "is_locked_device"

}