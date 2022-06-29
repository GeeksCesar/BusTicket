package com.smartgeeks.busticket.data.api

import com.smartgeeks.busticket.data.models.vehicle.ResponseOccupiedSeats
import com.smartgeeks.busticket.data.models.vehicle.ResponseVehicleInfo
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface VehicleApi {

    @GET("apisync/getSillasOcupadasV2/{availableRouteId}/{hour}/{date}/{serviceId}")
    suspend fun getOccupiedSeats(
        @Path("availableRouteId") availableRouteId: Int,
        @Path("hour") hour: String,
        @Path("date") date: String,
        @Path("serviceId") serviceId: Int = 0,
    ): ResponseOccupiedSeats

    @GET("Api/getInfoVehiculo")
    suspend fun getVehicleInfo(
        @Query("id") vehicleId: Int,
        @Query("type") type: String = "default"
    ): ResponseVehicleInfo

}