package com.smartgeeks.busticket.data.models.auth

import com.google.gson.annotations.SerializedName

data class RequestSessionLogs(
    @SerializedName("Usuario")
    val userID: Int,
    val deviceID: String,
    val latLong: String
)
