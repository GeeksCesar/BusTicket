package com.smartgeeks.busticket.core

import android.app.Application
import com.orm.SugarContext
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