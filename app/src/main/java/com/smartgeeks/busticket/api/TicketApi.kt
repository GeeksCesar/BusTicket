package com.smartgeeks.busticket.api

import com.smartgeeks.busticket.data.auth.AuthResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface TicketApi {

    @FormUrlEncoded
    @POST("api/setTicketAsientoNew")
    suspend fun userLogin(
        @Field("email") email: String,
        @Field("pass") password: String
    ): AuthResponse
}