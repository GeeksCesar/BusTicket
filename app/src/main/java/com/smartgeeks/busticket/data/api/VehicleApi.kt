package com.smartgeeks.busticket.data.api

import com.smartgeeks.busticket.data.models.vehicle.ResponseOccupiedSeats
import com.smartgeeks.busticket.data.models.vehicle.ResponseVehicleInfo
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface VehicleApi {

    @GET("apisync/getSillasOcupadas/{availableRouteId}/{hour}")
    suspend fun getOccupiedSeats(
        @Path("availableRouteId") availableRouteId: Int,
        @Path("hour") hour: String
    ): ResponseOccupiedSeats

    @GET("Api/getInfoVehiculo")
    suspend fun getVehicleInfo(
        @Query("id") vehicleId: Int
    ): ResponseVehicleInfo

}