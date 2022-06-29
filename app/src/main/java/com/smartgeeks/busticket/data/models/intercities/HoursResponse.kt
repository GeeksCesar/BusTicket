package com.smartgeeks.busticket.data.models.intercities


import com.google.gson.annotations.SerializedName

data class HoursResponse(
    @SerializedName("Origen")
    val origen: Int,
    @SerializedName("Destino")
    val destino: Int,
    @SerializedName("Horario")
    val horario: String,
    @SerializedName("FechaInicio")
    val fechaInicio: String,
    @SerializedName("FechaFin")
    val fechaFin: String,
    @SerializedName("Dia")
    val dia: Int
)