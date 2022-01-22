package com.smartgeeks.busticket.repository.vehicle

import com.smartgeeks.busticket.data.vehicle.ResponseOccupiedSeats
import com.smartgeeks.busticket.data.vehicle.ResponseVehicleInfo

interface VehicleRepository {
    suspend fun getOccupiedSeats(availableRouteId: Int, hour: String) : ResponseOccupiedSeats
    suspend fun getVehicleInfo(vehicleId: Int) : ResponseVehicleInfo
}