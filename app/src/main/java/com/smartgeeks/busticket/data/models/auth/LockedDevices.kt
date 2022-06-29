package com.smartgeeks.busticket.data.models.auth

data class LockedDevices(
    val DispositivosBloqueados: List<String>,
    val Id: String,
    val Nombre: String
)