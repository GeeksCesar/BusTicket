package com.smartgeeks.busticket.data.models.vehicle

data class ResponseVehicleInfo(
    val vehiculos: List<Vehiculo>
)

data class Vehiculo(
    val can_sillas: Int,
    val idVehiculo: Int
)