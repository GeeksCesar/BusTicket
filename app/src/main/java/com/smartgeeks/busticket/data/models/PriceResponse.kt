package com.smartgeeks.busticket.data.models

import com.google.gson.annotations.SerializedName
import com.smartgeeks.busticket.domain.models.PriceByDate

data class PriceResponse(
    val horario: String,
    val tarifa: String,
    @SerializedName("tarifa_idayvuelta") val tarifaIdayvuelta: String,
    @SerializedName("id_ruta") val idRuta: String,
    val origen: String,
    val destino: String,
    @SerializedName("id_tipo_pasajero") val idTipoPasajero: String,
    val dia: String,
    val fecha: String
) {
    fun toDomain() = PriceByDate(
        horario,
        tarifa,
        tarifaIdayvuelta,
        idRuta.toInt(),
        origen,
        destino,
        idTipoPasajero,
        dia,
        fecha
    )
}

