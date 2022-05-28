package com.smartgeeks.busticket.data.models.intercities

import com.google.gson.annotations.SerializedName

data class Vehiculo(
    @SerializedName("IdVehiculo")
    val idVehiculo: Int,
    @SerializedName("Vehiculo")
    val vehiculo: String
)