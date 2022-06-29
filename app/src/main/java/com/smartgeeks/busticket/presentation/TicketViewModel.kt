package com.smartgeeks.busticket.presentation

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.smartgeeks.busticket.core.Resource
import com.smartgeeks.busticket.data.local.entities.TicketEntity
import com.smartgeeks.busticket.repository.ticket.TicketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

private val TAG: String = VehicleViewModel::class.java.simpleName

@HiltViewModel
class TicketViewModel @Inject constructor(
    private val ticketRepository: TicketRepository
) : ViewModel() {

    var isSyncing : Boolean = false

    fun saveSeatTicket(
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
        date: String = "",
        tipoTicket : String = "SoloIda",
        idService: Int = 0,
    ) = liveData(Dispatchers.IO) {
        emit(Resource.Loading())
        try {
            emit(
                Resource.Success(
                    ticketRepository.saveSeatTicket(
                        idStartRoute,
                        idEndRoute,
                        routeId,
                        operatorId,
                        hour,
                        userType,
                        totalCost,
                        seatsList,
                        companyId,
                        vehicleId,
                        date, idService,
                        tipoTicket
                    )
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "saveSeatTicket: ${e.message}")
            emit(Resource.Failure(e))
        }
    }

    fun saveTicket(ticketEntity: TicketEntity) = liveData(Dispatchers.IO) {
        emit(Resource.Loading())
        try {
            val response = ticketRepository.saveTicket(ticketEntity)
            Log.e(TAG, "saveTicket: $response")
            emit(Resource.Success(response))
        } catch (e: Exception) {
            Log.e(TAG, "saveTicket: ${e.message}")
            emit(Resource.Failure(e))
        }
    }

    fun syncTickets() = liveData(Dispatchers.IO) {
        isSyncing = true
        emit(Resource.Loading())
        isSyncing = try {
            emit(Resource.Success(ticketRepository.syncTickets()))
            false
        } catch (e: Exception) {
            emit(Resource.Failure(e))
            false
        }
    }

    fun getTickets() = liveData(Dispatchers.IO) {
        emit(Resource.Loading())
        try {
            emit(Resource.Success(ticketRepository.getTickets()))
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }

    fun getCountTickets() = liveData(Dispatchers.IO) {
        emit(ticketRepository.getCountTickets())
    }
}