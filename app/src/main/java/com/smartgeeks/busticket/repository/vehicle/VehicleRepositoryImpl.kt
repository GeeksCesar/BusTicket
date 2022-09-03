package com.smartgeeks.busticket.repository.vehicle

import com.smartgeeks.busticket.data.api.VehicleApi
import com.smartgeeks.busticket.data.models.vehicle.ResponseOccupiedSeats
import com.smartgeeks.busticket.data.models.vehicle.ResponseVehicleInfo
import javax.inject.Inject

class VehicleRepositoryImpl @Inject constructor(
    private val vehicleApi: VehicleApi
) : VehicleRepository {

    override suspend fun getOccupiedSeats(
        availableRouteId: Int,
        hour: String,
        date : String,
        serviceId : Int,
        vehicleId : Int
    ): ResponseOccupiedSeats = vehicleApi.getOccupiedSeats(availableRouteId, hour, date, serviceId, vehicleId)

    override suspend fun getVehicleInfo(vehicleId: Int, type : String): ResponseVehicleInfo =
        vehicleApi.getVehicleInfo(vehicleId, type)
}