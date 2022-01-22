package com.smartgeeks.busticket.data.vehicle

data class ResponseOccupiedSeats(
    val estado: Int,
    val sillas_ocupadas: List<SillaOcupada>
)

data class SillaOcupada(
    val destino: Int,
    val numeroSilla: Int,
    val origen: Int
)