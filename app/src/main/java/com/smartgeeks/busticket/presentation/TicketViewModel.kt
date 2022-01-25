package com.smartgeeks.busticket.presentation

import android.util.Log
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
        vehicleId: Int
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
                        vehicleId
                    )
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "saveSeatTicket: ${e.message}")
            emit(Resource.Failure(e))
        }
    }

    fun saveTicketLocally(ticketEntity: TicketEntity) = viewModelScope.launch {
        ticketRepository.saveTicketLocally(ticketEntity)
    }

}