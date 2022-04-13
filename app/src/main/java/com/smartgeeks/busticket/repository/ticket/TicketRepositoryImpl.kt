package com.smartgeeks.busticket.repository.ticket

import android.app.Application
import android.util.Log
import com.smartgeeks.busticket.data.api.TicketApi
import com.smartgeeks.busticket.data.local.TicketDAO
import com.smartgeeks.busticket.data.local.entities.TicketEntity
import com.smartgeeks.busticket.data.models.ticket.ResponseSaveTicket
import com.smartgeeks.busticket.data.models.ticket.ResponseSendSeatTicket
import com.smartgeeks.busticket.utils.InternetChecker
import javax.inject.Inject

private val TAG: String = TicketRepositoryImpl::class.java.simpleName

class TicketRepositoryImpl @Inject constructor(
    private val ticketApi: TicketApi,
    private val ticketDAO: TicketDAO,
    private val context: Application
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

    override suspend fun saveTicket(ticketEntity: TicketEntity): Any {

        return if (InternetChecker.checkForInternetConnection(context)) {
            Log.e("Repo saveTicket", "Internet: ")
            val response = ticketApi.saveTicket(ticketEntity)
            if (response.estado == 1 && ticketEntity.id > 0) {
                Log.e(TAG, "Deleting ticket: ${ticketEntity.id}")
                ticketDAO.delete(ticketEntity)
            }
            response
        } else {
            Log.e("Repo saveTicket", "NO Internet: ")
            ticketDAO.insert(ticketEntity)
        }
    }

    override suspend fun syncTickets(): ResponseSaveTicket {
        return if (InternetChecker.checkForInternetConnection(context)) {

            for (ticket: TicketEntity in ticketDAO.getAllTickets()) {
                Log.e(TAG, "Sincronizando ticket: ${ticket.id}")
                val response = ticketApi.saveTicket(ticket)
                if (response.estado == 1 && response.remoto != 0) {
                    Log.e(TAG, "Deleting ticket: ${ticket.id}")
                    ticketDAO.delete(ticket)
                }
            }

            ResponseSaveTicket("Tickets sincronizados", 1)
        } else {
            ResponseSaveTicket("No hay conexión a internet", 0)
        }
    }

    override suspend fun getTickets(): List<TicketEntity> {
        return ticketDAO.getAllTickets()
    }

    override suspend fun getCountTickets(): Int = ticketDAO.getCountTickets()
}