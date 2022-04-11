package com.smartgeeks.busticket.repository.vehicle

import com.smartgeeks.busticket.data.api.VehicleApi
import com.smartgeeks.busticket.data.vehicle.ResponseOccupiedSeats
import com.smartgeeks.busticket.data.vehicle.ResponseVehicleInfo
import javax.inject.Inject

class VehicleRepositoryImpl @Inject constructor(
    private val vehicleApi: VehicleApi
) : VehicleRepository {

    override suspend fun getOccupiedSeats(
        availableRouteId: Int,
        hour: String
    ): ResponseOccupiedSeats = vehicleApi.getOccupiedSeats(availableRouteId, hour)

    override suspend fun getVehicleInfo(vehicleId: Int): ResponseVehicleInfo =
        vehicleApi.getVehicleInfo(vehicleId)
}