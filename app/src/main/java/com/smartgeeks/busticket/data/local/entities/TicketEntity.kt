package com.smartgeeks.busticket.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ticket")
data class TicketEntity(
    @PrimaryKey(autoGenerate = true)
    val id : Int,
    var paradaInicio: Int,
    var paradaDestino: Int,
    var idRutaDisponible: Int,
    var idOperador: Int,
    var horaSalida: String,
    var tipoUsuario: Int,
    var fecha: String,
    var hora: String,
    var cantPasajes: Int,
    var totalPagar: Double,
    var idVehiculo: Int,
    var voucher: String = ""
)
