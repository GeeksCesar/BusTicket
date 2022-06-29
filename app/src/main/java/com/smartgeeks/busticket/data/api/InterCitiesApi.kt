package com.smartgeeks.busticket.data.api

import com.smartgeeks.busticket.data.models.PriceResponse
import com.smartgeeks.busticket.data.models.intercities.HoursResponse
import com.smartgeeks.busticket.data.models.intercities.RouteIntercityResponse
import com.smartgeeks.busticket.data.models.intercities.StopBusResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface InterCitiesApi {

    @GET("api/getRoutesIntercitiesAllBuses/{companyId}")
    suspend fun getRoutesInterCities(
        @Path("companyId") companyId: Int
    ): List<RouteIntercityResponse>

    @GET("api/getStopBusIntercities/{routeId}")
    suspend fun getStopBusInterCities(
        @Path("routeId") routeId: Int
    ): List<StopBusResponse>

    @FormUrlEncoded
    @POST("api/getPriceAndHours")
    suspend fun getPriceAndHours(
        @Field("departureId") departureId: Int,
        @Field("arrivalId") arrivalId: Int,
        @Field("date") date: String,
        @Field("hour") hour: String = "",
    ): List<PriceResponse>

    @FormUrlEncoded
    @POST("api/getHoursIntercities")
    suspend fun getHoursIntercities(
        @Field("departureId") departureId: Int,
        @Field("arrivalId") arrivalId: Int,
        @Field("date") date: String
    ): List<HoursResponse>


}