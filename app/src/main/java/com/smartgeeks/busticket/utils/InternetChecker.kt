package com.smartgeeks.busticket.utils

import kotlinx.coroutines.coroutineScope
import java.net.InetSocketAddress
import java.net.Socket

object InternetChecker {

    suspend fun isInternetAvailable() : Boolean = coroutineScope {
        return@coroutineScope try {
            val socket = Socket()
            val socketAddress = InetSocketAddress("8.8.8.8", 53)
            socket.connect(socketAddress)
            socket.close()
            true
        } catch (e : Exception) {
            false
        }
    }

}