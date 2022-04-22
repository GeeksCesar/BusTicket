package com.smartgeeks.busticket.data.models.intercities

import com.google.gson.annotations.SerializedName

data class RoutesIntercityResponse(
    @SerializedName("Id")
    val id: Int,
    @SerializedName("Ruta")
    val ruta: Int,
    @SerializedName("IdVehiculo")
    val IdVehiculo: Int,
    @SerializedName("Vehiculo")
    val vehiculo: String,
    @SerializedName("Inicio")
    val inicio: String,
    @SerializedName("Termino")
    val termino: String
)