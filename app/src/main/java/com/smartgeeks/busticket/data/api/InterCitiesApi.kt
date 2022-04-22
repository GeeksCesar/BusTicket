package com.smartgeeks.busticket.data.api

import com.smartgeeks.busticket.data.models.PriceResponse
import com.smartgeeks.busticket.data.models.intercities.RoutesIntercityResponse
import com.smartgeeks.busticket.data.models.intercities.StopBusResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface InterCitiesApi {

    @GET("api/getRoutesIntercities/{companyId}")
    suspend fun getRoutesInterCities(
        @Path("companyId") companyId: Int
    ): List<RoutesIntercityResponse>

    @GET("api/getStopBusIntercities/{routeId}")
    suspend fun getStopBusInterCities(
        @Path("routeId") routeId: Int
    ): List<StopBusResponse>

    @FormUrlEncoded
    @POST("api/getPriceAndHours")
    suspend fun getPriceAndHours(
        @Field("departureId") departureId: Int,
        @Field("arrivalId") arrivalId: Int,
        @Field("date") date: String
    ): List<PriceResponse>


}