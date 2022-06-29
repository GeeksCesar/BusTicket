package com.smartgeeks.busticket.utils

import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection

object Constants {

    const val SHARED_PREFERENCES_NAME = "sharedPrefBusticket"

    const val SUCCESS_RESPONSE = 1
    const val FAILED_RESPONSE = 0

    var selectedDevice : BluetoothConnection? = null

}