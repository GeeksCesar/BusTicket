package com.smartgeeks.busticket.repository.ticket

import com.smartgeeks.busticket.data.local.entities.TicketEntity
import com.smartgeeks.busticket.data.ticket.ResponseSendSeatTicket

interface TicketRepository {
    suspend fun saveSeatTicket(
        idStartRoute: Int,
        idEndRoute: Int,
        routeId: Int,
        operatorId: Int,
        hour: String,
        userType: Int,
        totalCost: Double,
        seatsList: String,
        companyId: Int,
        vehicleId: Int,
    ): ResponseSendSeatTicket

    suspend fun saveTicketLocally(ticketEntity: TicketEntity)
}