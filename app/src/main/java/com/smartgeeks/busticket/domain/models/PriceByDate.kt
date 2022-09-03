package com.smartgeeks.busticket.domain.models

import java.io.Serializable

data class PriceByDate(
    val horario: String = "",
    val tarifa: String = "",
    val tarifaIdayvuelta: String = "",
    val idRuta: Int = 0,
    val origen: String = "",
    val destino: String = "",
    val idTipoPasajero: Int = 0,
    val dia: String = "",
    val fecha: String = "",
    val pasajero : String = ""
) : Serializable