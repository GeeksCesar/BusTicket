package com.smartgeeks.busticket.data.models.ticket


import com.google.gson.annotations.SerializedName

data class Ticket(
    @SerializedName("Id")
    val id: String,
    @SerializedName("IdServicio")
    val idServicio: String,
    @SerializedName("IdCliente")
    val idCliente: String,
    @SerializedName("IdVehiculo")
    val idVehiculo: String,
    @SerializedName("Num_Voucher")
    val numVoucher: String,
    @SerializedName("Parada_Inicio")
    val paradaInicio: String,
    @SerializedName("Parada_Destino")
    val paradaDestino: String,
    @SerializedName("Id_Ruta_Disponible")
    val idRutaDisponible: String,
    @SerializedName("Id_Operador")
    val idOperador: String,
    @SerializedName("Hora_Salida")
    var horaSalida: String,
    @SerializedName("Tipo_Usuario")
    val tipoUsuario: String,
    @SerializedName("Fecha")
    var fecha: String,
    @SerializedName("Hora")
    val hora: String,
    @SerializedName("Fecha_Hora")
    val fechaHora: String,
    @SerializedName("Cant_Pasajes")
    val cantPasajes: String,
    @SerializedName("Total_Pagar")
    val totalPagar: String,
    @SerializedName("Estado")
    val estado: String,
    @SerializedName("EstadoQr")
    val estadoQr: String,
    @SerializedName("EstadoSilla")
    val estadoSilla: String,
    @SerializedName("Sincronizado")
    val sincronizado: Any,
    @SerializedName("Fecha_Sincronizacion")
    val fechaSincronizacion: Any,
    @SerializedName("FechaCanje")
    val fechaCanje: String,
    @SerializedName("HoraCanje")
    val horaCanje: String,
    @SerializedName("IdPay")
    val idPay: String,
    @SerializedName("IdDetallePago")
    val idDetallePago: String,
    @SerializedName("Sillas")
    val sillas: String,
    @SerializedName("Tipo_Ticket")
    val tipoTicket: String,
    @SerializedName("Origen")
    val origen: String,
    @SerializedName("TipoAsiento")
    val tipoAsiento: String,
    @SerializedName("IsRetencion")
    val isRetencion: String,
    @SerializedName("NombreParadaInicio")
    val nombreParadaInicio: String,
    @SerializedName("NombreParadaDestino")
    val nombreParadaDestino: String,
    @SerializedName("NombreRuta")
    val nombreRuta: String,
    @SerializedName("NombreTipoUsuario")
    val nombreTipoUsuario: String,
    @SerializedName("Empresa")
    val empresa: String,
    @SerializedName("Placa")
    val placa : String
)