package com.smartgeeks.busticket.data.ticket

data class ResponseSendSeatTicket(
    val error: Boolean,
    val message: String,
    val num_voucher: String,
    val silla : List<String> = listOf()
)