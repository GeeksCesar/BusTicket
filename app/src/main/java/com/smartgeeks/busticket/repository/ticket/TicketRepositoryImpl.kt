package com.smartgeeks.busticket.repository.ticket

import com.smartgeeks.busticket.api.TicketApi
import com.smartgeeks.busticket.data.local.TicketDAO
import com.smartgeeks.busticket.data.local.entities.TicketEntity
import com.smartgeeks.busticket.data.ticket.ResponseSendSeatTicket
import javax.inject.Inject

class TicketRepositoryImpl @Inject constructor(
    private val ticketApi: TicketApi,
    private val ticketDAO: TicketDAO
) : TicketRepository {

    override suspend fun saveSeatTicket(
        idStartRoute: Int,
        idEndRoute: Int,
        routeId: Int,
        operatorId: Int,
        hour: String,
        userType: Int,
        totalCost: Double,
        seatsList: String,
        companyId: Int,
        vehicleId: Int
    ): ResponseSendSeatTicket = ticketApi.saveSeatTicket(
        idStartRoute,
        idEndRoute,
        routeId,
        operatorId,
        hour,
        userType,
        totalCost,
        seatsList,
        companyId,
        vehicleId
    )

    override suspend fun saveTicketLocally(ticketEntity: TicketEntity) {
        ticketDAO.insert(ticketEntity)
    }

}