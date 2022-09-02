package com.smartgeeks.busticket.data.models.ticket

data class ResponseGetTicket(
    val estado: Int,
    val ticket: Ticket,
    val mensaje: String
)