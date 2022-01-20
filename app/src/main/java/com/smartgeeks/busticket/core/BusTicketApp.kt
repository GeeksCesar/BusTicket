package com.smartgeeks.busticket.core

import android.app.Application
import android.util.Log
import com.orm.SugarContext
import com.smartgeeks.busticket.core.AppPreferences
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BusTicketApp : Application() {

    override fun onCreate() {
        super.onCreate()
        SugarContext.init(this)
        AppPreferences.init(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        SugarContext.terminate()
    }
}