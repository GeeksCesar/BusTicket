package com.smartgeeks.busticket.data.api

import com.smartgeeks.busticket.data.models.PriceResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface PriceApi {

    @FormUrlEncoded
    @POST("api/getPriceByDate")
    suspend fun getPricesByDate(
        @Field("departureId") departureId: Int,
        @Field("arrivalId") arrivalId: Int,
        @Field("passengerType") passengerType: Int,
        @Field("date") date: String
    ): List<PriceResponse>
}