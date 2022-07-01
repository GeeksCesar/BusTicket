package com.smartgeeks.busticket.repository.ticket

import com.smartgeeks.busticket.data.local.entities.TicketEntity
import com.smartgeeks.busticket.data.models.DefaultResponse
import com.smartgeeks.busticket.data.models.ticket.ResponseGetTicket
import com.smartgeeks.busticket.data.models.ticket.ResponseSaveTicket
import com.smartgeeks.busticket.data.models.ticket.ResponseSendSeatTicket
import com.smartgeeks.busticket.data.models.ticket.UpdateTicketPayload

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
        date: String,
        idService: Int,
        tipoTicket: String
    ): ResponseSendSeatTicket

    suspend fun saveTicket(ticketEntity: TicketEntity): Any

    /**
     * This method send all tickets saved in local database to server
     */
    suspend fun syncTickets(): ResponseSaveTicket

    suspend fun getTickets(): List<TicketEntity>

    suspend fun getCountTickets(): Int

    suspend fun searchTicketByVoucher(string: String): ResponseGetTicket

    suspend fun updateTicket(ticketPayload: UpdateTicketPayload): DefaultResponse
}