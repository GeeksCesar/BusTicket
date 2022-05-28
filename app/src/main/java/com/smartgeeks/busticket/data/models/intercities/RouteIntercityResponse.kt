package com.smartgeeks.busticket.data.models.intercities


import com.google.gson.annotations.SerializedName

data class RouteIntercityResponse(
    @SerializedName("Id")
    val id: Int,
    @SerializedName("Ruta")
    val ruta: Int,
    @SerializedName("Inicio")
    val inicio: String,
    @SerializedName("Termino")
    val termino: String,
    @SerializedName("Vehiculos")
    val vehiculos: List<Vehiculo> = arrayListOf()
)