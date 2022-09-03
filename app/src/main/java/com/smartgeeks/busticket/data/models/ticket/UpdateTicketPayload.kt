package com.smartgeeks.busticket.data.models.ticket

data class UpdateTicketPayload(
    val numVoucher: String,
    val horaSalida: String,
    val fecha: String,
    val sillas : String
)