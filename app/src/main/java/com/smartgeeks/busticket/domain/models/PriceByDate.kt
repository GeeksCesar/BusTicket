package com.smartgeeks.busticket.domain.models

import java.io.Serializable

data class PriceByDate(
    val horario: String,
    val tarifa: String,
    val tarifaIdayvuelta: String,
    val idRuta: Int,
    val origen: String,
    val destino: String,
    val idTipoPasajero: Int,
    val dia: String,
    val fecha: String
) : Serializable