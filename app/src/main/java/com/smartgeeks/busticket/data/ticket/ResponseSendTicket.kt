package com.smartgeeks.busticket.data.ticket

data class ResponseSendTicket(
    val error: Boolean,
    val message: String,
    val num_voucher: String
)