package com.smartgeeks.busticket.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ticket")
data class TicketEntity(
    @PrimaryKey(autoGenerate = true)
    val id : Int,
    var paradaInicio: Int = 0,
    var paradaDestino: Int = 0,
    var idRutaDisponible: Int = 0,
    var idOperador: Int = 0,
    var horaSalida: String = "",
    var tipoUsuario: Int = 0,
    var fecha: String = "",
    var hora: String = "",
    var cantPasajes: Int = 0,
    var totalPagar: Double = 0.0,
    var idVehiculo: Int = 0,
    var voucher: String = ""
)
