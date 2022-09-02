package com.smartgeeks.busticket.data.models.ticket

data class RefundTicketPayload(
    val id : Long,
    val numVoucher: String,
    val seatsToRefund: String,
    val seatsToMaintain: String,
)