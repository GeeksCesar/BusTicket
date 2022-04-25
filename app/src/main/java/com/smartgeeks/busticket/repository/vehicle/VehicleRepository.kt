package com.smartgeeks.busticket.repository.vehicle

import com.smartgeeks.busticket.data.models.vehicle.ResponseOccupiedSeats
import com.smartgeeks.busticket.data.models.vehicle.ResponseVehicleInfo

interface VehicleRepository {
    suspend fun getOccupiedSeats(availableRouteId: Int, hour: String, date : String) : ResponseOccupiedSeats
    suspend fun getVehicleInfo(vehicleId: Int, type : String) : ResponseVehicleInfo
}