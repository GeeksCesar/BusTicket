package com.smartgeeks.busticket.domain.models

data class PriceByDate(
    val horario: String,
    val tarifa: String,
    val tarifaIdayvuelta: String,
    val idRuta: String,
    val origen: String,
    val destino: String,
    val idTipoPasajero: String,
    val dia: String
)