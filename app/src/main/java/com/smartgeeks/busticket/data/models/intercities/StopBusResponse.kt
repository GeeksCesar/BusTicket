package com.smartgeeks.busticket.data.models.intercities


import com.google.gson.annotations.SerializedName

data class StopBusResponse(
    @SerializedName("IdParadero")
    val idParadero: Int,
    @SerializedName("Paradero")
    val paradero: String,
    @SerializedName("Tipo")
    val tipo: String
)