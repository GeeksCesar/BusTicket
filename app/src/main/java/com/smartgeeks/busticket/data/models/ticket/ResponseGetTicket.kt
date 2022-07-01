package com.smartgeeks.busticket.data.models.ticket


import com.google.gson.annotations.SerializedName

data class ResponseGetTicket(
    val estado: Int,
    val ticket: Ticket
)